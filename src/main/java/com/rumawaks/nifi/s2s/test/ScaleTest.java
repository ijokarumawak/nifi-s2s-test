package com.rumawaks.nifi.s2s.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScaleTest {

    public static void main(String[] args) {
        final TestOptions testOptions = new TestOptions();
        testOptions.setNumOfClient(1200);
        testOptions.setNumOfTx(500);
        testOptions.setNumOfPacketsPerTx(5);

        final int numOfClient = testOptions.getNumOfClient();
        try (final MetricCollector metricCollector =
                 new MetricCollector(testOptions)) {
            final List<Thread> threads = new ArrayList<>(numOfClient);
            for (int i = 0; i < numOfClient; i++) {
                final S2SClient s2sClient = new S2SClient(i);
                final Thread thread = new Thread(() -> s2sClient.send(metricCollector));
                thread.start();
                threads.add(thread);

                try {
                    // Jitter
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
