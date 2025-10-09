package com.fti.qm.listeners.standard.base;

import com.fti.qm.constants.IncomingQualityStandardLFields;
import org.springframework.stereotype.Component;

@Component
public class IncomingQSLFields implements QualityStandardLineFields {
    @Override public String QUALITY_CRITERIA() { return IncomingQualityStandardLFields.QUALITY_CRITERIA; }
    @Override public String UNIT() { return IncomingQualityStandardLFields.UNIT; }
    @Override public String QUANTITATIVE_VALUE() { return IncomingQualityStandardLFields.QUANTITATIVE_VALUE; }
    @Override public String TOLERANCE() { return IncomingQualityStandardLFields.TOLERANCE; }
    @Override public String UP_VALUE() { return IncomingQualityStandardLFields.UP_VALUE; }
    @Override public String DOWN_VALUE() { return IncomingQualityStandardLFields.DOWN_VALUE; }
}
