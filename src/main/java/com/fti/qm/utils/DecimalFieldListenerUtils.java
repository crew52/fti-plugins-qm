package com.fti.qm.utils;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DecimalFieldListenerUtils {

    @Autowired
    private DecimalFieldFormatter decimalFieldFormatter;

    /**
     * Gọi trong bất kỳ listener nào gắn với field kiểu decimal.
     * - Nếu field hợp lệ → tự động chuyển định dạng
     * - Nếu không hợp lệ → không làm gì
     *
     * @param view view hiện tại
     * @param state component kích hoạt sự kiện
     */
    public void handleDecimalInput(ViewDefinitionState view, ComponentState state) {
        if (state instanceof FieldComponent) {
            FieldComponent field = (FieldComponent) state;
            decimalFieldFormatter.normalizeDecimalField(field, true);
        }
    }
}