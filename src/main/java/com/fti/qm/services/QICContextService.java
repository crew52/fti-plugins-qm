package com.fti.qm.services;

import java.util.*;

import com.fti.qm.constants.qualityInspectionCommand.QICContextFields;
import com.fti.qm.criteriaModifiers.QICCriteriaModifiersCMP;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class QICContextService {

    private static final String L_QUALITY_INSPECTION_COMMAND = "qualityInspectionCommand";
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void confirmOrChangeContext(ViewDefinitionState view, ComponentState componentState, String[] args) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity qICContextEntity = prepareContextEntity(formComponent.getEntity());

        if (qICContextEntity.getBooleanField(QICContextFields.CONFIRMED)) {
            qICContextEntity = changeContext(view, qICContextEntity);
        } else {
            Date dateFrom = qICContextEntity.getDateField(QICContextFields.DATE_FROM);
            Date dateTo = qICContextEntity.getDateField(QICContextFields.DATE_TO);
            if(Objects.nonNull(dateFrom) && Objects.nonNull(dateTo) && dateTo.before(dateFrom)) {
                view.addMessage("qm.qualityInspectionCommandList.window.contextTab.error.datesOrder", ComponentState.MessageType.FAILURE);
                return;
            }
            qICContextEntity = confirmContext(qICContextEntity, args);
        }

        formComponent.setEntity(qICContextEntity);
    }

    public Entity prepareContextEntity(Entity qICContextEntity) {
        SearchCriteriaBuilder searchCriteriaBuilder = qICContextEntity.getDataDefinition().find();
//         Belong to
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(QICContextFields.COMPANY, qICContextEntity.getBelongsToField(QICContextFields.COMPANY)));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(QICContextFields.PRODUCT, qICContextEntity.getBelongsToField(QICContextFields.PRODUCT)));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(QICContextFields.TOOL, qICContextEntity.getBelongsToField(QICContextFields.TOOL)));
//        Name
        searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.COMPANY_NAME, qICContextEntity.getStringField(QICContextFields.COMPANY_NAME)));
        searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.PRODUCT_NAME, qICContextEntity.getStringField(QICContextFields.PRODUCT_NAME)));
        searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.TOOL_NAME, qICContextEntity.getStringField(QICContextFields.TOOL_NAME)));
//        Enum
        searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.INSPECTION_TYPE, qICContextEntity.getStringField(QICContextFields.INSPECTION_TYPE)));
        searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.STATUS, qICContextEntity.getStringField(QICContextFields.STATUS)));
//        else
        searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.PRODUCTION_ORDER_NUMBER, qICContextEntity.getStringField(QICContextFields.PRODUCTION_ORDER_NUMBER)));
        searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.OPERATION_NUMBER, qICContextEntity.getStringField(QICContextFields.OPERATION_NUMBER)));

        Date dateFrom = qICContextEntity.getDateField(QICContextFields.DATE_FROM);
        Date dateTo = qICContextEntity.getDateField(QICContextFields.DATE_TO);

        if(Objects.nonNull(dateFrom)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.DATE_FROM, dateFrom));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.isNull(QICContextFields.DATE_FROM));
        }
        if(Objects.nonNull(dateTo)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.DATE_TO, dateTo));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.isNull(QICContextFields.DATE_TO));
        }

        Entity qICContextEntityFromDb = searchCriteriaBuilder.uniqueResult();

        if (qICContextEntityFromDb == null) {
            qICContextEntity.setField(QICContextFields.CONFIRMED, false);
            qICContextEntity.setField(QICContextFields.DATE_FROM, dateFrom);
            qICContextEntity.setField(QICContextFields.DATE_TO, dateTo);
            qICContextEntity = qICContextEntity.getDataDefinition().save(qICContextEntity);
        } else {
            Long id = qICContextEntity.getId();
            qICContextEntity = qICContextEntityFromDb;

            if (id == null) {
                qICContextEntity.setField(QICContextFields.CONFIRMED, false);
            }
        }

        return qICContextEntity;
    }
    private Entity changeContext(ViewDefinitionState view, Entity qICContextEntity) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        qICContextEntity.setField(QICContextFields.CONFIRMED, false);
        qICContextEntity = qICContextEntity.getDataDefinition().save(qICContextEntity);
        formComponent.setEntity(qICContextEntity);

        return qICContextEntity;
    }

    private Entity confirmContext(Entity qICContextEntity, String[] args) {
        qICContextEntity.setField(QICContextFields.CONFIRMED, true);
        qICContextEntity = qICContextEntity.getDataDefinition().save(qICContextEntity);

        Long maintenanceQICContextEntityId = qICContextEntity.getId();

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("contextId", maintenanceQICContextEntityId);

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);

        if (args.length > 0 && args[0].equals(L_QUALITY_INSPECTION_COMMAND)) {
            String sql = "update qm_qualityInspectionCommand set qualityInspectionCommandContext_id = :contextId";

            jdbcTemplate.update(sql, namedParameters);

        }

        return qICContextEntity;
    }

