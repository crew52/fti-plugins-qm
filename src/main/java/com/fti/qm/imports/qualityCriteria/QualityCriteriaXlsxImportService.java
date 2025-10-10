package com.fti.qm.imports.qualityCriteria;

import com.fti.qm.constants.QualityCriteriaFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class QualityCriteriaXlsxImportService extends XlsxImportService {
    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM =
            "qcadooView.validate.field.error.custom";

    @Override
    public void validateEntity(final Entity criteria, final DataDefinition criteriaDD) {
        // Nếu type không null thì phải là 01qualitative hoặc 02quantitative
        Object type = criteria.getField(QualityCriteriaFields.TYPE);

        if (type != null &&
                !(type.equals("01qualitative") || type.equals("02quantitative"))) {
            criteria.addError(criteriaDD.getField(QualityCriteriaFields.TYPE),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }
    }
}
