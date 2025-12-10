package com.fti.qm.imports.parsers;

import com.fti.qm.constants.QSHFields;
import org.springframework.stereotype.Component;

@Component("qshIncomingByProductNumberParser")
public class QSHIncomingLookupByProductNumberParser extends AbstractQSHLookupByProductNumberParser{
    @Override
    protected String getType() {
        return QSHFields.Type.INCOMING;
    }
}
