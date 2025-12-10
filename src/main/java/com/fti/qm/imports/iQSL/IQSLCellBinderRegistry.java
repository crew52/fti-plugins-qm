package com.fti.qm.imports.iQSL;

import com.fti.qm.imports.common.AbstractQSLCellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class IQSLCellBinderRegistry extends AbstractQSLCellBinderRegistry {
    @Autowired
    @Qualifier("qshIncomingByProductNumberParser")
    private CellParser qshIncomingLookupByProductNumberParser;

    @Override
    protected CellParser getLookupParser() {
        return qshIncomingLookupByProductNumberParser;
    }
}
