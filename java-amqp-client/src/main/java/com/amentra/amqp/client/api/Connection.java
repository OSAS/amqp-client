package com.amentra.amqp.client.api;

import com.amentra.amqp.client.impl.ConnectionImpl;
import com.amentra.amqp.client.impl.SessionImpl;


public class Connection {
	private String host;
	private short port = 5672;
	private ConnectionImpl impl = new ConnectionImpl(this);
	
	public Connection(String host, short port) {
	    this.host = host;
	    this.port = port;
	}
	
	public void open() {
		impl.open(host, port);
	}
	
	public SessionImpl createSession(String sessionId) {
		return impl.createSession(sessionId);
	}
}
