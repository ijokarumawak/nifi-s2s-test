package com.rumawaks.nifi.s2s.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static java.lang.String.format;

public class MetricCollector implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollector.class);
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final int numOfTx;
    private final int numOfPacketsPerTx;
    private final long txIntervalMillis;

    private final OutputStream indexOutput;
    private final OutputStream output;

    private int totalSuccessfulTx = 0;
    private int totalFailureTx = 0;
    private long totalCreateTxMillis = 0;
    private long totalSendMillis = 0;
    private long totalConfirmMillis = 0;
    private long totalCompleteMillis = 0;

    public MetricCollector(int numOfClient, int numOfTx, int numOfPacketsPerTx, long txIntervalMillis) {
        this.numOfTx = numOfTx;
        this.numOfPacketsPerTx = numOfPacketsPerTx;
        this.txIntervalMillis = txIntervalMillis;

        final String csvFileName = format("%1$tY%1$tm%1$td_%1$tH%1$tM_%2$d.csv", new Date(), numOfClient);

        try {
            indexOutput = new FileOutputStream("test-results/metrics-index.txt", true);
            output = new FileOutputStream(format("test-results/metrics/%s", csvFileName));

            indexOutput.write(format("- %s: numOfClient=%d, numOfTx=%d, numOfPacketsPerTx=%d, txIntervalMillis=%d, ",
                csvFileName, numOfClient, numOfTx, numOfPacketsPerTx, txIntervalMillis).getBytes(CHARSET));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public synchronized void send(int clientId, long start, long end, int numOfFailure,
                     long createTxMillis, long sendMillis, long confirmMillis, long completeMillis) {

        final int successfulTx = numOfTx - numOfFailure;
        totalSuccessfulTx += successfulTx;
        totalFailureTx += numOfFailure;
        totalCreateTxMillis += createTxMillis;
        totalSendMillis += sendMillis;
        totalConfirmMillis += confirmMillis;
        totalCompleteMillis += completeMillis;

        final long avgCreateTxMillis = successfulTx == 0 ? -1 : createTxMillis / successfulTx;
        final long avgSendMillis = successfulTx == 0 ? -1 : sendMillis / successfulTx;
        final long avgConfirmMillis = successfulTx == 0 ? -1 : confirmMillis / successfulTx;
        final long avgCompleteMillis = successfulTx == 0 ? -1 : completeMillis / successfulTx;
        final String line = format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d", clientId, start, end, numOfFailure, createTxMillis, sendMillis, confirmMillis, completeMillis, avgCreateTxMillis, avgSendMillis, avgConfirmMillis, avgCompleteMillis);

        try {
            logger.info(line);
            output.write((line + System.lineSeparator()).getBytes(CHARSET));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumOfTx() {
        return numOfTx;
    }

    public int getNumOfPacketsPerTx() {
        return numOfPacketsPerTx;
    }

    public long getTxIntervalMillis() {
        return txIntervalMillis;
    }

    @Override
    public void close() throws IOException {
        try {
            if (indexOutput != null) {

                final long avgCreateTxMillis = totalSuccessfulTx == 0 ? -1 : totalCreateTxMillis / totalSuccessfulTx;
                final long avgSendMillis = totalSuccessfulTx == 0 ? -1 : totalSendMillis / totalSuccessfulTx;
                final long avgConfirmMillis = totalSuccessfulTx == 0 ? -1 : totalConfirmMillis / totalSuccessfulTx;
                final long avgCompleteMillis = totalSuccessfulTx == 0 ? -1 : totalCompleteMillis / totalSuccessfulTx;

                indexOutput.write(format("totalSuccessfulTx=%d, totalFailureTx=%d, avgCreateTxMillis=%d, avgSendMillis=%d, avgConfirmMillis=%d, avgCompleteMillis=%d\n",
                    totalSuccessfulTx, totalFailureTx, avgCreateTxMillis, avgSendMillis, avgConfirmMillis, avgCompleteMillis).getBytes(CHARSET));
                indexOutput.close();
            }
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
