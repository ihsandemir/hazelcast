package com.hazelcast.internal.quota;

import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.Operation;



public class QuotaCheckOperation
        extends Operation {

    private final NodeEngine nodeEngine;

    public QuotaCheckOperation(NodeEngine nodeEngine) {
        this.nodeEngine = nodeEngine;
    }

    @Override
    public boolean returnsResponse() {
        return true;
    }

    @Override
    public Object getResponse() {
        return nodeEngine.getSentBytes();
    }



}
