package com.amentra.amqp.client;

import org.jboss.netty.buffer.ChannelBuffer;

public class SegmentBuilder {
	public static Segment createSegment(Segment.Type type, ChannelBuffer message, int channelId, boolean firstSegment, boolean lastSegment) {
		Segment segment = new Segment(type);
		
		int messageSize = message.readableBytes();
		int maxPayloadSize = 65535-12;
		int framesNeeded = (int)Math.ceil((double)messageSize / maxPayloadSize);
		
		for (int i = 0; i < framesNeeded; i++) {
			Frame frame = new Frame();
			frame.setFirstSegment(firstSegment);
			frame.setLastSegment(lastSegment);
			frame.setFirstFrame(i == 0);
			frame.setLastFrame(i == (framesNeeded - 1));
			frame.setSegmentType(type.getValue());
			frame.setChannel(channelId);
			frame.setTrack(type == Segment.Type.CONTROL ? 0 : 1);
			
			int frameBytes = Math.min(messageSize, maxPayloadSize);
			if (framesNeeded > 1 && i == (framesNeeded - 1)) {
				frameBytes = messageSize - (framesNeeded * maxPayloadSize);
			}
			
			frame.setPayload(message.slice(i * maxPayloadSize, frameBytes));
			frame.setFrameSize(12 + frameBytes);
			
			segment.addFrame(frame);
		}
		
		return segment;
	}
}
