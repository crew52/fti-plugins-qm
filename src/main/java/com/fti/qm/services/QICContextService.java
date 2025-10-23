package com.fti.qm.services;

import java.util.*;

import com.fti.qm.constants.qualityInspectionCommand.QICContextFields;
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
}
