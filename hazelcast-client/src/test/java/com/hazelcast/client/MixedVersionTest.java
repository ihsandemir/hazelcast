/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.CompatibilityTest;
import com.hazelcast.test.annotation.SlowTest;
import com.hazelcast.test.starter.HazelcastStarter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(HazelcastSerialClassRunner.class)
@Category({CompatibilityTest.class, SlowTest.class})
public class MixedVersionTest {

    private static final String MEMBER_VERSION = "3.8.3";
    private static final String CLIENT_VERSION = "3.6.2";

    private HazelcastInstance member;
    private HazelcastInstance client;

    @Before
    public void setup() {
        member = HazelcastStarter.newHazelcastInstance(MEMBER_VERSION);
        client = HazelcastStarter.newHazelcastClient(CLIENT_VERSION, false);
    }

    @Test
    public void testHzClient362_onHzMember383() {
        IMap mapFromClient = client.getMap("test");
        mapFromClient.put("a", "b");

        IMap mapFromMember = member.getMap("test");
        assertEquals("b", mapFromMember.get("a"));
    }

    @After
    public void tearDown() {
        client.getLifecycleService().terminate();
        member.getLifecycleService().terminate();
    }
}
