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
