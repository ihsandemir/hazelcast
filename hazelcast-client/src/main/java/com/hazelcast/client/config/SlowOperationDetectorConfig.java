/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.config;

import com.hazelcast.client.spi.impl.ClientInvocation;

import java.util.HashMap;

public class SlowOperationDetectorConfig {
    private boolean isEnabled;
    private long checkPeriodInMillis;
    private HashMap<Integer, Long> timeouts = new HashMap<Integer, Long>();
    private UserNotifier userNotifier;
    private long warningPrintIntervalMillis = 10000;

    public interface UserNotifier {
        void notifySlowInvocation(ClientInvocation clientInvocation, long configuredTimeoutInMillis,
                                  long currentInvocationTimeInMillis);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public long getCheckPeriodInMillis() {
        return checkPeriodInMillis;
    }

    public void setCheckPeriodInMillis(long checkPeriodInMillis) {
        this.checkPeriodInMillis = checkPeriodInMillis;
    }

    public void setTimeoutInMillis(int messageType, long timeoutInMillis) {
        timeouts.put(messageType, timeoutInMillis);
    }

    public long getTimeoutInMillis(int messageType) {
        Long timeout = timeouts.get(messageType);
        if (timeout == null) {
            return -1;
        }

        return timeout;
    }

    public UserNotifier getUserNotifier() {
        return userNotifier;
    }

    public void setUserNotifier(UserNotifier userNotifier) {
        this.userNotifier = userNotifier;
    }

    public long getWarningPrintIntervalMillis() {
        return warningPrintIntervalMillis;
    }

    public void setWarningPrintIntervalMillis(long warningPrintIntervalMillis) {
        this.warningPrintIntervalMillis = warningPrintIntervalMillis;
    }
}
