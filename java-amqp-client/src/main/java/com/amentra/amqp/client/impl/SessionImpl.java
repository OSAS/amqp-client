package com.amentra.amqp.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amentra.amqp.client.Future;
import com.amentra.amqp.client.FutureListener;
import com.amentra.amqp.client.ProtocolMessage;
import com.amentra.amqp.client.SequencePair;
import com.amentra.amqp.client.SequenceSet;
import com.amentra.amqp.client.SessionBase;
import com.amentra.amqp.client.api.Connection;
import com.amentra.amqp.client.api.Message;
import com.amentra.amqp.client.api.Receiver;
import com.amentra.amqp.client.api.Sender;
import com.amentra.amqp.client.api.Session;
import com.amentra.amqp.client.protocol.MessageTransferArguments;
import com.amentra.amqp.client.protocol.SessionCompletedArguments;
import com.amentra.amqp.client.protocol.SessionFlushArguments;
import com.amentra.amqp.client.protocol.SessionKnownCompletedArguments;
import com.amentra.amqp.client.protocol.SessionProxy;

public class SessionImpl extends SessionBase implements Session {
    private static final Logger LOG = LoggerFactory.getLogger(SessionBase.class);
    
    enum State {
        DETACHING,
        DETACHED,
        ATTACHING,
        ATTACHED
    }
    
    State state = State.DETACHED;
    private ConnectionImpl connectionImpl;
    
    @SuppressWarnings("unused")
    private Long peerCommandOffset;
    
    private Long peerCommandId;
    private long nextCommandId = 0;
    private Map<String, ReceiverImpl> receiverMap = new HashMap<String, ReceiverImpl>();
    private int channelId = 0;
    
//    private List<Long> peerIncomplete = new ArrayList<Long>();
    private List<Long> peerCompleted = new ArrayList<Long>();
    
    private Map<Long, ProtocolMessage> myIncompleteCommands = new HashMap<Long, ProtocolMessage>();
    private Map<String, SenderImpl> senderMap = new HashMap<String, SenderImpl>();
    private SessionProxy sessionProxy = new SessionProxy();
    
    // TODO there has to be a better way to do this
    private Map<Message, MessageImpl> messageImplMap = new HashMap<Message, MessageImpl>();
    
    private CountDownLatch closeLatch = null;
    private Lock myLock = new ReentrantLock();
    private Condition myCondition = myLock.newCondition();
    
    private Map<Long, Future> futures = new HashMap<Long, Future>();
    
//    private Set<MessageImpl> unackedMessages = new HashSet<MessageImpl>();
    private Map<Long, ReceiverImpl> receiversByMessage = new HashMap<Long, ReceiverImpl>();
    private Map<Long, ReceiverImpl> receiversByPendingAck = new HashMap<Long, ReceiverImpl>();
    
    public SessionImpl(ConnectionImpl connectionImpl) {
        this.connectionImpl = connectionImpl;
    }

