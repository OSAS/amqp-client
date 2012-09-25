package com.amentra.amqp.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amentra.amqp.client.impl.ConnectionImpl;

public class AMQPVersionNegotiator extends SimpleChannelHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AMQPVersionNegotiator.class);
    
	boolean done = false;
	private ConnectionImpl connectionImpl;
	
	public AMQPVersionNegotiator(ConnectionImpl connectionImpl) {
		this.connectionImpl = connectionImpl;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		LOG.debug("AMQPVersionNegotiator :: Channel connected");
		Channel channel = e.getChannel();
		
		ChannelBuffer header = ChannelBuffers.buffer(8);
		byte[] headerBytes = new byte[8];
		headerBytes[0] = 'A';
		headerBytes[1] = 'M';
		headerBytes[2] = 'Q';
		headerBytes[3] = 'P';
		headerBytes[4] = 1;
		headerBytes[5] = 1;
		headerBytes[6] = 0;
		headerBytes[7] = 10;
		header.writeBytes(headerBytes);
		
		ChannelFuture cf = channel.write(header);
		cf.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
			    LOG.debug("AMQPVersionNegotiator :: Operation complete");
			    LOG.debug("AMQPVersionNegotiator :: Success = {}", future.isSuccess());
			}
		});
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if(done) {
			super.messageReceived(ctx, e);
		} else {
//			VersionMessage vm = (VersionMessage)e.getMessage();
		    LOG.debug("AMQPVersionNegotiator :: message received");
			ctx.getPipeline().addLast("amqpFrameDecoder", new AMQPFrameDecoder());
			ctx.getPipeline().addLast("segmentEncoder", new SegmentEncoder());
			ctx.getPipeline().addLast("controlMessageEncoder", new ProtocolMessageEncoder());
			ctx.getPipeline().addLast("handler", connectionImpl);
			ctx.getPipeline().remove(this);
			done = true;
		}
	}
	
	
}
