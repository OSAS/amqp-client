package com.amentra.amqp.client.api;

public interface Session {
    void close();
    void commit();
    void rollback();
    void acknowledge();
    void acknowledge(boolean sync);
    void acknowledge(Message message);
    void acknowledge(Message message, boolean sync);
    void acknowledgeThrough(Message message);
    void acknowledgeThrough(Message message, boolean sync);
    void reject(Message message);
    void release(Message message);
    void sync();
    void sync(boolean block);
    long getReceivable();
    long getUnsettledAcks();
    Receiver nextReceiver();
    Receiver nextReceiver(long timeout);
    Sender createSender(String address);
    Receiver createReceiver(String address);
    Sender getSender(String address);
    Receiver getReceiver(String address);
    Connection getConnection();
    boolean hasError();
    void checkError();
}
