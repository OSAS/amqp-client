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

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class ProtocolMessageEncoder extends OneToOneEncoder {
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if(!(msg instanceof ProtocolMessage)) {
			return msg;
		}
		
		ProtocolMessage cm = (ProtocolMessage)msg;
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		
		buffer.writeByte(cm.getMessageClass());
		buffer.writeByte(cm.getMethodCode());
		
		cm.getArguments().encode(buffer);
		
		Segment segment = SegmentBuilder.createSegment(cm.messageType == MessageType.COMMAND ? Segment.Type.COMMAND : Segment.Type.CONTROL, buffer, cm.getChannelId(), true, cm.getArguments().getHeaders().isEmpty());
		
		Assembly assembly = new Assembly();
		assembly.addSegment(segment);
		
		if(cm.messageType == MessageType.COMMAND) {
		    List<Struct> headers = cm.getArguments().getHeaders();
		    int numHeaders = headers.size();
		    boolean hasBody = cm.getArguments().hasBody() && cm.getArguments().getBody() != null && cm.getArguments().getBody().length > 0;
		    int i = 1;
		    for (Struct header : headers) {
		        ChannelBuffer headerBuffer = ChannelBuffers.dynamicBuffer();
		        header.encode(headerBuffer);
		        segment = SegmentBuilder.createSegment(Segment.Type.HEADER, headerBuffer, cm.getChannelId(), false, i == numHeaders && !hasBody);
		        assembly.addSegment(segment);
		        i++;
            }
		    
		    if (hasBody) {
		        byte[] body = cm.getArguments().getBody();
                ChannelBuffer bodyBuffer = ChannelBuffers.directBuffer(body.length);
                bodyBuffer.writeBytes(body);
                segment = SegmentBuilder.createSegment(Segment.Type.BODY, bodyBuffer, cm.getChannelId(), false, true);
                assembly.addSegment(segment);
		    }
		}
		
		return assembly;
	}
}
