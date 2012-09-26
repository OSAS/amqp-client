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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class AMQPVersionFramer extends FrameDecoder {
	boolean done = false;
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		if (done && buffer.readable()) {
			return buffer.readBytes(buffer.readableBytes());
		}
		
		if (buffer.readableBytes() < 8) {
			return null;
		}
		
		byte[] amqp = new byte[4];
		buffer.readBytes(amqp);
		
		short protocolClass = buffer.readUnsignedByte();
		short protocolInstance = buffer.readUnsignedByte();
		short majorVersionSupported = buffer.readUnsignedByte();
		short minorVersionSupported = buffer.readUnsignedByte();
		
		VersionMessage vm = new VersionMessage();
		
		vm.setProtocol(new String(amqp));
		vm.setProtocolClass(protocolClass);
		vm.setProtocolInstance(protocolInstance);
		vm.setMajorVersionSupported(majorVersionSupported);
		vm.setMinorVersionSupported(minorVersionSupported);
		
		done = true;
		ctx.getPipeline().remove(this);
		return vm;
	}
}