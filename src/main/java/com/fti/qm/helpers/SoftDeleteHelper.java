package com.fti.qm.helpers;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.QMConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SoftDeleteHelper {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Cập nhật trạng thái xóa mềm (deleted=true, deletedDate=now)
     * cho tất cả bản ghi con của model liên kết với entity cha.
     *
     * @param modelName tên model con (ví dụ: "incomingQualityStandardL")
     * @param parentModelField tên trường joinField trong model con (ví dụ: "qualityCriteria" hoặc "measuringEquipment")
     * @param parentEntity thực thể cha
     */
    public void softDeleteRelatedLines(final String modelName, final String parentModelField, final Entity parentEntity) {
        DataDefinition lineDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, modelName);

        List<Entity> lines = lineDD.find()
                .add(SearchRestrictions.belongsTo(parentModelField, parentEntity))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .list()
                .getEntities();

        for (Entity line : lines) {
            line.setField(GlobalFields.DELETED, true);
            line.setField(GlobalFields.DELETED_DATE, new Date());
            lineDD.save(line);
        }
    }

    /**
     * Kiểm tra xem entity có đang được tham chiếu ở model con hay không.
     *
     * @param dataDefinition  DataDefinition hiện tại
     * @param entity          Entity đang bị xóa mềm
     * @param childPlugin     Plugin chứa model con
     * @param childModel      Tên model con
     * @param childFieldName  Tên field trên model con tham chiếu đến entity cha
     * @param errorMessageKey Key thông báo lỗi nếu bị chặn
     */
    public void validateSoftDelete(
            DataDefinition dataDefinition,
            Entity entity,
            String childPlugin,
            String childModel,
            String childFieldName,
            String errorMessageKey
    ) {

        Boolean deleted = entity.getBooleanField(GlobalFields.DELETED);
        if (deleted == null || !deleted) {
            return;
        }

        Long id = entity.getId();

        DataDefinition childDD = dataDefinitionService.get(childPlugin, childModel);

        boolean exists = !childDD.find()
                .add(SearchRestrictions.eq(childFieldName, id))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .setMaxResults(1)
                .list()
                .getEntities()
                .isEmpty();

        if (exists) {
            entity.addError(
                    dataDefinition.getField(GlobalFields.DELETED),
                    errorMessageKey
            );
        }
    }
}
