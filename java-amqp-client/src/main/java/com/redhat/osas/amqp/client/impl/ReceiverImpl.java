package com.redhat.osas.amqp.client.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;

import com.redhat.osas.amqp.client.api.Message;
import com.redhat.osas.amqp.client.api.Receiver;
import com.redhat.osas.amqp.client.api.Session;

public class ReceiverImpl implements Receiver {
    private List<Message> buffer = new ArrayList<Message>();
    private String address;
    private Channel channel;
    private long capacity = 0;
    private long window = 0;
    private SessionImpl session;
    private CountDownLatch fetchLatch = null;
    private Set<Long> unsettledAcks = new HashSet<Long>();
    
    
    public ReceiverImpl(String address, Channel channel, SessionImpl session) {
        this.address = address;
        this.channel = channel;
        this.session = session;
        
        // explicit accept
        // auto acquire
        session.messageSubscribe(address, address, (short)0, (short)0, false, null, null, null);
    }
    
    public void setCapacity(long count) {
        this.capacity = count;
        session.messageSetFlowMode(address, (short)1); // window
        session.messageFlow(address, (short)0, count); // <count> messages
        session.messageFlow(address, (short)1, 0xFFFFFFFFL); // infinite bytes
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
            session.messageSetFlowMode(address, (short)0); // credit
            session.messageFlow(address, (short)0, 1L); // <count> messages
            session.messageFlow(address, (short)1, 0xFFFFFFFFL); // infinite bytes
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
        // TODO Auto-generated method stub
        return false;
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public Session getSession() {
        // TODO Auto-generated method stub
        return null;
    }
}
