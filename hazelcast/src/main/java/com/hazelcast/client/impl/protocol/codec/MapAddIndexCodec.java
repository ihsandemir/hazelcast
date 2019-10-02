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
 * Adds an index to this map for the specified entries so that queries can run faster.If you are querying your values
 * mostly based on age and active then you should consider indexing these fields.
 * Index attribute should either have a getter method or be public.You should also make sure to add the indexes before
 * adding entries to this map.
 * Indexing time is executed in parallel on each partition by operation threads. The Map is not blocked during this
 * operation.The time taken in proportional to the size of the Map and the number Members.
 * Until the index finishes being created, any searches for the attribute will use a full Map scan, thus avoiding
 * using a partially built index and returning incorrect results.
 */
@Generated("b7df796047464a5b8ffea603bed1b134")
public final class MapAddIndexCodec {
    //hex: 0x012D00
    public static final int REQUEST_MESSAGE_TYPE = 77056;
    //hex: 0x012D01
    public static final int RESPONSE_MESSAGE_TYPE = 77057;
    private static final int REQUEST_ORDERED_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_INITIAL_FRAME_SIZE = REQUEST_ORDERED_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + INT_SIZE_IN_BYTES;

    private MapAddIndexCodec() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class RequestParameters {

        /**
         * name of map
         */
        public java.lang.String name;

        /**
         * index attribute of value
         */
        public java.lang.String attribute;

        /**
         * true if index should be ordered, false otherwise.
         */
        public boolean ordered;
    }

    public static ClientMessage encodeRequest(java.lang.String name, java.lang.String attribute, boolean ordered) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(false);
        clientMessage.setAcquiresResource(false);
        clientMessage.setOperationName("Map.AddIndex");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeBoolean(initialFrame.content, REQUEST_ORDERED_FIELD_OFFSET, ordered);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, name);
        StringCodec.encode(clientMessage, attribute);
        return clientMessage;
    }

    public static MapAddIndexCodec.RequestParameters decodeRequest(ClientMessage clientMessage) {
        RequestParameters request = new RequestParameters();
        ClientMessage.Frame initialFrame = clientMessage.next();
        request.ordered = decodeBoolean(initialFrame.content, REQUEST_ORDERED_FIELD_OFFSET);
        request.name = StringCodec.decode(clientMessage);
        request.attribute = StringCodec.decode(clientMessage);
        return request;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class ResponseParameters {
    }

    public static ClientMessage encodeResponse() {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        clientMessage.add(initialFrame);

        return clientMessage;
    }

    public static MapAddIndexCodec.ResponseParameters decodeResponse(ClientMessage clientMessage) {
        ResponseParameters response = new ResponseParameters();
        //empty initial frame
        clientMessage.next();
        return response;
    }

}
