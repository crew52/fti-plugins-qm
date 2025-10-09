package com.fti.qm.listeners.standard;

import com.fti.qm.constants.outgoingQualityStandard.OutgoingQualityStandardLFields;
import com.fti.qm.listeners.standard.base.BaseQualityStandardDetailsListener;
import org.springframework.stereotype.Service;

@Service
public class OQSLDetailsListeners extends BaseQualityStandardDetailsListener {

    @Override public String QUALITY_CRITERIA() { return OutgoingQualityStandardLFields.QUALITY_CRITERIA; }
    @Override public String UNIT() { return OutgoingQualityStandardLFields.UNIT; }
    @Override public String QUANTITATIVE_VALUE() { return OutgoingQualityStandardLFields.QUANTITATIVE_VALUE; }
    @Override public String TOLERANCE() { return OutgoingQualityStandardLFields.TOLERANCE; }
    @Override public String UP_VALUE() { return OutgoingQualityStandardLFields.UP_VALUE; }
    @Override public String DOWN_VALUE() { return OutgoingQualityStandardLFields.DOWN_VALUE; }
}
