package com.fti.qm.listeners.standard.base;

import com.fti.qm.constants.outgoingQualityStandard.OutgoingQualityStandardLFields;
import org.springframework.stereotype.Component;

@Component
public class OutgoingQSLFields implements QualityStandardLineFields{
    @Override public String QUALITY_CRITERIA() { return OutgoingQualityStandardLFields.QUALITY_CRITERIA; }
    @Override public String UNIT() { return OutgoingQualityStandardLFields.UNIT; }
    @Override public String QUANTITATIVE_VALUE() { return OutgoingQualityStandardLFields.QUANTITATIVE_VALUE; }
    @Override public String TOLERANCE() { return OutgoingQualityStandardLFields.TOLERANCE; }
    @Override public String UP_VALUE() { return OutgoingQualityStandardLFields.UP_VALUE; }
    @Override public String DOWN_VALUE() { return OutgoingQualityStandardLFields.DOWN_VALUE; }
}
