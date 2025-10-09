package com.fti.qm.listeners.standard;

import com.fti.qm.listeners.standard.base.BaseQualityStandardDetailsListener;
import com.fti.qm.listeners.standard.base.InProcessQSLFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IPQSLDetailsListeners extends BaseQualityStandardDetailsListener<InProcessQSLFields> {
    @Autowired
    public IPQSLDetailsListeners(InProcessQSLFields fields) {
        super(fields);
    }
}

