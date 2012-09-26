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

public class SegmentEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if(!(msg instanceof Assembly)) {
			return msg;
		}
		
		Assembly assembly = (Assembly)msg;
		
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		
		List<Segment> segments = assembly.getSegments();
		for (Segment segment : segments) {
    		List<Frame> frames = segment.getFrames();
    		for (Frame frame : frames) {
    			short h1 = 0;
    			
    			if(frame.isFirstSegment()) {
    				h1 |= 0x08;
    			}
    			if(frame.isLastSegment()) {
    				h1 |= 0x04;
    			}
    			if(frame.isFirstFrame()) {
    				h1 |= 0x02;
    			}
    			if(frame.isLastFrame()) {
    				h1 |= 0x01;
    			}
    			
    			buffer.writeByte(h1);
    			buffer.writeByte(frame.getSegmentType());
    			buffer.writeShort(frame.getFrameSize());
    			buffer.writeByte(0);
    			buffer.writeByte(0x0F & frame.getTrack());
    			buffer.writeShort(frame.getChannel());
    			buffer.writeInt(0);
    			buffer.writeBytes(frame.getPayload());
    		}
		}
		
		return buffer;
	}

}
