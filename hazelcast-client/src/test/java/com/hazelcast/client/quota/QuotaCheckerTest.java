package com.hazelcast.client.quota;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class QuotaCheckerTest extends HazelcastTestSupport {

    @Test
    public void checkQuota() {
        int quotaInBytes = 100;
        Config config = new Config();
        config.setClientQuotaInBytes(quotaInBytes);
        Hazelcast.newHazelcastInstance(config);
        IMap<Integer, byte[]> map = HazelcastClient.newHazelcastClient().getMap("quotaMap");
        map.put(1, new byte[quotaInBytes]);

        // let it exceed the quota
        map.get(1);

        sleepSeconds(10);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            map.get(1);
        }
        long end = System.currentTimeMillis();
        assertGreaterOrEquals("Should last longer than 3 seconds", end - start, 3 * 1000);
    }

    @Test
    public void checkNoQuota() {
        int quotaInBytes = 100;
        Hazelcast.newHazelcastInstance();
        IMap<Integer, byte[]> map = HazelcastClient.newHazelcastClient().getMap("quotaMap");
        map.put(1, new byte[quotaInBytes]);

        // let it exceed the quota
        map.get(1);

        sleepSeconds(10);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            map.get(1);
        }
        long end = System.currentTimeMillis();
        assertTrue(end - start < 3 * 1000);
    }
}
