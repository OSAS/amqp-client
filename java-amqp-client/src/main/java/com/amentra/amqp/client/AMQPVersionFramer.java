package com.amentra.amqp.client;

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