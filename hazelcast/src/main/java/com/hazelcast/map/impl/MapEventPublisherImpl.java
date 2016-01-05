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

package com.hazelcast.map.impl;

import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.EntryView;
import com.hazelcast.map.impl.wan.MapReplicationRemove;
import com.hazelcast.map.impl.wan.MapReplicationUpdate;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.nio.serialization.SerializationService;
import com.hazelcast.query.impl.QueryEntry;
import com.hazelcast.spi.EventFilter;
import com.hazelcast.spi.EventRegistration;
import com.hazelcast.spi.EventService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.impl.eventservice.impl.EmptyFilter;
import com.hazelcast.wan.ReplicationEventObject;
import com.hazelcast.wan.WanReplicationPublisher;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.hazelcast.map.impl.MapService.SERVICE_NAME;

class MapEventPublisherImpl implements MapEventPublisher {

    protected final MapServiceContext mapServiceContext;

    protected MapEventPublisherImpl(MapServiceContext mapServiceContext) {
        this.mapServiceContext = mapServiceContext;
    }

    @Override
    public void publishWanReplicationUpdate(String mapName, EntryView entryView) {
        MapContainer mapContainer = mapServiceContext.getMapContainer(mapName);
        MapReplicationUpdate replicationEvent = new MapReplicationUpdate(mapName, mapContainer.getWanMergePolicy(),
                entryView);
        mapContainer.getWanReplicationPublisher().publishReplicationEvent(SERVICE_NAME, replicationEvent);
    }

    @Override
    public void publishWanReplicationRemove(String mapName, Data key, long removeTime) {
        final MapReplicationRemove event = new MapReplicationRemove(mapName, key, removeTime);

        publishWanReplicationEventInternal(mapName, event);
    }

    @Override
    public void publishMapEvent(Address caller, String mapName, EntryEventType eventType,
                                int numberOfEntriesAffected) {
        final Collection<EventRegistration> registrations = new LinkedList<EventRegistration>();
        for (EventRegistration registration : getRegistrations(mapName)) {
            if (!(registration.getFilter() instanceof MapPartitionLostEventFilter)) {
                registrations.add(registration);
            }
        }

        if (registrations.isEmpty()) {
            return;
        }

        final String source = getThisNodesAddress();
        final MapEventData mapEventData = new MapEventData(source, mapName, caller,
                eventType.getType(), numberOfEntriesAffected);

        publishEventInternal(registrations, mapEventData, mapName.hashCode());
    }

    @Override
    public void publishEvent(Address caller, String mapName, EntryEventType eventType,
                             Data dataKey, Object oldValue, Object value) {
        publishEvent(caller, mapName, eventType, false, dataKey, oldValue, value);
    }

    @Override
    public void publishEvent(Address caller, String mapName, EntryEventType eventType, boolean syntheticEvent,
                             final Data dataKey, Object oldValue, Object value) {
        publishEvent(caller, mapName, eventType, syntheticEvent, dataKey, oldValue, value, null);

    }

    @Override
    public void publishEvent(Address caller, String mapName, EntryEventType eventType, boolean syntheticEvent,
                             final Data dataKey, Object oldValue, Object value, Object mergingValue) {
        final Collection<EventRegistration> registrations = getRegistrations(mapName);
        if (registrations.isEmpty()) {
            return;
        }

        List<EventRegistration> registrationsWithValue = null;
        List<EventRegistration> registrationsWithoutValue = null;

        for (final EventRegistration candidate : registrations) {
            final EventFilter filter = candidate.getFilter();
            final Result result = applyEventFilter(filter, syntheticEvent, dataKey, oldValue, value, eventType);

            registrationsWithValue = initRegistrationsWithValue(registrationsWithValue, result);
            registrationsWithoutValue = initRegistrationsWithoutValue(registrationsWithoutValue, result);

            registerCandidate(result, candidate, registrationsWithValue, registrationsWithoutValue);
        }

        final boolean withValueRegistrationExists = isNotEmpty(registrationsWithValue);
        final boolean withoutValueRegistrationExists = isNotEmpty(registrationsWithoutValue);

        if (!withValueRegistrationExists && !withoutValueRegistrationExists) {
            return;
        }

        final int orderKey = pickOrderKey(dataKey);

        if (withValueRegistrationExists) {
            Data dataOldValue = mapServiceContext.toData(oldValue);
            Data dataValue = mapServiceContext.toData(value);
            Data dataMergingValue = mapServiceContext.toData(mergingValue);
            EntryEventData eventData = createEntryEventData(mapName, caller,
                    dataKey, dataValue, dataOldValue, dataMergingValue, eventType.getType());
            publishEventInternal(registrationsWithValue, eventData, orderKey);
        }
        if (withoutValueRegistrationExists) {
            EntryEventData eventData = createEntryEventData(mapName, caller,
                    dataKey, null, null, null, eventType.getType());
            publishEventInternal(registrationsWithoutValue, eventData, orderKey);
        }
    }

