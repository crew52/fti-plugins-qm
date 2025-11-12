package com.fti.qm.listeners.standard;

import com.fti.qm.listeners.standard.base.BaseQualityStandardDetailsListener;
import org.springframework.stereotype.Service;

@Service
public class QSLDetailsListeners extends BaseQualityStandardDetailsListener {
    @Override public String QUALITY_CRITERIA() { return "qualityCriteria"; }
    @Override public String UNIT() { return "unit"; }
    @Override public String QUANTITATIVE_VALUE() { return "quantitativeValue"; }
    @Override public String TOLERANCE() { return "tolerance"; }
    @Override public String UP_VALUE() { return "upValue"; }
    @Override public String DOWN_VALUE() { return "downValue"; }
}
