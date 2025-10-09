package com.fti.qm.listeners;

import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QualityCriteriaFields;
import com.fti.qm.imports.measuringEquipment.MeasuringEquipmentCellBinderRegistry;
import com.fti.qm.imports.measuringEquipment.MeasuringEquipmentXlsxImportService;
import com.fti.qm.imports.qualityCriteria.QualityCriteriaCellBinderRegistry;
import com.fti.qm.imports.qualityCriteria.QualityCriteriaXlsxImportService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class QualityCriteriasImportListeners {
    @Autowired
    private QualityCriteriaXlsxImportService qualityCriteriaXlsxImportService;

    @Autowired
    private QualityCriteriaCellBinderRegistry qualityCriteriaCellBinderRegistry;

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        qualityCriteriaXlsxImportService.processImportFile(view, qualityCriteriaCellBinderRegistry.getCellBinderRegistry(), true,
                QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_CRITERIA, QualityCriteriasImportListeners::createRestrictionForQualityCriteria);
    }

    private static SearchCriterion createRestrictionForQualityCriteria(final Entity qualityCriteria) {
        return SearchRestrictions.eq(QualityCriteriaFields.NUMBER, qualityCriteria.getStringField(QualityCriteriaFields.NUMBER));
    }

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        qualityCriteriaXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER,
                QMConstants.MODEL_QUALITY_CRITERIA, XlsxImportService.L_XLSX);
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        qualityCriteriaXlsxImportService.redirectToLogs(view, QMConstants.MODEL_QUALITY_CRITERIA);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        qualityCriteriaXlsxImportService.changeButtonsState(view, false);
    }
}
