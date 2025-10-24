package com.fti.qm.criteriaModifiers;

import com.fti.qm.constants.qualityInspectionCommand.QICContextFields;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class QICCriteriaModifiersCMP {
    public static final String L_COMPANY = "company";
    public static final String L_COMPANY_NAME = "companyName";
    public static final String L_PRODUCT = "product";
    public static final String L_PRODUCT_NAME = "productName";
    public static final String L_TOOL = "tool";
    public static final String L_TOOL_NAME = "toolName";

    public static final String L_INSPECTION_TYPE = "inspectionType";
    public static final String L_STATUS = "status";
    public static final String L_PRODUCTION_ORDER_NUMBER = "productionOrderNumber";
    public static final String L_OPERATION_NUMBER = "operationNumber";
    public static final String L_DATE_FROM = "fromDate";
    public static final String L_DATE_TO = "toDate";

    public void showQICFromContext(final SearchCriteriaBuilder searchCriteriaBuilder,
                                             final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_COMPANY)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.COMPANY + "_id",
                    filterValueHolder.getInteger(L_COMPANY)));
        }

        if (filterValueHolder.has(L_PRODUCT)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.PRODUCT + "_id",
                    filterValueHolder.getInteger(L_PRODUCT)));
        }

        if (filterValueHolder.has(L_TOOL)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.TOOL + "_id",
                    filterValueHolder.getInteger(L_TOOL)));
        }

        // Filter by names (join on related entity)
        if (filterValueHolder.has(L_COMPANY_NAME)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.COMPANY + "Name",
                    filterValueHolder.getString(L_COMPANY_NAME)));
        }
        if (filterValueHolder.has(L_PRODUCT_NAME)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.PRODUCT + "Name",
                    filterValueHolder.getString(L_PRODUCT_NAME)));
        }
        if (filterValueHolder.has(L_TOOL_NAME)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICContextFields.TOOL + "Name",
                    filterValueHolder.getString(L_TOOL_NAME)));
        }

        // Filter by enum fields
        if (filterValueHolder.has(L_INSPECTION_TYPE)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICFields.INSPECTION_TYPE,
                    filterValueHolder.getString(L_INSPECTION_TYPE)));
        }
        if (filterValueHolder.has(L_STATUS)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICFields.STATUS,
                    filterValueHolder.getString(L_STATUS)));
        }

        // Filter by production order and operation number
        if (filterValueHolder.has(L_PRODUCTION_ORDER_NUMBER)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICFields.PRODUCTION_ORDER_NUMBER,
                    filterValueHolder.getString(L_PRODUCTION_ORDER_NUMBER)));
        }
        if (filterValueHolder.has(L_OPERATION_NUMBER)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(QICFields.OPERATION_NUMBER,
                    filterValueHolder.getString(L_OPERATION_NUMBER)));
        }

        if (filterValueHolder.has(L_DATE_FROM)) {
            searchCriteriaBuilder.add(SearchRestrictions.ge(QICFields.EXECUTION_DATE,
                    new Date(filterValueHolder.getLong(L_DATE_FROM))));
        }

        if (filterValueHolder.has(L_DATE_TO)) {
            searchCriteriaBuilder.add(SearchRestrictions.le(QICFields.EXECUTION_DATE,
                    new Date(filterValueHolder.getLong(L_DATE_TO))));
        }

    }
}