    @Override
    public void publishMapPartitionLostEvent(Address caller, String mapName, int partitionId) {
        Logger logger = Logger.getLogger("publishMapPartitionLostEvent");
        final Collection<EventRegistration> registrations = new LinkedList<EventRegistration>();
        for (EventRegistration registration : getRegistrations(mapName)) {
            if (registration.getFilter() instanceof MapPartitionLostEventFilter) {
                logger.info("Registration:" + registration + " mapped the MapPartitionLostEventFilter. Adding into the list.");
                registrations.add(registration);
            }
        }

        if (registrations.isEmpty()) {
            logger.info("No registration mapped the partition lost filter. registrations.size:" + registrations.size() + ", regs:" + registrations);
            return;
        }

        final String thisNodesAddress = getThisNodesAddress();
        final MapPartitionEventData eventData = new MapPartitionEventData(thisNodesAddress, mapName, caller, partitionId);
        logger.info("[publishMapPartitionLostEvent] publishEventInternal is performed. Registrations size:" + registrations.size() + ", regs:" + registrations);
        publishEventInternal(registrations, eventData, partitionId);
    }

    @Override
    public void hintMapEvent(Address caller, String mapName, EntryEventType eventType,
                             int numberOfEntriesAffected, int partitionId) {
        // NOP
    }

    private List<EventRegistration> initRegistrationsWithoutValue(List<EventRegistration> registrationsWithoutValue,
                                                                  Result result) {
        if (registrationsWithoutValue != null) {
            return registrationsWithoutValue;
        }

        if (Result.NO_VALUE_INCLUDED.equals(result)) {
            registrationsWithoutValue = new ArrayList<EventRegistration>();
        }
        return registrationsWithoutValue;
    }

    private List<EventRegistration> initRegistrationsWithValue(List<EventRegistration> registrationsWithValue,
                                                               Result result) {
        if (registrationsWithValue != null) {
            return registrationsWithValue;
        }

        if (Result.VALUE_INCLUDED.equals(result)) {
            registrationsWithValue = new ArrayList<EventRegistration>();
        }

        return registrationsWithValue;
    }

    private static <T> boolean isNotEmpty(Collection<T> collection) {
        return !(collection == null || collection.isEmpty());
    }

    protected Result applyEventFilter(EventFilter filter, boolean syntheticEvent, Data dataKey,
                                      Object oldValue, Object value, EntryEventType eventType) {

        if (filter instanceof MapPartitionLostEventFilter) {
            return Result.NONE;
        }

        // below, order of ifs are important.
        // QueryEventFilter is instance of EntryEventFilter.
        // SyntheticEventFilter wraps an event filter.
        if (filter instanceof SyntheticEventFilter) {
            if (syntheticEvent) {
                return Result.NONE;
            }
            final SyntheticEventFilter syntheticEventFilter = (SyntheticEventFilter) filter;
            filter = syntheticEventFilter.getFilter();
        }

        if (filter instanceof EmptyFilter) {
            return Result.VALUE_INCLUDED;
        }


        if (filter instanceof QueryEventFilter) {
            return processQueryEventFilter(filter, eventType, dataKey, oldValue, value);
        }

        if (filter instanceof EntryEventFilter) {
            return processEntryEventFilter(filter, dataKey);
        }


        throw new IllegalArgumentException("Unknown EventFilter type = [" + filter.getClass().getCanonicalName() + "]");
    }

