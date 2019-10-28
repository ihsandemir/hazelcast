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

package com.hazelcast.map.impl;

import com.hazelcast.map.listener.MapPartitionLostListener;
import com.hazelcast.spi.impl.eventservice.EventFilter;
import com.hazelcast.spi.impl.eventservice.EventRegistration;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Helper event listener methods for {@link MapServiceContext}.
 */
public interface MapServiceContextEventListenerSupport {

    CompletableFuture<EventRegistration> addLocalEventListener(Object mapListener, String mapName);

    CompletableFuture<EventRegistration> addLocalEventListener(Object mapListener, EventFilter eventFilter, String mapName);

    CompletableFuture<EventRegistration> addLocalPartitionLostListener(MapPartitionLostListener listener, String mapName);

    CompletableFuture<EventRegistration> addEventListener(Object mapListener, EventFilter eventFilter, String mapName);

    CompletableFuture<EventRegistration> addPartitionLostListener(MapPartitionLostListener listener, String mapName);

    CompletableFuture<EventRegistration> removeEventListener(String mapName, UUID registrationId);

    CompletableFuture<EventRegistration> removePartitionLostListener(String mapName, UUID registrationId);

}
