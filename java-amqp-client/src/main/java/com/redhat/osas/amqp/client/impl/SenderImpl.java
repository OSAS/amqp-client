package com.redhat.osas.amqp.client.impl;

import java.util.ArrayList;
import java.util.List;

import com.redhat.osas.amqp.client.protocol.DeliveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.osas.amqp.client.Future;
import com.redhat.osas.amqp.client.api.Message;
import com.redhat.osas.amqp.client.api.Sender;
import com.redhat.osas.amqp.client.api.Session;


public class SenderImpl implements Sender {
    private static final Logger LOG = LoggerFactory.getLogger(SenderImpl.class);
    private SessionImpl session;
    private String address;
    private List<Message> buffer = new ArrayList<Message>(50);
    private int capacity = 50;
    private int window = 0;
    private List<Future> pending = new ArrayList<Future>();
    private int sent = 0, completed = 0;
    
    public SenderImpl(String address, SessionImpl session) {
        this.address = address;
        this.session = session;
    }

    public void send(Message message) {
        send(message, false);
    }

    private void checkPending(boolean flush) {
        if (flush) {
            session.flush();
        }
        
        List<Future> completedFutures = new ArrayList<Future>();
        for (Future pendingSend : pending) {
            if (pendingSend.isComplete(session)) {
                completedFutures.add(pendingSend);
                buffer.remove(0);
                completed++;
            } else {
                break;
            }
        }
        pending.removeAll(completedFutures);
        if (flush) {
            LOG.error("Buffer size now = {}, sent = {}, completed = {}", new Object[] {buffer.size(), sent, completed});
            LOG.error("checkPending :: Available now = {}", getAvailable());
        }
    }
    
    public void send(Message message, boolean sync) {
        LOG.info("Attempting to send");
        if (getAvailable() == 0) {
            LOG.error("Capacity reached, buffer size = {}", getUnsettled());
            session.flush();
            while (getAvailable() == 0) {
                // wait for at least the first pending send to be complete
                pending.get(0).waitForCompletion(session);
                pending.remove(0);
                buffer.remove(0);
                completed++;
                
                // check the rest
                checkPending(false);
            }
            
            LOG.error("Available now = {}", getAvailable());
        }
        
        if (++window > (capacity / 4)) {
            checkPending(true);
            window = 0;
        }
        sent++;
        
        LOG.info("Proceeding to send");
        DeliveryProperties deliveryProperties = null;
        
        if (message.getSubject() != null ||
            message.getTtl() != null)
        {
            deliveryProperties = new DeliveryProperties();
            deliveryProperties.setRoutingKey(message.getSubject());
            deliveryProperties.setTtl(message.getTtl());    
        }
        
        buffer.add(message);
        
        //explicit, not acquired
        Future future = session.messageTransfer(address, (short)0, (short)1, deliveryProperties, null, null, message.getContent());
        pending.add(future);
        if (sync) {
            session.sync();
        }
    }

    public void close() {
        // TODO Auto-generated method stub
        
    }

    public void setCapacity(int capacity) {
        if (null == buffer) {
            buffer = new ArrayList<Message>(capacity);
        }
        this.capacity = capacity;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getUnsettled() {
        return buffer.size();
    }

    public long getAvailable() {
        return capacity - buffer.size();
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public Session getSession() {
        return session;
    }
}
