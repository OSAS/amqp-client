package com.redhat.osas.amqp.client;

import com.redhat.osas.amqp.client.impl.SessionImpl;

public class Future {
    private long commandId;
    private boolean complete = false;
    private FutureListener listener;
    
    public Future(long commandId) {
        this.commandId = commandId;
    }
    
    public void waitForCompletion(SessionImpl session) {
        if (!complete) {
            session.waitForCompletion(commandId);
        }
        
        complete = true;
    }
    
    public boolean isComplete(SessionImpl session) {
        return complete || session.isComplete(commandId);
    }
    
    public long getCommandId() {
        return commandId;
    }

    public void setListener(FutureListener listener) {
        this.listener = listener;
    }
    
    public void notifyListener() {
        if (listener != null) {
            listener.onCompletion(this);
        }
    }
}
