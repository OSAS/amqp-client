/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.redhat.osas.amqp.client.impl;

import com.redhat.osas.amqp.client.api.Message;
import com.redhat.osas.amqp.client.api.Receiver;
import com.redhat.osas.amqp.client.api.Session;
import org.jboss.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReceiverImpl implements Receiver {
    private List<Message> buffer = new ArrayList<Message>();
    private String address;
    private Channel channel;
    private long capacity = 0;
    private long window = 0;
    boolean closed = true;
    private SessionImpl session;
    private CountDownLatch fetchLatch = null;
    private Set<Long> unsettledAcks = new HashSet<Long>();


    public ReceiverImpl(String address, Channel channel, SessionImpl session) {
        this.address = address;
        this.channel = channel;
        this.session = session;

        // explicit accept
        // auto acquire
        session.messageSubscribe(address, address, (short) 0, (short) 0, false, null, null, null);
        closed = false;
    }

    public void setCapacity(long count) {
        this.capacity = count;
        session.messageSetFlowMode(address, (short) 1); // window
        session.messageFlow(address, (short) 0, count); // <count> messages
        session.messageFlow(address, (short) 1, 0xFFFFFFFFL); // infinite bytes
        session.messageFlush(address);
    }

    public Message fetch() {
        return fetch(0);
    }

    public Message fetch(long timeout) {
        if (!buffer.isEmpty()) {
            Message m = buffer.remove(0);
            return m;
        }

        if (capacity == 0) {
            // allocate credit for 1 message if not using a buffer
            session.messageSetFlowMode(address, (short) 0); // credit
            session.messageFlow(address, (short) 0, 1L); // <count> messages
            session.messageFlow(address, (short) 1, 0xFFFFFFFFL); // infinite bytes
        }

        return get(timeout);
    }

    public void onMessageTransfer(Message message) {
        buffer.add(message);
        if (fetchLatch != null) {
            fetchLatch.countDown();
        }
    }

    public void close() {
        session.messageStop(address);
        closed = true;
    }

    public Message get() {
        return get(0);
    }

    public Message get(long timeout) {
        if (!buffer.isEmpty()) {
            Message m = buffer.remove(0);
            return m;
        }

        fetchLatch = new CountDownLatch(1);

        try {
            if (timeout > 0) {
                fetchLatch.await(timeout, TimeUnit.SECONDS);
            } else {
                fetchLatch.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fetchLatch = null;

        if (!buffer.isEmpty()) {
            Message m = buffer.remove(0);
            return m;
        }

        return null;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getAvailable() {
        return buffer.size();
    }

    public void addUnsettledAck(long commandId) {
        unsettledAcks.add(commandId);
    }

    public void acknowledgeCompleted(long commandId) {
        unsettledAcks.remove(commandId);
    }

    public long getUnsettled() {
        return unsettledAcks.size();
    }

    public boolean isClosed() {
        return closed;
    }

    public String getName() {
        return address;
    }

    public Session getSession() {
        return session;
    }
}
