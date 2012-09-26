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
