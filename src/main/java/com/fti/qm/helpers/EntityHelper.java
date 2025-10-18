package com.fti.qm.helpers;

import com.fti.qm.constants.GlobalFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper chứa các hàm tiện ích thao tác với Entity,
 * ví dụ lấy danh sách ID của các bản ghi chưa bị xóa mềm.
 */
@Component
public class EntityHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Trả về danh sách ID của tất cả entity trong model chỉ định
     * mà có trường {@code deleted = false}.
     *
     * @param pluginIdentifier plugin chứa model (ví dụ: "qm", "basic", "rew52")
     * @param modelName        tên model trong plugin (ví dụ: "qualityCriteria")
     * @return danh sách ID chưa bị xóa mềm
     */
    public List<Long> getNonDeletedIds(final String pluginIdentifier, final String modelName) {
        return dataDefinitionService
                .get(pluginIdentifier, modelName)
                .find()
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .list()
                .getEntities()
                .stream()
                .map(Entity::getId)
                .collect(Collectors.toList());
    }
}