    public void close() {
        state = State.DETACHING;
        closeLatch = new CountDownLatch(1);
        executionSync();
        try {
            closeLatch.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void acknowledge(long commandId) {
        SequenceSet transfers = new SequenceSet();
        transfers.add(new SequencePair(commandId, commandId));
        messageAccept(transfers);
    }
    
    public void setCommandByteOffset(Long commandOffset) {
        this.peerCommandOffset = commandOffset;
    }

    public void setPeerCommandId(Long commandId) {
        this.peerCommandId = commandId;
    }
    
    public Receiver createReceiver(String address) {
        ReceiverImpl receiver = new ReceiverImpl(address, connectionImpl.getChannel(), this);
        LOG.debug("Adding receiver to map");
        receiverMap.put(address, receiver);
        return receiver;
    }
    
    public Sender createSender(String address) {
        SenderImpl sender = new SenderImpl(address, this);
        senderMap.put(address, sender);
        return sender;
    }
    
    @SuppressWarnings("unchecked")
    public void onMessageTransfer(ProtocolMessage command) {
        MessageTransferArguments arguments = (MessageTransferArguments) command.getArguments();
        String destination = arguments.getDestination();
        ReceiverImpl receiver = receiverMap.get(destination);
        if (receiver != null) {
            MessageImpl m = new MessageImpl();
            m.setSubject(arguments.getDeliveryProperties().getRoutingKey());
            m.setContent(arguments.getBody());
            if (arguments.getMessageProperties() != null) {
                m.setProperties(arguments.getMessageProperties().getApplicationHeaders());
            }
            
            m.setCommandId(command.getCommandId());
            
//            if(arguments.getAcceptMode() == 0) {
//                unackedMessages.add(m);
//            }
            
            receiversByMessage.put(m.getCommandId(), receiver);
            
            receiver.onMessageTransfer(m);
        }
        markCompleted(command);
    }
    
    public void markCompleted(ProtocolMessage command) {
        LOG.debug("Marking peer command id {} complete", command.getCommandId());
        peerCompleted.add(command.getCommandId());
    }
    
    public long getNextCommandId() {
        return nextCommandId++;
    }
    
    public long getNextPeerCommandId() {
        return peerCommandId++;
    }
    
    public int getChannelId() {
        return channelId;
    }
    
    public void setChannelId(int channel) {
        this.channelId = channel;
    }
    
    public Channel getChannel() {
        return connectionImpl.getChannel();
    }
    
    protected Future send(ProtocolMessage pm) {
        long nextCommandId = getNextCommandId();
        LOG.debug("Adding command id {} for ProtocolMessage {} to incomplete commands", nextCommandId, pm);
        myIncompleteCommands.put(nextCommandId, pm);
        
        @SuppressWarnings("unused")
        ChannelFuture future = getChannel().write(pm);
        
        Future f = new Future(nextCommandId);
        futures.put(nextCommandId, f);
        return f;
    }

    public void onFlush(SessionFlushArguments arguments) {
        if (arguments.getCompleted()) {
            SessionCompletedArguments completedArgs = new SessionCompletedArguments();
            SequenceSet commands = new SequenceSet();
            
            long low = -1;
            long high = -1;
            for (Long completedId : peerCompleted) {
                if (-1 == low) {
                    low = completedId;
                    high = completedId;
                    continue;
                }
                
                if (completedId > high + 1) {
                    commands.add(new SequencePair(low, high));
                    low = completedId;
                    high = completedId;
                }
            }
            if (low != -1) {
                commands.add(new SequencePair(low, high));
            }
            
            completedArgs.setCommands(commands);
            sessionProxy.completed(completedArgs, getChannel());
        }
        
        if (arguments.getConfirmed()) {
            
        }
        
        if (arguments.getExpected()) {
            
        }
    }
    
    public void onCompleted(SessionCompletedArguments arguments) {
        myLock.lock();
        try {
            SequenceSet completedCommands = arguments.getCommands();
            LOG.error("onCompleted: {}", completedCommands);
            List<SequencePair> pairs = completedCommands.getPairs();
            for (SequencePair pair : pairs) {
                for (long start = pair.getBegin(), end = pair.getEnd(); start <= end; start++) {
                    myIncompleteCommands.remove(start);
                    Future future = futures.remove(start);
                    future.notifyListener();
                }
            }
            
            myCondition.signalAll();
            
            SessionKnownCompletedArguments knownCompletedArguments = new SessionKnownCompletedArguments();
            knownCompletedArguments.setCommands(completedCommands);
            sessionProxy.known_completed(knownCompletedArguments, connectionImpl.getChannel());
            
            if (state == State.DETACHING) {
                if (completedCommands.getPairs().isEmpty()) {
                    closeLatch.countDown();
                } else {
                    SessionFlushArguments flushArguments = new SessionFlushArguments();
                    flushArguments.setCompleted(true);
                    sessionProxy.flush(flushArguments, getChannel());
                }
            }
        } finally {
            myLock.unlock();
        }
    }
    
    public void onKnownCompleted(SessionKnownCompletedArguments arguments) {
        SequenceSet completedCommands = arguments.getCommands();
        List<SequencePair> pairs = completedCommands.getPairs();
        for (SequencePair pair : pairs) {
            for (long start = pair.getBegin(), end = pair.getEnd(); start <= end; start++) {
                peerCompleted.remove(start);
            }
        }
    }

    public void commit() {
        // TODO Auto-generated method stub
        
    }

    public void rollback() {
        // TODO Auto-generated method stub
        
    }

    public void acknowledge() {
        acknowledge(false);
    }

    public void acknowledge(boolean sync) {
        // TODO Auto-generated method stub
    }
    
    public void acknowledge(Message message) {
        acknowledge(message, false);
    }

    private FutureListener acknowledgeCompleted = new FutureListener() {
        public void onCompletion(Future future) {
            ReceiverImpl receiver = receiversByPendingAck.get(future.getCommandId());
            receiver.acknowledgeCompleted(future.getCommandId());
        }
    };
    
    public void acknowledge(Message message, boolean sync) {
        Future future = messageAccept(sequenceSetForMessage(message));
        ReceiverImpl receiver = receiversByMessage.get(((MessageImpl)message).getCommandId());
        receiver.addUnsettledAck(future.getCommandId());
        receiversByPendingAck.put(future.getCommandId(), receiver);
        future.setListener(acknowledgeCompleted);
        
        if (sync) {
            sync();
        }
    }

    public void acknowledgeThrough(Message message) {
        acknowledgeThrough(message, false);
    }

    public void acknowledgeThrough(Message message, boolean sync) {
        // TODO Auto-generated method stub
        
    }

    public void reject(Message message) {
        // 0 means unspecified
        messageReject(sequenceSetForMessage(message), 0, null);
        //TODO need to remove from messageImplMap at some point
    }

    public void release(Message message) {
        messageRelease(sequenceSetForMessage(message), true);
        //TODO need to remove from messageImplMap at some point
    }
    
    private SequenceSet sequenceSetForMessage(Message message) {
        SequenceSet s = new SequenceSet();
        long commandId = ((MessageImpl)message).getCommandId();
        s.add(new SequencePair(commandId, commandId));
        return s;
    }

    public void sync() {
        sync(true);
    }

    public void sync(boolean block) {
        Future future = executionSync();
        if (block) {
            future.waitForCompletion(this);
        }
    }

    public long getReceivable() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getUnsettledAcks() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Receiver nextReceiver() {
        return nextReceiver(0);
    }

    public Receiver nextReceiver(long timeout) {
        // TODO Auto-generated method stub
        return null;
    }

    public Sender getSender(String address) {
        // TODO Auto-generated method stub
        return senderMap.get(address);
    }

    public Receiver getReceiver(String address) {
        return receiverMap.get(address);
    }

    public Connection getConnection() {
        return connectionImpl.getConnection();
    }

    public boolean hasError() {
        // TODO Auto-generated method stub
        return false;
    }

    public void checkError() {
        // TODO Auto-generated method stub
    }
    
    public void waitForCompletion(long commandId) {
        myLock.lock();
        try {
            while (!isComplete(commandId)) {
                myCondition.await();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            myLock.unlock();
        }
    }
    
    public boolean isComplete(long commandId) {
        return !myIncompleteCommands.containsKey(commandId);
    }
    
    public void flush() {
        SessionFlushArguments arguments = new SessionFlushArguments();
        arguments.setCompleted(true);
        sessionProxy.flush(arguments, getChannel());
    }
}
