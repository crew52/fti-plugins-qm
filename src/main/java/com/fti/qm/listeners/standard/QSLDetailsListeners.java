package com.fti.qm.listeners.standard;

import com.fti.qm.constants.QSLFields;
import com.fti.qm.listeners.standard.base.BaseQualityStandardDetailsListener;
import org.springframework.stereotype.Service;

@Service
public class QSLDetailsListeners extends BaseQualityStandardDetailsListener {
    @Override public String QUALITY_CRITERIA() { return QSLFields.QUALITY_CRITERIA; }
    @Override public String UNIT() { return QSLFields.UNIT; }
    @Override public String QUANTITATIVE_VALUE() { return QSLFields.QUANTITATIVE_VALUE; }
    @Override public String TOLERANCE() { return QSLFields.TOLERANCE; }
    @Override public String UP_VALUE() { return QSLFields.UP_VALUE; }
    @Override public String DOWN_VALUE() { return QSLFields.DOWN_VALUE; }
}
