package com.rumawaks.nifi.s2s.test;

public class TestOptions {
    private int numOfClient = 100;
    private int numOfTx = 300;
    private int numOfPacketsPerTx = 5;
    private long txIntervalMillis = 1_000;

    public int getNumOfClient() {
        return numOfClient;
    }

    public void setNumOfClient(int numOfClient) {
        this.numOfClient = numOfClient;
    }

    public int getNumOfTx() {
        return numOfTx;
    }

    public void setNumOfTx(int numOfTx) {
        this.numOfTx = numOfTx;
    }

    public int getNumOfPacketsPerTx() {
        return numOfPacketsPerTx;
    }

    public void setNumOfPacketsPerTx(int numOfPacketsPerTx) {
        this.numOfPacketsPerTx = numOfPacketsPerTx;
    }

    public long getTxIntervalMillis() {
        return txIntervalMillis;
    }

    public void setTxIntervalMillis(long txIntervalMillis) {
        this.txIntervalMillis = txIntervalMillis;
    }
}
