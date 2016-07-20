/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
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
import com.hazelcast.client.config.ClientProperty;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.client.util.ClientDelegatingFuture;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastOverloadException;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class ClientMaxAllowedInvocationTest extends HazelcastTestSupport {

    private final TestHazelcastFactory hazelcastFactory = new TestHazelcastFactory();

    @After
    public void cleanup() {
        hazelcastFactory.terminateAll();
    }


    @Test(expected = HazelcastOverloadException.class)
    public void testMaxAllowed_withSyncOperation() {
        int MAX_ALLOWED = 10;
        hazelcastFactory.newHazelcastInstance();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setProperty(ClientProperty.MAX_CONCURRENT_INVOCATIONS.getName(), String.valueOf(MAX_ALLOWED));
        HazelcastInstance client = hazelcastFactory.newHazelcastClient(clientConfig);
        IMap map = client.getMap(randomString());

        IExecutorService executorService = client.getExecutorService(randomString());

        for (int i = 0; i < MAX_ALLOWED; i++) {
            executorService.submit(new SleepyProcessor(Integer.MAX_VALUE));
        }

        map.get(2);
    }

    @Test(expected = HazelcastOverloadException.class)
    public void testMaxAllowed_withAsyncOperation() {
        int MAX_ALLOWED = 10;
        hazelcastFactory.newHazelcastInstance();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setProperty(ClientProperty.MAX_CONCURRENT_INVOCATIONS.getName(), String.valueOf(MAX_ALLOWED));
        HazelcastInstance client = hazelcastFactory.newHazelcastClient(clientConfig);
        IMap map = client.getMap(randomString());

        IExecutorService executorService = client.getExecutorService(randomString());

        for (int i = 0; i < MAX_ALLOWED; i++) {
            executorService.submit(new SleepyProcessor(Integer.MAX_VALUE));
        }

        map.getAsync(1);
    }

    @Test
    public void testMaxAllowed_withAsyncGetSlowCallback() {
        final int MAX_ALLOWED = 3;
        hazelcastFactory.newHazelcastInstance();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setProperty(ClientProperty.MAX_CONCURRENT_INVOCATIONS.getName(), String.valueOf(MAX_ALLOWED));
        HazelcastInstance client = hazelcastFactory.newHazelcastClient(clientConfig);
        IMap map = client.getMap(randomString());

        final AtomicInteger numberOfOutStandingCallbacks = new AtomicInteger(0);
        final CountDownLatch waitLatch = new CountDownLatch(1);

        fillAllInvocations(MAX_ALLOWED, map, numberOfOutStandingCallbacks, waitLatch);

        // Do one more invocation to overflow
        getAsynch(map, numberOfOutStandingCallbacks, waitLatch);

        try {
            assertTrueAllTheTime(new AssertTask() {
                @Override
                public void run()
                        throws Exception {
                    int numberOfCalls = numberOfOutStandingCallbacks.get();
                    Assert.assertTrue(
                            "Outstanding number of calls:" + numberOfCalls + "  should be less than max allowed:" + MAX_ALLOWED,
                            numberOfCalls < MAX_ALLOWED);
                }
            }, 3);
        } catch (AssertionError error) {
            waitLatch.countDown();
            throw error;
        }

        try {
            assertTrueEventually(new AssertTask() {
                @Override
                public void run()
                        throws Exception {
                    Assert.assertEquals(0, numberOfOutStandingCallbacks.get());
                }
            }, 5);
        } finally {
            waitLatch.countDown();
        }
    }

    private void fillAllInvocations(final int MAX_ALLOWED, IMap map, final AtomicInteger numberOfOutStandingCallbacks,
                                    CountDownLatch waitLatch) {
        for (int i = 0; i < MAX_ALLOWED - 1; i++) {
            getAsynch(map, numberOfOutStandingCallbacks, waitLatch);
        }

        assertTrueEventually(new AssertTask() {
            @Override
            public void run()
                    throws Exception {
                int numberOfCalls = numberOfOutStandingCallbacks.get();
                Assert.assertEquals(MAX_ALLOWED - 1, numberOfCalls);
            }
        }, 5);
    }

    private void getAsynch(IMap map, final AtomicInteger numberOfOutStandingCallbacks, final CountDownLatch waitLatch) {
        int outstandingCallbacks = numberOfOutStandingCallbacks.get();
        ((ClientDelegatingFuture)map.getAsync(randomString())).andThen(new ExecutionCallback() {
            @Override
            public void onResponse(Object response) {
                waitForLatch();
            }

            @Override
            public void onFailure(Throwable t) {
                waitForLatch();
            }

            private void waitForLatch() {
                numberOfOutStandingCallbacks.incrementAndGet();
                try {
                    waitLatch.await();
                } catch (InterruptedException e) {
                    fail("Could not wait on the latch");
                }
            }
        });
    }

    static class SleepyProcessor implements Runnable, Serializable {

        private long millis;

        SleepyProcessor(long millis) {
            this.millis = millis;
        }

        @Override
        public void run() {
            try {

                Thread.sleep(millis);
            } catch (InterruptedException e) {
                //ignored
            }
        }
    }
}
