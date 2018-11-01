package com.hazelcast.internal.quota;

import com.hazelcast.core.Member;
import com.hazelcast.internal.cluster.impl.ClusterServiceImpl;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.OperationService;
import com.hazelcast.util.ExceptionUtil;
import com.hazelcast.util.function.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class QuotaChecker implements Runnable {

    public final static String NAME = "QuotaChecker";
    private final NodeEngine nodeEngine;
    private final long clientQuotaInBytes;

    public QuotaChecker(NodeEngine nodeEngine) {
        this.nodeEngine = nodeEngine;
        this.clientQuotaInBytes = nodeEngine.getConfig().getClientQuotaInBytes();
    }

    private class QuotaCheckerSupplier implements Supplier<Operation> {
        @Override
        public Operation get() {
            return new QuotaCheckOperation(nodeEngine);
        }
    }

    @Override
    public void run() {
        if (clientQuotaInBytes <= 0) {
            return;
        }

        OperationService os = nodeEngine.getOperationService();
        try {
/*
            Set<Member> members = nodeEngine.getClusterService().getMembers();
            List<Future<Object> > futures = new ArrayList<Future<Object>>(members.size());
            for (Member member : members) {
                if (!member.localMember()) {
                    futures.add(os.invokeOnTarget(ClusterServiceImpl.SERVICE_NAME, new QuotaCheckOperation(nodeEngine), member.getAddress()));
                } else {
                    os.execute(new QuotaCheckOperation(nodeEngine));
                }
            }
*/

            long totalSentBytesInCluster = nodeEngine.getSentBytes();
/*
            for (Future<Object> future : futures) {
                totalSentBytesInCluster += (Long) future.get();
            }
*/

            if (!nodeEngine.getQuotaExceeded() && totalSentBytesInCluster > clientQuotaInBytes) {
                nodeEngine.setQuotaExceeded(true);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.rethrow(t);
        }

    }


}
