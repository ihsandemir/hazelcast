/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.client.cluster;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.impl.clientside.ClientTestUtil;
import com.hazelcast.client.spi.ClientClusterService;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Set;

import static com.hazelcast.cluster.memberselector.MemberSelectors.DATA_MEMBER_SELECTOR;
import static com.hazelcast.cluster.memberselector.MemberSelectors.LITE_MEMBER_SELECTOR;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class ClientClusterServiceMemberListTest extends HazelcastTestSupport {

    @Test
    public void testUnlimited() {
        HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient(null);

        generateTraffic(hazelcastClient);
    }

    @Test
    public void testLimited() {
        ClientConfig config = new ClientConfig();
        config.getGroupConfig().setName("rateLimitedCluster");
        HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient(config);

        generateTraffic(hazelcastClient);
    }

    private void generateTraffic(HazelcastInstance hazelcastClient) {
        IMap<Integer, byte[]> map = hazelcastClient.getMap("unlimited");

        byte[] data = new byte[10 * 1024];
        map.put(1, data);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            map.get(1);
        }
        long end = System.currentTimeMillis();
        System.out.println("Test Lasted in " + (end - start) + " msecs");
        map.destroy();
    }

}
