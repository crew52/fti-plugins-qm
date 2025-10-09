package com.fti.qm.listeners.standard;

import com.fti.qm.listeners.standard.base.BaseQualityStandardDetailsListener;
import com.fti.qm.listeners.standard.base.IncomingQSLFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IQSLDetailsListeners extends BaseQualityStandardDetailsListener<IncomingQSLFields> {
    @Autowired
    public IQSLDetailsListeners(IncomingQSLFields fields) {
        super(fields);
    }
}
