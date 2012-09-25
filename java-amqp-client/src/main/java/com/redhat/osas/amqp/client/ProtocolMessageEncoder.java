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
