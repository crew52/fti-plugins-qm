package com.fti.qm.hooks;

import com.fti.qm.constants.*;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class QualityStandardSampleDetailsHooks {

    private static final String QUALITATIVE_VALUE_PREFIX = "qm.qualityStandardL.qualitativeValue.value.";
    private static final String QC_TYPE_PREFIX = "qm.qualityCriteria.type.value.";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public void beforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form == null || form.getEntityId() == null) return;

        // 1️⃣ Load entity sample từ DB
        Entity sample = loadSampleEntity(form);
        if (sample == null) return;

        // 2️⃣ Lấy standardLine và gán giá trị vào form
        Entity standardLine = sample.getBelongsToField(QMConstants.MODEL_QUALITY_STANDARD_L);
        if (standardLine != null) {
            fillFieldsFromStandardLine(view, standardLine);
        }

        // 3️⃣ Disable các field nếu QIC status = COMPLETED
        disableFieldsIfCompleted(view);
    }

    private Entity loadSampleEntity(FormComponent form) {
        return dataDefinitionService
                .get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE)
                .get(form.getEntityId());
    }

    private void fillFieldsFromStandardLine(ViewDefinitionState view, Entity standardLine) {
        Locale locale = LocaleContextHolder.getLocale();

        setFieldValue(view, QualityStandardSampleViewFields.POSITION, standardLine.getStringField(QSLFields.POSITION));
        setFieldValue(view, QualityStandardSampleViewFields.DESCRIPTION, standardLine.getStringField(QSLFields.DESCRIPTION));
        setFieldValue(view, QualityStandardSampleViewFields.QUANTITATIVE_VALUE, standardLine.getDecimalField(QSLFields.QUANTITATIVE_VALUE));
        setFieldValue(view, QualityStandardSampleViewFields.UP_VALUE, standardLine.getDecimalField(QSLFields.UP_VALUE));
        setFieldValue(view, QualityStandardSampleViewFields.DOWN_VALUE, standardLine.getDecimalField(QSLFields.DOWN_VALUE));
        setFieldValue(view, QualityStandardSampleViewFields.SAMPLE_SIZE, standardLine.getIntegerField(QSLFields.SAMPLE_SIZE));
        setFieldValue(view, QualityStandardSampleViewFields.UNIT, standardLine.getStringField(QSLFields.UNIT));

        translateEnumField(view, QualityStandardSampleViewFields.QUALITATIVE_VALUE, QUALITATIVE_VALUE_PREFIX,
                standardLine.getStringField(QSLFields.QUALITATIVE_VALUE), locale);

        Entity criteria = standardLine.getBelongsToField(QMConstants.MODEL_QUALITY_CRITERIA);
        if (criteria != null) {
            setFieldValue(view, QualityStandardSampleViewFields.QC_NAME, criteria.getStringField(QualityCriteriaFields.NUMBER));
            setFieldValue(view, QualityStandardSampleViewFields.QC_NUMBER, criteria.getStringField(QualityCriteriaFields.NAME));
            translateEnumField(view, QualityStandardSampleViewFields.QC_TYPE, QC_TYPE_PREFIX, criteria.getStringField(QualityCriteriaFields.TYPE), locale);
        }

        Entity equipment = standardLine.getBelongsToField(QMConstants.MODEL_MEASURING_EQUIPMENT);
        if (equipment != null) {
            setFieldValue(view, QualityStandardSampleViewFields.ME_NAME, equipment.getStringField(MeasuringEquipmentFields.NAME));
            setFieldValue(view, QualityStandardSampleViewFields.ME_MEASURING_METHOD, equipment.getStringField(MeasuringEquipmentFields.MEASURING_METHOD));
        }
    }

    private void setFieldValue(final ViewDefinitionState view, final String reference, final Object value) {
        FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
        if (field != null && value != null) {
            field.setFieldValue(value.toString());
            field.requestComponentUpdateState();
        }
    }

    private void translateEnumField(final ViewDefinitionState view, final String reference,
                                    final String translationPrefix, final String enumValue, final Locale locale) {
        if (enumValue == null) return;
        String translatedValue = translationService.translate(translationPrefix + enumValue, locale);
        setFieldValue(view, reference, translatedValue);
    }

    /**
     * Disable các field nếu status của QIC = COMPLETED
     */
    public void disableFieldsIfCompleted(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form == null || form.getEntity() == null) return;

        Entity sample = form.getEntity();
        Entity qic = sample.getBelongsToField(QualityStandardSampleFields.QUALITY_INSPECTION_COMMAND);
        if (qic == null) return;

        String status = (String) qic.getField(QICFields.STATUS);
        if (!QICFields.Status.COMPLETED.equals(status)) return;

        List<String> fieldsToDisable = Arrays.asList(
                QualityStandardSampleViewFields.QUALITATIVE_RESULT,
                QualityStandardSampleViewFields.QUANTITATIVE_RESULT,
                QualityStandardSampleViewFields.QUANTITATIVE_EVALUATION
        );

        for (String fieldRef : fieldsToDisable) {
            ComponentState comp = view.getComponentByReference(fieldRef);
            if (comp != null) {
                comp.setEnabled(false);
            }
        }
    }
}
