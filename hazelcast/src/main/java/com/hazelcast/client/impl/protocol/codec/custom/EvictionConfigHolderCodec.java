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

package com.hazelcast.client.impl.protocol.codec.custom;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.Generated;
import com.hazelcast.client.impl.protocol.codec.builtin.*;

import static com.hazelcast.client.impl.protocol.codec.builtin.CodecUtil.fastForwardToEndFrame;
import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;

@Generated("ee687ea916cfa9b70951d916783af314")
public final class EvictionConfigHolderCodec {
    private static final int SIZE_FIELD_OFFSET = 0;
    private static final int INITIAL_FRAME_SIZE = SIZE_FIELD_OFFSET + INT_SIZE_IN_BYTES;

    private EvictionConfigHolderCodec() {
    }

    public static void encode(ClientMessage clientMessage, com.hazelcast.client.impl.protocol.task.dynamicconfig.EvictionConfigHolder evictionConfigHolder) {
        clientMessage.add(BEGIN_FRAME.copy());

        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[INITIAL_FRAME_SIZE]);
        encodeInt(initialFrame.content, SIZE_FIELD_OFFSET, evictionConfigHolder.getSize());
        clientMessage.add(initialFrame);

        StringCodec.encode(clientMessage, evictionConfigHolder.getMaxSizePolicy());
        StringCodec.encode(clientMessage, evictionConfigHolder.getEvictionPolicy());
        CodecUtil.encodeNullable(clientMessage, evictionConfigHolder.getComparatorClassName(), StringCodec::encode);
        CodecUtil.encodeNullable(clientMessage, evictionConfigHolder.getComparator(), DataCodec::encode);

        clientMessage.add(END_FRAME.copy());
    }

    public static com.hazelcast.client.impl.protocol.task.dynamicconfig.EvictionConfigHolder decode(ClientMessage clientMessage) {
        // begin frame
        clientMessage.next();

        ClientMessage.Frame initialFrame = clientMessage.next();
        int size = decodeInt(initialFrame.content, SIZE_FIELD_OFFSET);

        java.lang.String maxSizePolicy = StringCodec.decode(clientMessage);
        java.lang.String evictionPolicy = StringCodec.decode(clientMessage);
        java.lang.String comparatorClassName = CodecUtil.decodeNullable(clientMessage, StringCodec::decode);
        com.hazelcast.nio.serialization.Data comparator = CodecUtil.decodeNullable(clientMessage, DataCodec::decode);

        fastForwardToEndFrame(clientMessage);

        return new com.hazelcast.client.impl.protocol.task.dynamicconfig.EvictionConfigHolder(size, maxSizePolicy, evictionPolicy, comparatorClassName, comparator);
    }
}