    Collection<EventRegistration> getRegistrations(String mapName) {
        final MapServiceContext mapServiceContext = this.mapServiceContext;
        final NodeEngine nodeEngine = mapServiceContext.getNodeEngine();
        final EventService eventService = nodeEngine.getEventService();

        return eventService.getRegistrations(SERVICE_NAME, mapName);
    }

    private int pickOrderKey(Data key) {
        return key == null ? -1 : key.hashCode();
    }

    private void registerCandidate(Result result, EventRegistration candidate,
                                   Collection<EventRegistration> registrationsWithValue,
                                   Collection<EventRegistration> registrationsWithoutValue) {
        switch (result) {
            case VALUE_INCLUDED:
                registrationsWithValue.add(candidate);
                break;
            case NO_VALUE_INCLUDED:
                registrationsWithoutValue.add(candidate);
                break;
            case NONE:
                break;
            default:
                throw new IllegalArgumentException("Not a known result type [" + result + "]");
        }
    }

    void publishEventInternal(Collection<EventRegistration> registrations, Object eventData, int orderKey) {
        final MapServiceContext mapServiceContext = this.mapServiceContext;
        final NodeEngine nodeEngine = mapServiceContext.getNodeEngine();
        final EventService eventService = nodeEngine.getEventService();

        eventService.publishEvent(SERVICE_NAME, registrations, eventData, orderKey);
    }

    private String getThisNodesAddress() {
        final NodeEngine nodeEngine = mapServiceContext.getNodeEngine();
        final Address thisAddress = nodeEngine.getThisAddress();
        return thisAddress.toString();
    }

    private Result processEntryEventFilter(EventFilter filter, Data dataKey) {
        final EntryEventFilter eventFilter = (EntryEventFilter) filter;

        if (!eventFilter.eval(dataKey)) {
            return Result.NONE;
        }

        if (eventFilter.isIncludeValue()) {
            return Result.VALUE_INCLUDED;
        }

        return Result.NO_VALUE_INCLUDED;
    }

    private Result processQueryEventFilter(EventFilter filter, EntryEventType eventType,
                                           final Data dataKey, Object oldValue, Object value) {
        final NodeEngine nodeEngine = mapServiceContext.getNodeEngine();
        final SerializationService serializationService = nodeEngine.getSerializationService();
        Object testValue;
        if (eventType == EntryEventType.REMOVED || eventType == EntryEventType.EVICTED) {
            testValue = oldValue;
        } else {
            testValue = value;
        }
        final QueryEventFilter queryEventFilter = (QueryEventFilter) filter;
        final QueryEntry entry = new QueryEntry(serializationService, dataKey, dataKey, testValue);
        if (queryEventFilter.eval(entry)) {
            return queryEventFilter.isIncludeValue() ? Result.VALUE_INCLUDED : Result.NO_VALUE_INCLUDED;
        }
        return Result.NONE;
    }

    void publishWanReplicationEventInternal(String mapName, ReplicationEventObject event) {
        final MapServiceContext mapServiceContext = this.mapServiceContext;
        final MapContainer mapContainer = mapServiceContext.getMapContainer(mapName);
        final WanReplicationPublisher wanReplicationPublisher = mapContainer.getWanReplicationPublisher();
        wanReplicationPublisher.publishReplicationEvent(SERVICE_NAME, event);
    }

    private EntryEventData createEntryEventData(String mapName, Address caller,
                                                Data dataKey, Data dataNewValue, Data dataOldValue,
                                                Data dataMergingValue, int eventType) {
        final String thisNodesAddress = getThisNodesAddress();
        return new EntryEventData(thisNodesAddress, mapName, caller,
                dataKey, dataNewValue, dataOldValue, dataMergingValue, eventType);
    }

    protected enum Result {
        VALUE_INCLUDED,
        NO_VALUE_INCLUDED,
        NONE
    }
}
