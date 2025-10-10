package com.fti.qm.listeners.standard;

import com.fti.qm.constants.IncomingQualityStandardLFields;
import com.fti.qm.constants.inProcessQualityStandard.InProcessQualityStandardLFields;
import com.fti.qm.listeners.standard.base.BaseQualityStandardDetailsListener;
import org.springframework.stereotype.Service;

@Service
public class IPQSLDetailsListeners extends BaseQualityStandardDetailsListener {

    @Override public String QUALITY_CRITERIA() { return InProcessQualityStandardLFields.QUALITY_CRITERIA; }
    @Override public String UNIT() { return InProcessQualityStandardLFields.UNIT; }
    @Override public String QUANTITATIVE_VALUE() { return InProcessQualityStandardLFields.QUANTITATIVE_VALUE; }
    @Override public String TOLERANCE() { return InProcessQualityStandardLFields.TOLERANCE; }
    @Override public String UP_VALUE() { return InProcessQualityStandardLFields.UP_VALUE; }
    @Override public String DOWN_VALUE() { return InProcessQualityStandardLFields.DOWN_VALUE; }
}
