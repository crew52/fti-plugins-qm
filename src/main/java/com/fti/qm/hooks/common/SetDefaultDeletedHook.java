package com.fti.qm.hooks.common;

import com.fti.qm.constants.GlobalFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

/**
 * Hook dùng chung để tự động set giá trị mặc định cho field "deleted".
 * Sử dụng ở mọi model có field "deleted".
 */
@Service
public class SetDefaultDeletedHook {

    /**
     * Đảm bảo field "deleted" luôn được set FALSE nếu chưa có giá trị.
     *
     * @param dataDefinition DataDefinition của model
     * @param entity Entity đang được lưu
     */
    public void onCreate(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField(GlobalFields.DELETED) == null) {
            entity.setField(GlobalFields.DELETED, false);
        }
    }

}

