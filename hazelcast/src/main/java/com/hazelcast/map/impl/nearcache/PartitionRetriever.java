package com.hazelcast.map.impl.nearcache;/*
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

import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ihsan on 01/02/17.
 */
public class PartitionRetriever implements EntryProcessor, DataSerializable {
    private int maxPartitionId;
    private PartitionService partitionService;

    public PartitionRetriever() {
        this.partitionService = null;
    }

    public PartitionRetriever(int maxPartition, PartitionService service) {
        this.maxPartitionId = maxPartition;
        this.partitionService = service;
    }

    @Override
    public Object process(Map.Entry entry) {
        Partition partition = partitionService.getPartition(entry.getKey());
        if (partition.getPartitionId() == maxPartitionId) {
            return entry.getValue();
        }
        return null;
    }

    @Override
    public EntryBackupProcessor getBackupProcessor() {
        return null;
    }

    @Override
    public void writeData(ObjectDataOutput out)
            throws IOException {
        out.writeInt(maxPartitionId);
    }

    @Override
    public void readData(ObjectDataInput in)
            throws IOException {
        maxPartitionId = in.readInt();
    }
}
