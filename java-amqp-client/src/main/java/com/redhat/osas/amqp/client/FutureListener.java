package com.redhat.osas.amqp.client;

public interface FutureListener {
    void onCompletion(Future future);
}
