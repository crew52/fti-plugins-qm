package com.fti.qm.imports.oQSL;

import com.fti.qm.imports.common.AbstractQSLCellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OQSLCellBinderRegistry extends AbstractQSLCellBinderRegistry {
    @Autowired
    @Qualifier("qshOutgoingByProductNumberParser")
    private CellParser qshOutgoingLookupByProductNumberParser;

    @Override
    protected CellParser getLookupParser() {
        return qshOutgoingLookupByProductNumberParser;
    }
}
