package com.fti.qm.imports.measuringEquipment;

import com.fti.qm.constants.MeasuringEquipmentFields;
import org.springframework.stereotype.Component;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;
import javax.annotation.PostConstruct;

@Component
public class MeasuringEquipmentCellBinderRegistry {
    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(MeasuringEquipmentFields.NAME));
        cellBinderRegistry.setCellBinder(required(MeasuringEquipmentFields.MEASURING_METHOD));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }
}
