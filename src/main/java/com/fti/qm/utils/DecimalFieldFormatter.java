package com.fti.qm.utils;

import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component
public class DecimalFieldFormatter {

    /**
     * Chuẩn hóa giá trị decimal trên FieldComponent:
     * - Nếu không hợp lệ: return null
     * - Nếu hợp lệ: parse và format lại → gán lại giá trị mới vào field
     *
     * @param field component có giá trị cần xử lý
     * @param updateState nếu true thì gọi `requestComponentUpdateState`
     * @return BigDecimal đã chuẩn hóa, hoặc null nếu không hợp lệ
     */
    public BigDecimal normalizeDecimalField(FieldComponent field, boolean updateState) {
        String raw = toDotString(field.getFieldValue());

        if (raw == null || raw.isEmpty()) {
            return null;
        }

        try {
            BigDecimal decimal = new BigDecimal(raw);
            String formatted = formatDecimalForLocale(decimal);

            field.setFieldValue(formatted);
            if (updateState) {
                field.requestComponentUpdateState();
            }

            return decimal;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Format giá trị BigDecimal thành chuỗi theo locale hiện tại
     */
    public String formatDecimalForLocale(BigDecimal value) {
        if (value == null) return "";

        Locale locale = LocaleContextHolder.getLocale();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        DecimalFormat df = new DecimalFormat("#0.##", symbols);
        return df.format(value);
    }

    /**
     * Chuyển giá trị nhập thành chuỗi có dấu chấm (.) làm phân cách thập phân
     */
    private String toDotString(Object value) {
        if (value == null) return null;
        return value.toString().replace(',', '.').trim();
    }
}
