package com.fti.qm.utils;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

public final class RibbonUtils {

    private RibbonUtils() {
        // private constructor to prevent instantiation
    }

    public static final String GROUP_NAVIGATION = "navigation";
    public static final String GROUP_ATTACHMENTS = "attachments";

    /**
     * Vô hiệu hóa tất cả các action trong ribbon trừ nhóm "navigation".
     *
     * @param view ViewDefinitionState hiện tại
     */
    public static void disableActionsExceptNavigation(final ViewDefinitionState view) {
        if (view == null) return;

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        if (window == null) return;

        Ribbon ribbon = window.getRibbon();
        if (ribbon == null) return;

        for (RibbonGroup group : ribbon.getGroups()) {
            if (GROUP_NAVIGATION.equalsIgnoreCase(group.getName()) || GROUP_ATTACHMENTS.equalsIgnoreCase(group.getName())) {
                continue;
            }

            for (RibbonActionItem item : group.getItems()) {
                item.setEnabled(false);
                item.requestUpdate(true);
            }
        }
    }
}