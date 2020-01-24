/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.impl.spi.impl.listener;

import com.hazelcast.client.impl.spi.ProxyManager;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.spi.exception.DistributedObjectDestroyedException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.UUID;

@SuppressFBWarnings("SE_BAD_FIELD")
public final class LazyDistributedObjectEvent extends DistributedObjectEvent {
    private static final long serialVersionUID = 6564764769334087775L;
    private ProxyManager proxyManager;
    private final ProxyManager.ClientProxyFuture clientProxyFuture;

    /**
     * Constructs a DistributedObject Event.
     * @param eventType         The event type as an enum {@link EventType} integer.
     * @param serviceName       The service name of the DistributedObject.
     * @param objectName        The name of the DistributedObject.
     * @param clientProxyFuture The DistributedObject future for the event.
     * @param source            The UUID of the client/member which caused the create/destroy of the proxy.
     * @param proxyManager      The ProxyManager for lazily creating the proxy if not available on the client.
     */
    public LazyDistributedObjectEvent(EventType eventType, String serviceName, String objectName,
                                      ProxyManager.ClientProxyFuture clientProxyFuture, UUID source, ProxyManager proxyManager) {
        super(eventType, serviceName, objectName, null, source);
        this.proxyManager = proxyManager;
        this.clientProxyFuture = clientProxyFuture;
    }

    @Override
    public DistributedObject getDistributedObject() {
        if (EventType.DESTROYED.equals(getEventType())) {
            throw new DistributedObjectDestroyedException(getObjectName() + " destroyed!");
        }
        if (distributedObject != null) {
            return distributedObject;
        }
        if (clientProxyFuture != null) {
            distributedObject = clientProxyFuture.get();
        }
        if (distributedObject == null) {
            distributedObject = proxyManager.getOrCreateLocalProxy(getServiceName(), (String) getObjectName());
        }
        return distributedObject;
    }

    @Override
    public String toString() {
        DistributedObject object = null;
        if (EventType.CREATED.equals(getEventType())) {
            object = getDistributedObject();
        }
        return "LazyDistributedObjectEvent{" + "distributedObject=" + object + ", eventType=" + getEventType() + ", serviceName='"
                + getServiceName() + '\'' + ", objectName='" + getObjectName() + '\'' + ", source=" + source + '}';
    }
}
