package com.fti.qm.listeners.standard;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QualityStandardHVersionListener {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Listener được gọi từ nút "Tạo phiên bản mới".
     * Ở bước đầu tiên: kiểm tra xem QIC cho product + 01incoming có tồn tại hay không.
     */
    public void createVersion(final ViewDefinitionState view, final ComponentState button, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity qsh = form.getEntity();

        if (qsh == null || qsh.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noRecord", ComponentState.MessageType.FAILURE);
            return;
        }

        // Lấy product từ QualityStandardH
        Entity product = qsh.getBelongsToField("product");
        if (product == null || product.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noProduct", ComponentState.MessageType.FAILURE);
            return;
        }

        boolean exists = qicExistsForProductIncoming(product.getId());

        if (!exists) {
            // TH1: Không tìm thấy QIC cho product + 01incoming
            view.addMessage("qm.qualityStandardH.info.qicNotFound", ComponentState.MessageType.INFO);
            return;
        } else {
            // TH2: Tìm thấy QIC
            view.addMessage("qm.qualityStandardH.info.qicFound", ComponentState.MessageType.SUCCESS);

            // Bạn sẽ triển khai bước tiếp theo ở đây sau
            // qualityStandardVersionService.createNewVersion(qsh);
        }
    }

    /**
     * Kiểm tra có QIC nào cho productId với inspectionType = 01incoming hay không.
     */
    private boolean qicExistsForProductIncoming(final Long productId) {
        if (productId == null) return false;

        DataDefinition qicDD = dataDefinitionService.get("qm", "qualityInspectionCommandRe");
        if (qicDD == null) return false;

        List<?> qics = qicDD.find()
                .add(SearchRestrictions.eq("product.id", productId))
                .add(SearchRestrictions.eq(QICFields.INSPECTION_TYPE, QICFields.INSPECTION_TYPE_INCOMING))
                .setMaxResults(1)
                .list()
                .getEntities();

        return qics != null && !qics.isEmpty();
    }
}
