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
 * Returns the ScheduledFuture's delay in nanoseconds for the task in the scheduler.
 */
@Generated("1ac0072af2bd20d314267d3d90729d83")
public final class ScheduledExecutorGetDelayFromAddressCodec {
    //hex: 0x1D0800
    public static final int REQUEST_MESSAGE_TYPE = 1902592;
    //hex: 0x1D0801
    public static final int RESPONSE_MESSAGE_TYPE = 1902593;
    private static final int REQUEST_INITIAL_FRAME_SIZE = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int RESPONSE_RESPONSE_FIELD_OFFSET = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_RESPONSE_FIELD_OFFSET + LONG_SIZE_IN_BYTES;

    private ScheduledExecutorGetDelayFromAddressCodec() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class RequestParameters {

        /**
         * The name of the scheduler.
         */
        public java.lang.String schedulerName;

        /**
         * The name of the task
         */
        public java.lang.String taskName;

        /**
         * The address of the member where the task will get scheduled.
         */
        public com.hazelcast.nio.Address address;
    }

    public static ClientMessage encodeRequest(java.lang.String schedulerName, java.lang.String taskName, com.hazelcast.nio.Address address) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(true);
        clientMessage.setAcquiresResource(false);
        clientMessage.setOperationName("ScheduledExecutor.GetDelayFromAddress");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, schedulerName);
        StringCodec.encode(clientMessage, taskName);
        AddressCodec.encode(clientMessage, address);
        return clientMessage;
    }

    public static ScheduledExecutorGetDelayFromAddressCodec.RequestParameters decodeRequest(ClientMessage clientMessage) {
        RequestParameters request = new RequestParameters();
        //empty initial frame
        clientMessage.next();
        request.schedulerName = StringCodec.decode(clientMessage);
        request.taskName = StringCodec.decode(clientMessage);
        request.address = AddressCodec.decode(clientMessage);
        return request;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class ResponseParameters {

        /**
         * The remaining delay of the task formatted in nanoseconds.
         */
        public long response;
    }

    public static ClientMessage encodeResponse(long response) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        encodeLong(initialFrame.content, RESPONSE_RESPONSE_FIELD_OFFSET, response);
        clientMessage.add(initialFrame);

        return clientMessage;
    }

    public static ScheduledExecutorGetDelayFromAddressCodec.ResponseParameters decodeResponse(ClientMessage clientMessage) {
        ResponseParameters response = new ResponseParameters();
        ClientMessage.Frame initialFrame = clientMessage.next();
        response.response = decodeLong(initialFrame.content, RESPONSE_RESPONSE_FIELD_OFFSET);
        return response;
    }

}
