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

@Generated("1404e64b584d97497f4b2ff0086e8131")
public final class RingbufferStoreConfigHolderCodec {
    private static final int ENABLED_FIELD_OFFSET = 0;
    private static final int INITIAL_FRAME_SIZE = ENABLED_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES;

    private RingbufferStoreConfigHolderCodec() {
    }

    public static void encode(ClientMessage clientMessage, com.hazelcast.client.impl.protocol.task.dynamicconfig.RingbufferStoreConfigHolder ringbufferStoreConfigHolder) {
        clientMessage.add(BEGIN_FRAME.copy());

        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[INITIAL_FRAME_SIZE]);
        encodeBoolean(initialFrame.content, ENABLED_FIELD_OFFSET, ringbufferStoreConfigHolder.isEnabled());
        clientMessage.add(initialFrame);

        CodecUtil.encodeNullable(clientMessage, ringbufferStoreConfigHolder.getClassName(), StringCodec::encode);
        CodecUtil.encodeNullable(clientMessage, ringbufferStoreConfigHolder.getFactoryClassName(), StringCodec::encode);
        CodecUtil.encodeNullable(clientMessage, ringbufferStoreConfigHolder.getImplementation(), DataCodec::encode);
        CodecUtil.encodeNullable(clientMessage, ringbufferStoreConfigHolder.getFactoryImplementation(), DataCodec::encode);
        MapCodec.encodeNullable(clientMessage, ringbufferStoreConfigHolder.getProperties(), StringCodec::encode, StringCodec::encode);

        clientMessage.add(END_FRAME.copy());
    }

    public static com.hazelcast.client.impl.protocol.task.dynamicconfig.RingbufferStoreConfigHolder decode(ClientMessage clientMessage) {
        // begin frame
        clientMessage.next();

        ClientMessage.Frame initialFrame = clientMessage.next();
        boolean enabled = decodeBoolean(initialFrame.content, ENABLED_FIELD_OFFSET);

        java.lang.String className = CodecUtil.decodeNullable(clientMessage, StringCodec::decode);
        java.lang.String factoryClassName = CodecUtil.decodeNullable(clientMessage, StringCodec::decode);
        com.hazelcast.nio.serialization.Data implementation = CodecUtil.decodeNullable(clientMessage, DataCodec::decode);
        com.hazelcast.nio.serialization.Data factoryImplementation = CodecUtil.decodeNullable(clientMessage, DataCodec::decode);
        java.util.Map<java.lang.String, java.lang.String> properties = MapCodec.decodeNullable(clientMessage, StringCodec::decode, StringCodec::decode);

        fastForwardToEndFrame(clientMessage);

        return new com.hazelcast.client.impl.protocol.task.dynamicconfig.RingbufferStoreConfigHolder(className, factoryClassName, implementation, factoryImplementation, properties, enabled);
    }
}
