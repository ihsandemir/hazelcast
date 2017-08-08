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

import com.hazelcast.cache.CacheTestSupport;
import com.hazelcast.cache.ICache;
import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.config.Config;
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

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import static org.junit.Assert.assertEquals;

@RunWith(HazelcastSerialClassRunner.class)
@Category({CompatibilityTest.class, SlowTest.class})
public class MixedVersionTest extends CacheTestSupport {

    private static final String MEMBER_VERSION = "3.8.2";
    private static final String CLIENT_VERSION = "3.6.2";

    private HazelcastInstance member;
    private HazelcastInstance client;

    @Override
    protected HazelcastInstance getHazelcastInstance() {
        if (member != null) {
            return member;
        }

        Config config = new Config();
        config.setProperty("hazelcast.compatibility.3.6.client", "true");
        config.getCacheConfig("example").setBackupCount(2).setName("test");
        member = HazelcastStarter.newHazelcastInstance(MEMBER_VERSION, config, false);

        return member;
    }

    @Override
    protected void onSetup() {

    }

    @Override
    protected void onTearDown() {
        client.shutdown();
        member.shutdown();
    }

    @Override
    protected CachingProvider getCachingProvider() {
        client = HazelcastStarter.newHazelcastClient(CLIENT_VERSION, false);

        return HazelcastClientCachingProvider.createCachingProvider(client);
    }

    @Test
    public void testICacheHzClient362_onHzMember382() {
        ICache<String, String> cache = createCache("testICacheHzClient362_onHzMember382");

        cache.put("Key1", "Value1");
        assertEquals("Value1", cache.get("Key1"));
    }
}
