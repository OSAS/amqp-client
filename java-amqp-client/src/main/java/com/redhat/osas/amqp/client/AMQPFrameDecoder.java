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
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMQPFrameDecoder extends FrameDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(AMQPFrameDecoder.class);
    
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		if (buffer.readableBytes() < 4) {
			return null;
		}
		
		// mark the reader index in case the buffer doesn't contain the entire frame
		// (but we won't know until we get the frame size)
		buffer.markReaderIndex();
		
		short h1 = buffer.readUnsignedByte();
		short segmentType = buffer.readUnsignedByte();
		int totalFrameSize = buffer.readUnsignedShort();
		
		// need to subtract 4 from totalFrame size because we've just read the 1st 4 bytes of the header
		if (buffer.readableBytes() < (totalFrameSize - 4)) {
			buffer.resetReaderIndex();
			return null;
		}
		
		LOG.debug("Got frame with segment type {}", segmentType);
		
		int payloadSize = totalFrameSize - 12;
		int track = buffer.readUnsignedShort();
		int amqpChannel = buffer.readUnsignedShort();
		buffer.skipBytes(4);
		ChannelBuffer payloadBuffer = ChannelBuffers.buffer(payloadSize);
		buffer.readBytes(payloadBuffer);
		
		Frame frame = new Frame();
		frame.setFirstSegment((h1 & 0x08) == 0x08);
		frame.setLastSegment((h1 & 0x04) == 0x04);
		frame.setFirstFrame((h1 & 0x02) == 0x02);
		frame.setLastFrame((h1 & 0x01) == 0x01);
		frame.setSegmentType(segmentType);
		frame.setFrameSize(totalFrameSize);
		frame.setTrack(track);
		frame.setChannel(amqpChannel);
		frame.setPayload(payloadBuffer);
		
		return frame;
	}
}