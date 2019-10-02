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

package com.hazelcast.client.impl.protocol.codec;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.Generated;
import com.hazelcast.client.impl.protocol.codec.builtin.*;
import com.hazelcast.client.impl.protocol.codec.custom.*;

import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;

/*
 * This file is auto-generated by the Hazelcast Client Protocol Code Generator.
 * To change this file, edit the templates or the protocol
 * definitions on the https://github.com/hazelcast/hazelcast-client-protocol
 * and regenerate it.
 */

/**
 * Returns the EntryView for the specified key.
 * This method returns a clone of original mapping, modifying the returned value does not change the actual value
 * in the map. One should put modified value back to make changes visible to all nodes.
 */
@Generated("ed7f7b677d391dd1b74b44d2397cb6d7")
public final class MapGetEntryViewCodec {
    //hex: 0x012100
    public static final int REQUEST_MESSAGE_TYPE = 73984;
    //hex: 0x012101
    public static final int RESPONSE_MESSAGE_TYPE = 73985;
    private static final int REQUEST_THREAD_ID_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_INITIAL_FRAME_SIZE = REQUEST_THREAD_ID_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int RESPONSE_MAX_IDLE_FIELD_OFFSET = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_MAX_IDLE_FIELD_OFFSET + LONG_SIZE_IN_BYTES;

    private MapGetEntryViewCodec() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class RequestParameters {

        /**
         * name of map
         */
        public java.lang.String name;

        /**
         * the key of the entry.
         */
        public com.hazelcast.nio.serialization.Data key;

        /**
         * The id of the user thread performing the operation. It is used to guarantee that only the lock holder thread (if a lock exists on the entry) can perform the requested operation.
         */
        public long threadId;
    }

    public static ClientMessage encodeRequest(java.lang.String name, com.hazelcast.nio.serialization.Data key, long threadId) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(true);
        clientMessage.setAcquiresResource(false);
        clientMessage.setOperationName("Map.GetEntryView");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeLong(initialFrame.content, REQUEST_THREAD_ID_FIELD_OFFSET, threadId);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, name);
        DataCodec.encode(clientMessage, key);
        return clientMessage;
    }

    public static MapGetEntryViewCodec.RequestParameters decodeRequest(ClientMessage clientMessage) {
        RequestParameters request = new RequestParameters();
        ClientMessage.Frame initialFrame = clientMessage.next();
        request.threadId = decodeLong(initialFrame.content, REQUEST_THREAD_ID_FIELD_OFFSET);
        request.name = StringCodec.decode(clientMessage);
        request.key = DataCodec.decode(clientMessage);
        return request;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class ResponseParameters {

        /**
         * TODO DOC
         */
        public com.hazelcast.map.impl.SimpleEntryView<com.hazelcast.nio.serialization.Data, com.hazelcast.nio.serialization.Data> response;

        /**
         * TODO DOC
         */
        public long maxIdle;
    }

    public static ClientMessage encodeResponse(com.hazelcast.map.impl.SimpleEntryView<com.hazelcast.nio.serialization.Data, com.hazelcast.nio.serialization.Data> response, long maxIdle) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        encodeLong(initialFrame.content, RESPONSE_MAX_IDLE_FIELD_OFFSET, maxIdle);
        clientMessage.add(initialFrame);

        CodecUtil.encodeNullable(clientMessage, response, SimpleEntryViewCodec::encode);
        return clientMessage;
    }

    public static MapGetEntryViewCodec.ResponseParameters decodeResponse(ClientMessage clientMessage) {
        ResponseParameters response = new ResponseParameters();
        ClientMessage.Frame initialFrame = clientMessage.next();
        response.maxIdle = decodeLong(initialFrame.content, RESPONSE_MAX_IDLE_FIELD_OFFSET);
        response.response = CodecUtil.decodeNullable(clientMessage, SimpleEntryViewCodec::decode);
        return response;
    }

}
