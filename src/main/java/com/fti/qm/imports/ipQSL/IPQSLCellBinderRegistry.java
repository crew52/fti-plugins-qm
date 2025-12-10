package com.fti.qm.imports.ipQSL;

import com.fti.qm.imports.common.AbstractQSLCellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class IPQSLCellBinderRegistry extends AbstractQSLCellBinderRegistry {
    @Autowired
    @Qualifier("qshInProcessByProductNumberParser")
    private CellParser qshInProcessLookupByProductNumberParser;

    @Override
    protected CellParser getLookupParser() {
        return qshInProcessLookupByProductNumberParser;
    }
}
