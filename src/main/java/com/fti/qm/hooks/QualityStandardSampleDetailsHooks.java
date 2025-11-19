package com.fti.qm.hooks;

import com.fti.qm.constants.*;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

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
        if (form == null || form.getEntityId() == null)  return;

        // 🔁 Load lại entity từ DB để đảm bảo đầy đủ các belongsTo
        Entity sample = dataDefinitionService
                .get(QMConstants.PLUGIN_IDENTIFIER,  QMConstants.MODEL_QUALITY_STANDARD_SAMPLE)
                .get(form.getEntityId());
        if (sample == null) return;

        // 🔁 Lấy entity cha từ DB
        Entity standardLine = sample.getBelongsToField(QMConstants.MODEL_QUALITY_STANDARD_L);
        if (standardLine == null)  return;

        Locale locale = LocaleContextHolder.getLocale();

        // ✅ Gán giá trị từ standardLine vào form
        setFieldValue(view, QualityStandardSampleViewFields.POSITION, standardLine.getStringField(QSLFields.POSITION));
        setFieldValue(view, QualityStandardSampleViewFields.DESCRIPTION, standardLine.getStringField(QSLFields.DESCRIPTION));
        setFieldValue(view, QualityStandardSampleViewFields.QUANTITATIVE_VALUE, standardLine.getDecimalField(QSLFields.QUANTITATIVE_VALUE));
        setFieldValue(view, QualityStandardSampleViewFields.UP_VALUE, standardLine.getDecimalField(QSLFields.UP_VALUE));
        setFieldValue(view, QualityStandardSampleViewFields.DOWN_VALUE, standardLine.getDecimalField(QSLFields.DOWN_VALUE));
        setFieldValue(view, QualityStandardSampleViewFields.SAMPLE_NUMBER, standardLine.getIntegerField(QSLFields.SAMPLE_SIZE));
        setFieldValue(view, QualityStandardSampleViewFields.UNIT, standardLine.getStringField( QSLFields.UNIT));

        // ✅ Dịch qualitativeValue
        translateEnumField(view, QualityStandardSampleViewFields.QUALITATIVE_VALUE, QUALITATIVE_VALUE_PREFIX, standardLine.getStringField(QSLFields.QUALITATIVE_VALUE), locale);

         // 5️⃣ Lấy thông tin tiêu chí (qualityCriteria)
        Entity criteria = standardLine.getBelongsToField(QMConstants.MODEL_QUALITY_CRITERIA);
        if (criteria != null) {
            setFieldValue(view, QualityStandardSampleViewFields.QC_NAME, criteria.getStringField(QualityCriteriaFields.NUMBER));
            setFieldValue(view, QualityStandardSampleViewFields.QC_NUMBER, criteria.getStringField(QualityCriteriaFields.NAME));
            translateEnumField(view, QualityStandardSampleViewFields.QC_TYPE, QC_TYPE_PREFIX, criteria.getStringField(QualityCriteriaFields.TYPE), locale);
        }

        // 6️⃣ Lấy thông tin thiết bị đo (measuringEquipment)
        Entity equipment = standardLine.getBelongsToField(QMConstants.MODEL_MEASURING_EQUIPMENT);
        if (equipment != null) {
            setFieldValue(view, QualityStandardSampleViewFields.ME_NAME, equipment.getStringField(MeasuringEquipmentFields.NAME));
            setFieldValue(view, QualityStandardSampleViewFields.ME_MEASURING_METHOD, equipment.getStringField(MeasuringEquipmentFields.MEASURING_METHOD));
        }
    }

    // 🧰 Hàm hỗ trợ gán giá trị an toàn cho field
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
}
