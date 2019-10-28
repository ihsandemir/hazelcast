/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.impl.protocol.task;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.nio.Connection;
import com.hazelcast.spi.impl.eventservice.EventRegistration;

import java.util.UUID;

/**
 * Base message task for listener registration tasks
 */
public abstract class AbstractAddListenerMessageTask<P> extends AbstractListenerMessageTask<P> {

    protected AbstractAddListenerMessageTask(ClientMessage clientMessage, Node node, Connection connection) {
        super(clientMessage, node, connection);
    }

    @Override
    public void accept(Object response, Throwable throwable) {
        if (throwable == null) {
            if ((boolean) response) {
                EventRegistration eventRegistration = null;
                try {
                    eventRegistration = this.eventRegistrationFuture.get();
                } catch (Exception e) {
                    handleProcessingFailure(e);
                }
                UUID registrationId = eventRegistration.getId();
                endpoint.addListenerDestroyAction(getServiceName(), getDistributedObjectName(), registrationId);
                sendResponse(registrationId);
            } else {
                sendResponse(null);
            }
        } else {
            handleProcessingFailure(throwable);
        }
    }

}
