package com.fti.qm.services;

import java.util.*;

import com.fti.qm.constants.qualityInspectionCommand.QICContextFields;
import com.fti.qm.criteriaModifiers.QICCriteriaModifiersCMP;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import org.apache.commons.lang3.StringUtils;
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

        // --- belongsTo ---
        addBelongsTo(searchCriteriaBuilder, qICContextEntity, QICContextFields.COMPANY);
        addBelongsTo(searchCriteriaBuilder, qICContextEntity, QICContextFields.PRODUCT);
        addBelongsTo(searchCriteriaBuilder, qICContextEntity, QICContextFields.TOOL);

        // --- string ---
        addStringEq(searchCriteriaBuilder, qICContextEntity, QICContextFields.COMPANY_NAME);
        addStringEq(searchCriteriaBuilder, qICContextEntity, QICContextFields.PRODUCT_NAME);
        addStringEq(searchCriteriaBuilder, qICContextEntity, QICContextFields.TOOL_NAME);
        addStringEq(searchCriteriaBuilder, qICContextEntity, QICContextFields.PRODUCTION_ORDER_NUMBER);
        addStringEq(searchCriteriaBuilder, qICContextEntity, QICContextFields.OPERATION_NUMBER);

        // --- enum (string field type) ---
        addStringEq(searchCriteriaBuilder, qICContextEntity, QICContextFields.INSPECTION_TYPE);
        addStringEq(searchCriteriaBuilder, qICContextEntity, QICContextFields.STATUS);

        // --- date ---
        Date dateFrom = qICContextEntity.getDateField(QICContextFields.DATE_FROM);
        Date dateTo = qICContextEntity.getDateField(QICContextFields.DATE_TO);

        if (Objects.nonNull(dateFrom)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.DATE_FROM, dateFrom));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.isNull(QICContextFields.DATE_FROM));
        }
        if (Objects.nonNull(dateTo)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.DATE_TO, dateTo));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.isNull(QICContextFields.DATE_TO));
        }

        // --- find existing ---
        Entity qICContextEntityFromDb = searchCriteriaBuilder.uniqueResult();

        if (qICContextEntityFromDb == null) {
            qICContextEntity.setField(QICContextFields.CONFIRMED, false);
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

    private void addBelongsTo(SearchCriteriaBuilder scb, Entity e, String field) {
        Entity belongs = e.getBelongsToField(field);
        if (Objects.nonNull(belongs)) {
            scb.add(SearchRestrictions.belongsTo(field, belongs));
        } else {
            scb.add(SearchRestrictions.isNull(field));
        }
    }

    private void addStringEq(SearchCriteriaBuilder scb, Entity e, String field) {
        String value = e.getStringField(field);
        if (StringUtils.isNotBlank(value)) {
            scb.add(SearchRestrictions.eq(field, value));
        } else {
            scb.add(SearchRestrictions.isNull(field));
        }
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

    private void setGridFilterParameters(ViewDefinitionState view, Entity qICContextEntity) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        FilterValueHolder filterValueHolder = gridComponent.getFilterValue();

        // --- BelongsTo fields ---
        putBelongsToFilter(filterValueHolder, qICContextEntity, QICContextFields.COMPANY, QICCriteriaModifiersCMP.L_COMPANY);
        putBelongsToFilter(filterValueHolder, qICContextEntity, QICContextFields.PRODUCT, QICCriteriaModifiersCMP.L_PRODUCT);
        putBelongsToFilter(filterValueHolder, qICContextEntity, QICContextFields.TOOL, QICCriteriaModifiersCMP.L_TOOL);

        // --- String fields ---
        putStringFilter(filterValueHolder, qICContextEntity, QICContextFields.COMPANY_NAME, QICCriteriaModifiersCMP.L_COMPANY_NAME);
        putStringFilter(filterValueHolder, qICContextEntity, QICContextFields.PRODUCT_NAME, QICCriteriaModifiersCMP.L_PRODUCT_NAME);
        putStringFilter(filterValueHolder, qICContextEntity, QICContextFields.TOOL_NAME, QICCriteriaModifiersCMP.L_TOOL_NAME);
        putStringFilter(filterValueHolder, qICContextEntity, QICContextFields.INSPECTION_TYPE, QICCriteriaModifiersCMP.L_INSPECTION_TYPE);
        putStringFilter(filterValueHolder, qICContextEntity, QICContextFields.STATUS, QICCriteriaModifiersCMP.L_STATUS);
        putStringFilter(filterValueHolder, qICContextEntity, QICContextFields.PRODUCTION_ORDER_NUMBER, QICCriteriaModifiersCMP.L_PRODUCTION_ORDER_NUMBER);
        putStringFilter(filterValueHolder, qICContextEntity, QICContextFields.OPERATION_NUMBER, QICCriteriaModifiersCMP.L_OPERATION_NUMBER);

        // --- Date fields ---
        putDateFilter(filterValueHolder, qICContextEntity, QICContextFields.DATE_FROM, QICCriteriaModifiersCMP.L_DATE_FROM);
        putDateFilter(filterValueHolder, qICContextEntity, QICContextFields.DATE_TO, QICCriteriaModifiersCMP.L_DATE_TO);

        gridComponent.setFilterValue(filterValueHolder);
    }

    private void putBelongsToFilter(FilterValueHolder filter, Entity context, String contextField, String filterKey) {
        Entity entity = context.getBelongsToField(contextField);
        if (entity != null && entity.getId() != null) {
            int id = Math.toIntExact(entity.getId());
            filter.put(filterKey, id);
        }
    }

    private void putStringFilter(FilterValueHolder filter, Entity context, String contextField, String filterKey) {
        String value = context.getStringField(contextField);
        if (value != null && !value.trim().isEmpty()) {
            filter.put(filterKey, value);
        }
    }

    private void putDateFilter(FilterValueHolder filter, Entity context, String contextField, String filterKey) {
        Date date = context.getDateField(contextField);
        if (Objects.nonNull(date)) {
            filter.put(filterKey, date.getTime());
        }
    }

    private void prepareViewWithContext(ViewDefinitionState view, Entity qICContextEntity) {
        setEnableOfRibbonActions(view, false);
        setEnableOfContextTab(view, false);
        setEnableOfMainTab(view, true);

        setGridFilterParameters(view, qICContextEntity);
    }

    private void prepareViewWithEmptyContext(ViewDefinitionState view, Entity maintenanceEventContext) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        grid.setEntities(Arrays.asList());

        setEnableOfRibbonActions(view, true);
        setEnableOfContextTab(view, true);
        setEnableOfMainTab(view, false);
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

    private void setEnableOfContextTab(final ViewDefinitionState view, final boolean enabled) {
        view.<FieldComponent>tryFindComponentByReference(QICContextFields.COMPANY).orNull().setEnabled(enabled);
        view.<FieldComponent>tryFindComponentByReference(QICContextFields.COMPANY_NAME).orNull().setEnabled(enabled);

        view.<FieldComponent>tryFindComponentByReference(QICContextFields.PRODUCT).orNull().setEnabled(enabled);
        view.<FieldComponent>tryFindComponentByReference(QICContextFields.PRODUCT_NAME).orNull().setEnabled(enabled);

        view.<FieldComponent>tryFindComponentByReference(QICContextFields.TOOL).orNull().setEnabled(enabled);
        view.<FieldComponent>tryFindComponentByReference(QICContextFields.TOOL_NAME).orNull().setEnabled(enabled);

        view.<FieldComponent>tryFindComponentByReference(QICContextFields.INSPECTION_TYPE).orNull().setEnabled(enabled);
        view.<FieldComponent>tryFindComponentByReference(QICContextFields.STATUS).orNull().setEnabled(enabled);
        view.<FieldComponent>tryFindComponentByReference(QICContextFields.PRODUCTION_ORDER_NUMBER).orNull().setEnabled(enabled);
        view.<FieldComponent>tryFindComponentByReference(QICContextFields.OPERATION_NUMBER).orNull().setEnabled(enabled);

        view.<FieldComponent>tryFindComponentByReference(QICContextFields.DATE_FROM).orNull().setEnabled(enabled);
        view.<FieldComponent>tryFindComponentByReference(QICContextFields.DATE_TO).orNull().setEnabled(enabled);
    }

    private void setEnableOfMainTab(ViewDefinitionState view, boolean enabled) {
        view.getComponentByReference(QcadooViewConstants.L_GRID).setEnabled(enabled);
    }

    private void setEnableOfRibbonActions(ViewDefinitionState viewDefinitionState, boolean enabled) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup customActions = ribbon.getGroupByName("customActions");

        if (customActions == null) {
            return;
        }

        for (RibbonActionItem ribbonActionItem : customActions.getItems()) {
            ribbonActionItem.setEnabled(enabled);
            ribbonActionItem.requestUpdate(true);
        }
    }

    public void resetContext(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        String[] fieldRefs = {
                QICContextFields.INSPECTION_TYPE,
                QICContextFields.DATE_FROM,
                QICContextFields.DATE_TO,
                QICContextFields.STATUS,
                QICContextFields.PRODUCTION_ORDER_NUMBER,
                QICContextFields.OPERATION_NUMBER,
                QICContextFields.COMPANY,
                QICContextFields.COMPANY_NAME,
                QICContextFields.PRODUCT,
                QICContextFields.PRODUCT_NAME,
                QICContextFields.TOOL,
                QICContextFields.TOOL_NAME
        };

        for (String ref : fieldRefs) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(ref);
            if (field != null) {
                field.setFieldValue(null);
                field.requestComponentUpdateState();
            }
        }

        view.addMessage("qm.qualityInspectionCommandList.reset.success", ComponentState.MessageType.SUCCESS);
    }

}
