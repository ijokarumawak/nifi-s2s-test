package com.rumawaks.nifi.s2s.test;

import org.apache.nifi.remote.Transaction;
import org.apache.nifi.remote.TransferDirection;
import org.apache.nifi.remote.client.KeystoreType;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.protocol.DataPacket;
import org.apache.nifi.remote.util.StandardDataPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

public class S2SClient {

    private static final Logger LOG = LoggerFactory.getLogger(S2SClient.class);
    private final int id;

    public S2SClient(int id) {
        this.id = id;
    }

    public void send(final MetricCollector metricCollector) {

        final int numOfTx = metricCollector.getNumOfTx();
        final int numOfPacketsPerTx = metricCollector.getNumOfPacketsPerTx();
        final long txIntervalMillis = metricCollector.getTxIntervalMillis();

        final long start = currentTimeMillis();
        long createTxMillis = 0;
        long sendMillis = 0;
        long confirmMillis = 0;
        long completeMillis = 0;
        int numOfFailure = 0;

        try (
            final SiteToSiteClient client = new SiteToSiteClient.Builder()
            .url("https://nifi0:8443/nifi")
            .portName("input")
            .timeout(60, TimeUnit.SECONDS)
            .idleExpiration(70, TimeUnit.SECONDS)
            .truststoreFilename("s2s-client/truststore.jks")
            .truststorePass("password")
            .truststoreType(KeystoreType.JKS)
            .keystoreFilename("s2s-client/keystore.jks")
            .keystorePass("password")
            .keystoreType(KeystoreType.JKS)
            .build()
        ) {

            long s = currentTimeMillis();
            for (int i = 0; i < numOfTx; i++) {
                try {
                    s = currentTimeMillis();
                    final Transaction transaction = client.createTransaction(TransferDirection.SEND);
                    if (transaction == null) {
                        numOfFailure++;
                        continue;
                    }
                    final long _createTxMillis = currentTimeMillis() - s;


                    s = currentTimeMillis();
                    for (int p = 0; p < numOfPacketsPerTx; p++) {
                        final byte[] bytes = String.format("%d,%d,%d\n", id, i, p).getBytes();
                        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                        final DataPacket packet = new StandardDataPacket(Collections.emptyMap(), bais, bytes.length);
                        transaction.send(packet);
                    }
                    final long _sendMillis = currentTimeMillis() - s;

                    s = currentTimeMillis();
                    transaction.confirm();
                    final long _confirmMillis = currentTimeMillis() - s;

                    s = currentTimeMillis();
                    transaction.complete();
                    final long _completeMillis = currentTimeMillis() - s;

                    // Accumulate successful tx metrics.
                    createTxMillis += _createTxMillis;
                    sendMillis += _sendMillis;
                    confirmMillis += _confirmMillis;
                    completeMillis += _completeMillis;

                } catch (Exception e) {
                    final long failedMillis = currentTimeMillis() - s;
                    LOG.error("Transaction failed after {} millis due to " + e, failedMillis, e);
                    numOfFailure++;
                }

                try {
                    Thread.sleep(txIntervalMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        metricCollector.send(id, start, currentTimeMillis(), numOfFailure,
            createTxMillis, sendMillis, confirmMillis, completeMillis);
    }
}
