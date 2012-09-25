package com.redhat.osas.amqp.client;

import org.jboss.netty.channel.Channel;

public interface ProtocolCallback {
	void execute(ProtocolMessage command, Channel channel);
}
