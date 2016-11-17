/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.test;

import com.hazelcast.client.connection.ClientConnectionManager;
import com.hazelcast.client.impl.HazelcastClientInstanceImpl;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.Address;
import com.hazelcast.test.HazelcastTestSupport;

/**
 * Created by ihsan on 05/09/16.
 */
public class ClientTestSupport extends HazelcastTestSupport {

    protected void blockMessagesFromInstance(HazelcastInstance instance, HazelcastInstance client) {
        HazelcastClientInstanceImpl clientImpl = getHazelcastClientInstanceImpl(client);
        ClientConnectionManager connectionManager = clientImpl.getConnectionManager();
        Address address = instance.getCluster().getLocalMember().getAddress();
        ((TestClientRegistry.MockClientConnectionManager) connectionManager).block(address);
    }

    protected void unblockMessagesFromInstance(HazelcastInstance instance, HazelcastInstance client) {
        HazelcastClientInstanceImpl clientImpl = getHazelcastClientInstanceImpl(client);
        ClientConnectionManager connectionManager = clientImpl.getConnectionManager();
        Address address = instance.getCluster().getLocalMember().getAddress();
        ((TestClientRegistry.MockClientConnectionManager) connectionManager).unblock(address);
    }

    /**
     * Blocks the traffic both outgoing and incoming. The blocking is done at client connection side.
     * @param instance The connected member
     * @param client The client for which to block
     */
    protected void blockInstance(HazelcastInstance instance, HazelcastInstance client) {
        HazelcastClientInstanceImpl clientImpl = getHazelcastClientInstanceImpl(client);
        ClientConnectionManager connectionManager = clientImpl.getConnectionManager();
        Address address = instance.getCluster().getLocalMember().getAddress();
        ((TestClientRegistry.MockClientConnectionManager) connectionManager).blockAll(address);
    }

    protected HazelcastClientInstanceImpl getHazelcastClientInstanceImpl(HazelcastInstance client) {
        HazelcastClientProxy clientProxy = (HazelcastClientProxy) client;
        return clientProxy.client;
    }

    protected HazelcastInstance getOwnerServer(TestHazelcastFactory factory, HazelcastClientInstanceImpl client) {
        Address ownerConnectionAddress = client.getClientClusterService().getOwnerConnectionAddress();
        return factory.getInstance(ownerConnectionAddress);
    }
}
