package com.fti.qm.listeners.standard;

import com.fti.qm.listeners.standard.base.BaseQualityStandardDetailsListener;
import com.fti.qm.listeners.standard.base.OutgoingQSLFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OQSLDetailsListeners extends BaseQualityStandardDetailsListener<OutgoingQSLFields> {
    @Autowired
    public OQSLDetailsListeners(OutgoingQSLFields fields) {
        super(fields);
    }
}
