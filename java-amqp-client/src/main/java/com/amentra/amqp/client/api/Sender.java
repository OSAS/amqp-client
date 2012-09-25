package com.amentra.amqp.client.api;

public interface Sender {
    void send(Message message);
    void send(Message message, boolean sync);
    void close();
    void setCapacity(int capacity);
    long getCapacity();
    long getUnsettled();
    long getAvailable();
    String getName();
    Session getSession();
}
