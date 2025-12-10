package com.fti.qm.imports.parsers;

import com.fti.qm.constants.QSHFields;
import org.springframework.stereotype.Component;

@Component("qshOutgoingByProductNumberParser")
public class QSHOutgoingLookupByProductNumberParser extends AbstractQSHLookupByProductNumberParser{
    @Override
    protected String getType() {
        return QSHFields.Type.OUTGOING;
    }
}
