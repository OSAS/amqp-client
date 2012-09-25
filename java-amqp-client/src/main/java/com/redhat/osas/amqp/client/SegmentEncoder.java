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
