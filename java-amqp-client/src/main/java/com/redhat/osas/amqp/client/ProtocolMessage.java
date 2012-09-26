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

public class ProtocolMessage {
	private int messageClass;
	private int methodCode;
	MethodArguments arguments;
	MessageType messageType;
	private int channelId = 0;
	private final Long commandId;
	private SessionImpl session;
	
	public ProtocolMessage(int messageClass, int methodCode, MethodArguments arguments, MessageType messageType) {
	    this(messageClass, methodCode, arguments, messageType, null);
	}
	
	public ProtocolMessage(int messageClass, int methodCode, MethodArguments arguments, MessageType messageType, Long commandId) {
		this.messageClass = messageClass;
		this.methodCode = methodCode;
		this.arguments = arguments;
		this.messageType = messageType;
		this.commandId = commandId;
	}

	public int getMessageClass() {
		return messageClass;
	}

	public void setMessageClass(int controlClass) {
		this.messageClass = controlClass;
	}

	public int getMethodCode() {
		return methodCode;
	}

	public void setMethodCode(int code) {
		this.methodCode = code;
	}

	public MethodArguments getArguments() {
		return arguments;
	}

	public void setArguments(MethodArguments arguments) {
		this.arguments = arguments;
	}
	
	public int getChannelId() {
        return channelId;
    }
	
	public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
	
	public Long getCommandId() {
        return commandId;
    }
	
	public SessionImpl getSession() {
        return session;
    }
	
	public void setSession(SessionImpl session) {
        this.session = session;
    }
	
	@Override
	public String toString() {
	    return "ProtocolMessage[class=" + messageClass + ", methodCode=" + methodCode + ", arguments=" + arguments + "]";
	}
}