//    private void setGridFilterParameters(ViewDefinitionState view, Entity qICContextEntity) {
//        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
//
//        FilterValueHolder filterValueHolder = gridComponent.getFilterValue();
//
//        Entity companyEntity = qICContextEntity.getBelongsToField(QICContextFields.COMPANY);
//        if (companyEntity != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_COMPANY, Math.toIntExact(companyEntity.getId()));
//        }
//
//        Entity productEntity = qICContextEntity.getBelongsToField(QICContextFields.PRODUCT);
//        if (productEntity != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_PRODUCT, Math.toIntExact(productEntity.getId()));
//        }
//
//        Entity toolEntity = qICContextEntity.getBelongsToField(QICContextFields.TOOL);
//        if (toolEntity != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_TOOL, Math.toIntExact(toolEntity.getId()));
//        }
//
//        // --- String fields ---
//        String companyNameField = qICContextEntity.getStringField(QICContextFields.COMPANY_NAME);
//        if (companyNameField != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_COMPANY_NAME, companyNameField);
//        }
//
//        String productNameField = qICContextEntity.getStringField(QICContextFields.PRODUCT_NAME);
//        if (productNameField != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_PRODUCT_NAME, productNameField);
//        }
//
//        String toolNameField = qICContextEntity.getStringField(QICContextFields.TOOL_NAME);
//        if (toolNameField != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_TOOL_NAME, toolNameField);
//        }
//
//        String inspectionTypeField = qICContextEntity.getStringField(QICContextFields.INSPECTION_TYPE);
//        if (inspectionTypeField != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_INSPECTION_TYPE, inspectionTypeField);
//        }
//
//        String statusField = qICContextEntity.getStringField(QICContextFields.STATUS);
//        if (statusField != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_STATUS, statusField);
//        }
//
//        String productionOrderNumberField = qICContextEntity.getStringField(QICContextFields.PRODUCTION_ORDER_NUMBER);
//        if (productionOrderNumberField != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_PRODUCTION_ORDER_NUMBER, productionOrderNumberField);
//        }
//
//        String operationNumberField = qICContextEntity.getStringField(QICContextFields.OPERATION_NUMBER);
//        if (operationNumberField != null) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_OPERATION_NUMBER, operationNumberField);
//        }
//
//        Date dateFromField = qICContextEntity.getDateField(QICContextFields.DATE_FROM);
//        if (Objects.nonNull(dateFromField)) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_DATE_FROM, dateFromField.getTime());
//        }
//
//        Date dateToField = qICContextEntity.getDateField(QICContextFields.DATE_TO);
//        if (Objects.nonNull(dateToField)) {
//            filterValueHolder.put(QICCriteriaModifiersCMP.L_DATE_TO, dateToField.getTime());
//        }
//
//        gridComponent.setFilterValue(filterValueHolder);
//    }

    private void setGridFilterParameters(ViewDefinitionState view, Entity qICContextEntity) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        FilterValueHolder filterValueHolder = gridComponent.getFilterValue();

        // --- Belongs to entities ---
        Entity companyEntity = qICContextEntity.getBelongsToField(QICContextFields.COMPANY);
        if (companyEntity != null) {
            int companyId = Math.toIntExact(companyEntity.getId());
            filterValueHolder.put(QICCriteriaModifiersCMP.L_COMPANY, companyId);
            System.out.println("Filter COMPANY added: " + companyId);
        }

        Entity productEntity = qICContextEntity.getBelongsToField(QICContextFields.PRODUCT);
        if (productEntity != null) {
            int productId = Math.toIntExact(productEntity.getId());
            filterValueHolder.put(QICCriteriaModifiersCMP.L_PRODUCT, productId);
            System.out.println("Filter PRODUCT added: " + productId);
        }

        Entity toolEntity = qICContextEntity.getBelongsToField(QICContextFields.TOOL);
        if (toolEntity != null) {
            int toolId = Math.toIntExact(toolEntity.getId());
            filterValueHolder.put(QICCriteriaModifiersCMP.L_TOOL, toolId);
            System.out.println("Filter TOOL added: " + toolId);
        }

        // --- String fields ---
        String companyNameField = qICContextEntity.getStringField(QICContextFields.COMPANY_NAME);
        if (companyNameField != null && !companyNameField.trim().isEmpty()) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_COMPANY_NAME, companyNameField);
            System.out.println("Filter COMPANY_NAME added: " + companyNameField);
        }

        String productNameField = qICContextEntity.getStringField(QICContextFields.PRODUCT_NAME);
        if (productNameField != null && !productNameField.trim().isEmpty()) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_PRODUCT_NAME, productNameField);
            System.out.println("Filter PRODUCT_NAME added: " + productNameField);
        }

        String toolNameField = qICContextEntity.getStringField(QICContextFields.TOOL_NAME);
        if (toolNameField != null && !toolNameField.trim().isEmpty()) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_TOOL_NAME, toolNameField);
            System.out.println("Filter TOOL_NAME added: " + toolNameField);
        }

        String inspectionTypeField = qICContextEntity.getStringField(QICContextFields.INSPECTION_TYPE);
        if (inspectionTypeField != null && !inspectionTypeField.trim().isEmpty()) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_INSPECTION_TYPE, inspectionTypeField);
            System.out.println("Filter INSPECTION_TYPE added: " + inspectionTypeField);
        }

        String statusField = qICContextEntity.getStringField(QICContextFields.STATUS);
        if (statusField != null && !statusField.trim().isEmpty()) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_STATUS, statusField);
            System.out.println("Filter STATUS added: " + statusField);
        }

        String productionOrderNumberField = qICContextEntity.getStringField(QICContextFields.PRODUCTION_ORDER_NUMBER);
        if (productionOrderNumberField != null && !productionOrderNumberField.trim().isEmpty()) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_PRODUCTION_ORDER_NUMBER, productionOrderNumberField);
            System.out.println("Filter PRODUCTION_ORDER_NUMBER added: " + productionOrderNumberField);
        }

        String operationNumberField = qICContextEntity.getStringField(QICContextFields.OPERATION_NUMBER);
        if (operationNumberField != null && !operationNumberField.trim().isEmpty()) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_OPERATION_NUMBER, operationNumberField);
            System.out.println("Filter OPERATION_NUMBER added: " + operationNumberField);
        }

        Date dateFromField = qICContextEntity.getDateField(QICContextFields.DATE_FROM);
        if (Objects.nonNull(dateFromField)) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_DATE_FROM, dateFromField.getTime());
            System.out.println("Filter DATE_FROM added: " + dateFromField);
        }

        Date dateToField = qICContextEntity.getDateField(QICContextFields.DATE_TO);
        if (Objects.nonNull(dateToField)) {
            filterValueHolder.put(QICCriteriaModifiersCMP.L_DATE_TO, dateToField.getTime());
            System.out.println("Filter DATE_TO added: " + dateToField);
        }

        gridComponent.setFilterValue(filterValueHolder);
    }


    private void prepareViewWithContext(ViewDefinitionState view, Entity qICContextEntity) {
        setGridFilterParameters(view, qICContextEntity);
    }

    private void prepareViewWithEmptyContext(ViewDefinitionState view, Entity maintenanceEventContext) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        grid.setEntities(Arrays.asList());
    }

    public void beforeRenderListView(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity qICContextEntity = formComponent.getEntity();

        if (qICContextEntity.getBooleanField(QICContextFields.CONFIRMED)) {
            prepareViewWithContext(view, qICContextEntity);
        } else {
            prepareViewWithEmptyContext(view, qICContextEntity);
        }
    }
}
