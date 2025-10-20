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
}
