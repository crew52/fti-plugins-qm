package com.fti.qm.hooks.standard;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QSHFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardHDetailsHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityStandardHDetailsHooks extends BaseQualityStandardHDetailsHooks {
    @Autowired
    private DataDefinitionService dataDefinitionService;
    public void beforeRender(final ViewDefinitionState view) {
        setQSHIdForMultiUploadField(view);
        setDefaultTypeValueIncoming(view);
        setGenerateNewVersionEnabled(view);
    }

    public void setQSHIdForMultiUploadField(final ViewDefinitionState view) {
        setMultiUploadContext(view, "qSHIdForMultiUpload", "qSHMultiUploadLocale");
    }

    public void setDefaultTypeValueIncoming(final ViewDefinitionState view) {
        setDefaultTypeValue(view, QSHFields.Type.INCOMING);
    }

    public void setDefaultTypeValueInprocess(final ViewDefinitionState view) {
        setDefaultTypeValue(view, QSHFields.Type.INPROCESS);
    }

    public void setDefaultTypeValueOutgoing(final ViewDefinitionState view) {
        setDefaultTypeValue(view, QSHFields.Type.OUTGOING);
    }

    public void setDefaultTypeValueEquipment(final ViewDefinitionState view) {
        setDefaultTypeValue(view, QSHFields.Type.EQUIPMENT);
    }

    private void setGenerateNewVersionEnabled(final ViewDefinitionState view) {
        try {
            boolean enabled = isQualityStandardActive(view);
            updateGenerateNewVersionButton(view, enabled);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isQualityStandardActive(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity qsh = form != null ? form.getEntity() : null;

        if (qsh == null || qsh.getId() == null) {
            return false;
        }

        DataDefinition qshDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);

        Entity qshFromDB = qshDD.find()
                .add(SearchRestrictions.idEq(qsh.getId()))
                .setMaxResults(1)
                .uniqueResult();

        return qshFromDB != null && qshFromDB.isActive();
    }

    private void updateGenerateNewVersionButton(ViewDefinitionState view, boolean enabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        if (window == null) return;

        Ribbon ribbon = window.getRibbon();
        if (ribbon == null) return;

        RibbonGroup customActions = ribbon.getGroupByName("customActions");
        if (customActions == null) return;

        RibbonActionItem generateBtn = customActions.getItemByName("generateNewVersion");
        if (generateBtn != null) {
            generateBtn.setEnabled(enabled);
            generateBtn.requestUpdate(true);
        }
    }
}
