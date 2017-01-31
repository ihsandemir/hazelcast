/*
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
package com.hazelcast.client.map.impl.nearcache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.HdrHistogram.Histogram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ihsan on 24/01/17.
 */
public class NearCachePerformanceTest {
    private static final long NANOS_IN_MILLIS = 1000 * 1000;
    private ThreadParameters params;

    public NearCachePerformanceTest(String[] args) {
        params = parseArguments(args);
        if (params == null) {
            throw new IllegalArgumentException();
        }
    }

    public static void main(String[] args) {
        NearCachePerformanceTest test = new NearCachePerformanceTest(args);

        try {
            test.run();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void run()
            throws UnknownHostException {
        System.out.println();
        System.out.println("Starting test with the following parameters:\n" + params.toString());

        ClientConfig clientConfig = new ClientConfig();
        if (params.useNearCache) {
            clientConfig.addNearCacheConfig(new NearCacheConfig(params.mapName));
        }

        clientConfig.getNetworkConfig().addAddress(params.serverIp + ":5701");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<Integer, Integer> map = client.getMap(params.mapName);

        params.map = map;

        fillMap(params);

        warmup(params);

        if (params.useNearCache) {
            System.out.println(
                    "After warmup before test start the near cache stats is:" + map.getLocalMapStats().getNearCacheStats().
                            toString());
        }

        List<ThreadInfo> allThreadsInfo = new ArrayList<ThreadInfo>();

        for (int i = 0; i < params.numberOfThreads; ++i) {
            final ThreadParameters clonedParams = params.clone();
            final ThreadInfo threadInfo = new ThreadInfo(new Thread(new Runnable() {
                @Override
                public void run() {
                    performGets(clonedParams);
                }
            }), clonedParams);

            threadInfo.thread.start();
            allThreadsInfo.add(threadInfo);
        }

        for (ThreadInfo info : allThreadsInfo) {
            try {
                info.thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Test finished");

        if (params.useNearCache) {
            System.out.println("Near cache stats after the test is:" + map.getLocalMapStats().getNearCacheStats().toString());
        }

        generateHistogram(allThreadsInfo);

        client.shutdown();
    }

    private void generateHistogram(List<ThreadInfo> allThreadsInfo) {
        long highestTrackableValue = findMaximumValue(allThreadsInfo);

        Histogram histogram = new Histogram(highestTrackableValue, 1);

        for (ThreadInfo info : allThreadsInfo) {
            List<Long> values = info.params.values;
            System.out.println("Thread [" + info.thread.getId() + "] recorded " + values.size() + " values");
            for (long value : values) {
                try {
                    histogram.recordValue(value);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            PrintStream statsFile = new PrintStream(new File(params.outFileName));
            histogram.outputPercentileDistribution(statsFile, 1, 1.0, false);
            statsFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Failed to open stats file " + params.outFileName + " for output. Will use stdout.");
            histogram.outputPercentileDistribution(System.out, 1, 1.0, false);
        }
    }

    private long findMaximumValue(List<ThreadInfo> allThreadsInfo) {
        long max = 0;
        for (ThreadInfo info : allThreadsInfo) {
            for (long value : info.params.values) {
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    private void performGets(ThreadParameters params) {
        long testStart = System.currentTimeMillis();
        long testEndTime = testStart + params.testDuration;

        long configuredLatency = params.operationInterval * NANOS_IN_MILLIS;

        Random random = new Random();

        long expectedStartTime = System.nanoTime();
        while (System.currentTimeMillis() < testEndTime) {
            int key = random.nextInt() % params.keySetSize;

            sleepUntil(expectedStartTime);

            // perform the measured operation
            params.map.get(key);

            long end = System.nanoTime();

            long durationMicroseconds = (end - expectedStartTime) / 1000;
            if (durationMicroseconds > 0) {
                params.values.add(durationMicroseconds);
            }

            expectedStartTime += configuredLatency;
        }
    }

    private void sleepUntil(long expectedStartTime) {
        long now;
        while ((now = System.nanoTime()) < expectedStartTime) {
            LockSupport.parkNanos(expectedStartTime - now);
        }
    }

    private void fillMap(ThreadParameters params) {
        for (int i = 0; i < params.keySetSize; ++i) {
            params.map.put(i, i);
        }
    }

    void warmup(ThreadParameters params) {
        for (int i = 0; i < params.keySetSize; ++i) {
            params.map.get(i);
        }
    }

    private class ThreadParameters {
        private IMap<Integer, Integer> map;
        private int keySetSize = 100000;
        private int numberOfThreads = 40;
        private int testDuration = 10 * 60 * 1000;      // milliseconds
        private int operationInterval = 1; // milliseconds
        private String serverIp = "127.0.0.1";
        private boolean useNearCache = false;
        private String outFileName = "NearCacheResult.txt";
        private String mapName = "NearCachePerformanceMap";
        private List<Long> values = new ArrayList<Long>();

        public ThreadParameters clone() {
            ThreadParameters copy = new ThreadParameters();
            copy.map = map;
            copy.keySetSize = keySetSize;
            copy.mapName = mapName;
            copy.numberOfThreads = numberOfThreads;
            copy.operationInterval = operationInterval;
            copy.outFileName = outFileName;
            copy.serverIp = serverIp;
            copy.testDuration = testDuration;
            copy.useNearCache = useNearCache;
            return copy;
        }

        @Override
        public String toString() {
            return "ThreadParameters{" +
                    "\n" + "\t\tmap=" + map +
                    "\n" + "\t\tkeySetSize=" + keySetSize +
                    "\n" + "\t\tnumberOfThreads=" + numberOfThreads +
                    "\n" + "\t\ttestDuration=" + testDuration +
                    "\n" + "\t\toperationInterval=" + operationInterval +
                    "\n" + "\t\tserverIp='" + serverIp + '\'' +
                    "\n" + "\t\tuseNearCache=" + useNearCache +
                    "\n" + "\t\toutFileName='" + outFileName + '\'' +
                    "\n" + "\t\tmapName='" + mapName + '\'' +
                    "\n" + "\t\tvalues=" + values +
                    "\n}";
        }
    }

    private ThreadParameters parseArguments(String[] args) {
        final String serverIpArg = "--server-ip=";
        final String useNearCacheArg = "--use-near-cache";
        final String keySetSize = "--key-set-size=";
        final String threadCountArg = "--num-threads=";
        final String testDurationArg = "--test-duration-in-milliseconds=";
        final String operationIntervalArg = "--operation-interval-in-millis=";
        final String helpArg = "--help";
        final String statsFileArg = "--stats-output-file=";
        String usage = "USAGE: " + this.getClass().getSimpleName() + serverIpArg + "<value> " + serverIpArg + "<value> "
                + useNearCacheArg + " " + keySetSize + "<value> " + threadCountArg + "<value> " + testDurationArg + "<value> "
                + operationIntervalArg + "<value> " + helpArg + " " + statsFileArg + "<file path>";

        ThreadParameters params = new ThreadParameters();

        for (String argument : args) {
            if (helpArg.equals(argument)) {
                System.out.println(usage);
                return null;
            }

            String valueString = getValueString(serverIpArg, argument);
            if (valueString != null) {
                params.serverIp = valueString;
            }

            valueString = getValueString(statsFileArg, argument);
            if (valueString != null) {
                params.outFileName = valueString;
            }

            if (argument.equals(useNearCacheArg)) {
                params.useNearCache = true;
            }

            int val = getPozitifIntArgument(keySetSize, argument, usage);
            if (val > 0) {
                params.keySetSize = val;
            }

            val = getPozitifIntArgument(threadCountArg, argument, usage);
            if (val > 0) {
                params.numberOfThreads = val;
            }

            val = getPozitifIntArgument(testDurationArg, argument, usage);
            if (val > 0) {
                params.testDuration = val;
            }

            val = getIntArgument(operationIntervalArg, argument, usage);
            if (val >= 0) {
                params.operationInterval = val;
            }
        }

        // pre-allocate the result array
        if (params.operationInterval > 0) {
            int arraySize = (int) params.testDuration/params.operationInterval;
            params.values = new ArrayList<Long>(arraySize);
        }

        return params;
    }

    private int getIntArgument(String argumentName, String argumentValue, String usage) {
        String value = getValueString(argumentName, argumentValue);
        if (value == null) {
            return -1;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            System.out.println(argumentName + " is not an integer. Provided value is \'" + value + "\'. " + usage);
            return -1;
        }
    }

    private int getPozitifIntArgument(String argumentName, String argumentValue, String usage) {
        int val = getIntArgument(argumentName, argumentValue, usage);

        if (val < 0) {
            System.out.println(argumentName + " can not be negative. Provided value is \'" + argumentValue + "\'. " + usage);
            return -1;
        }
        return val;
    }

    private static String getValueString(String argumentName, String value) {
        if (value.startsWith(argumentName)) {
            return value.substring(argumentName.length());
        }
        return null;
    }

    private class ThreadInfo {
        private final Thread thread;
        private final ThreadParameters params;

        private ThreadInfo(Thread thread, ThreadParameters params) {
            this.thread = thread;
            this.params = params;
        }
    }
}
