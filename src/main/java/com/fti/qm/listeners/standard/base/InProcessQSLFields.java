package com.fti.qm.listeners.standard.base;

import com.fti.qm.constants.inProcessQualityStandard.InProcessQualityStandardLFields;
import org.springframework.stereotype.Component;

@Component
public class InProcessQSLFields implements QualityStandardLineFields{
    @Override public String QUALITY_CRITERIA() { return InProcessQualityStandardLFields.QUALITY_CRITERIA; }
    @Override public String UNIT() { return InProcessQualityStandardLFields.UNIT; }
    @Override public String QUANTITATIVE_VALUE() { return InProcessQualityStandardLFields.QUANTITATIVE_VALUE; }
    @Override public String TOLERANCE() { return InProcessQualityStandardLFields.TOLERANCE; }
    @Override public String UP_VALUE() { return InProcessQualityStandardLFields.UP_VALUE; }
    @Override public String DOWN_VALUE() { return InProcessQualityStandardLFields.DOWN_VALUE; }
}
