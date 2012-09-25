package com.redhat.osas.amqp.client.api;

public interface Receiver {
    Message get();
    Message get(long timeout);
    Message fetch();
    Message fetch(long timeout);
    void setCapacity(long capacity);
    long getCapacity();
    long getAvailable();
    long getUnsettled();
    void close();
    boolean isClosed();
    String getName();
    Session getSession();
}
