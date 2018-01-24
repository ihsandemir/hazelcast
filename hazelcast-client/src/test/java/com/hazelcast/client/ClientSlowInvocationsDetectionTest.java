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

package com.hazelcast.client;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.SlowOperationDetectorConfig;
import com.hazelcast.client.impl.protocol.codec.MapMessageType;
import com.hazelcast.client.spi.impl.ClientInvocation;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class ClientSlowInvocationsDetectionTest extends HazelcastTestSupport {

    private final TestHazelcastFactory hazelcastFactory = new TestHazelcastFactory();

    @Before
    public void setUp() {
        Config config = new Config();
        config.getManagementCenterConfig().setEnabled(true).setUrl("http://127.0.0.1:8083/mancenter");
        hazelcastFactory.newInstances(config, 1);
    }

    @After
    public void cleanup() {
        hazelcastFactory.terminateAll();
    }

    @Test
    public void testSlowGetInvocation() {
        final ClientConfig clientConfig = new ClientConfig();
        SlowOperationDetectorConfig slowOperationDetectorConfig = clientConfig.getSlowOperationDetectorConfig();
        slowOperationDetectorConfig.setEnabled(true);
        slowOperationDetectorConfig.setCheckPeriodInMillis(1000);
        slowOperationDetectorConfig.setTimeoutInMillis(MapMessageType.MAP_GET.id(), 1000);
        slowOperationDetectorConfig.setWarningPrintIntervalMillis(5000);
        slowOperationDetectorConfig.setUserNotifier(new SlowOperationDetectorConfig.UserNotifier() {
            @Override
            public void notifySlowInvocation(ClientInvocation clientInvocation, long configuredTimeoutInMillis, long currentInvocationTimeInMillis) {
                ILogger logger = Logger.getLogger(getClass());
                logger.info("Oh my god!!! My invocations for (" + clientInvocation.getClientMessage().getOperationName() +
                        ") are slowing down, what should I do? Cancel the invocation?");
            }
        });

        HazelcastInstance client = hazelcastFactory.newHazelcastClient(clientConfig);

        IMap<Integer, Integer> map = client.getMap("testSlowGetInvocationMap");

        Random random = new Random();
        while (true) {
            int key = random.nextInt();
            map.put(key, key);
            map.get(key);
        }
    }
}
