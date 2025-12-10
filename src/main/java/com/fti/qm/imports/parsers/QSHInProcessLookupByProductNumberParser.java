package com.fti.qm.imports.parsers;

import com.fti.qm.constants.QSHFields;
import com.fti.qm.imports.common.AbstractQSHLookupByProductNumberParser;
import org.springframework.stereotype.Component;

@Component("qshInProcessByProductNumberParser")
public class QSHInProcessLookupByProductNumberParser extends AbstractQSHLookupByProductNumberParser {
    @Override
    protected String getType() {
        return QSHFields.Type.INPROCESS;
    }
}
