/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.spi.impl;


import com.hazelcast.core.HazelcastOverloadException;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import static com.hazelcast.nio.Bits.CACHE_LINE_LENGTH;
import static com.hazelcast.nio.Bits.LONG_SIZE_IN_BYTES;


public abstract class CallIdSequence {

    /**
     * Creates the next call-id.
     * <p/>
     *
     * @return the generated callId.
     */
    public abstract long next();

    /**
     * Creates the call id without applying any backpressure
     *
     * @return the generated call id.
     */
    public abstract long renew();

    public abstract void complete();

    public abstract boolean isOverloadFeatureEnabled();

    /**
     * A {@link com.hazelcast.spi.impl.operationservice.impl.CallIdSequence} that provided backpressure by taking
     * the number in flight operations into account when a call-id needs to be determined.
     * <p/>
     * It is possible to temporary exceed the capacity:
     * - due to system operations
     * - due to racy nature of checking if space is available and getting the next sequence.
     * <p/>
     */
    public static final class CallIdSequenceFailFast extends CallIdSequence {
        private static final int INDEX_HEAD = 7;
        private static final int INDEX_TAIL = INDEX_HEAD + CACHE_LINE_LENGTH / LONG_SIZE_IN_BYTES;

        // instead of using 2 AtomicLongs, we use an array if width of 3 cache lines to prevent any false sharing.
        private final AtomicLongArray longs = new AtomicLongArray(3 * CACHE_LINE_LENGTH / LONG_SIZE_IN_BYTES);

        private final int maxConcurrentInvocations;
        private final boolean isOverloadFeatureEnabled;
        private final ILogger logger = Logger.getLogger(getClass());
        AtomicLong invCount = new AtomicLong(0);

        public CallIdSequenceFailFast(int maxConcurrentInvocations) {
            this.maxConcurrentInvocations = maxConcurrentInvocations;
            isOverloadFeatureEnabled = (maxConcurrentInvocations > -1);
        }

        @Override
        public long next() {
            if (isOverloadFeatureEnabled) {
                if (!hasSpace()) {
                    throw new HazelcastOverloadException(
                            "maxConcurrentInvocations : " + maxConcurrentInvocations + " is reached.");
                }
            }

            invCount.incrementAndGet();
            return longs.incrementAndGet(INDEX_HEAD);
        }

        @Override
        public long renew() {
            invCount.incrementAndGet();
            return longs.incrementAndGet(INDEX_HEAD);
        }

        private boolean hasSpace() {
            return invCount.get() < maxConcurrentInvocations;
            //return longs.get(INDEX_HEAD) - longs.get(INDEX_TAIL) < maxConcurrentInvocations;
        }

        @Override
        public void complete() {
            longs.incrementAndGet(INDEX_TAIL);
            invCount.decrementAndGet();
        }

        @Override
        public boolean isOverloadFeatureEnabled() {
            return isOverloadFeatureEnabled;
        }
    }
}
