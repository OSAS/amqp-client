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

package com.redhat.osas.amqp.client.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import com.redhat.osas.amqp.client.*;
import com.redhat.osas.amqp.client.protocol.*;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.osas.amqp.client.Segment.Type;
import com.redhat.osas.amqp.client.api.Connection;
import com.redhat.osas.amqp.client.impl.SessionImpl.State;

public class ConnectionImpl extends SimpleChannelHandler {
    private final static Logger LOG = LoggerFactory.getLogger(ConnectionImpl.class);
    
	private final CountDownLatch connectionOpenLatch;
	
	//TODO support more than 1
	private final CountDownLatch sessionAttachLatch;
	
	Map<String, Assembly> messages = new HashMap<String, Assembly>();
	private ClientBootstrap bootstrap;
	private Channel channel;
//	private Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();
	private SessionImpl newSession = null;
	private SessionProxy sessionProxy;
	private ConnectionProxy connectionClassHandler;
	private Map<Integer, SessionImpl> sessions = new HashMap<Integer, SessionImpl>();
	private int nextChannel = 0;
	private Map<Integer, Map<Integer, ProtocolCallback>> callbacks = new HashMap<Integer, Map<Integer, ProtocolCallback>>();

    private Connection parent;
	
	public ConnectionImpl(Connection parent) {
	    this.parent = parent;
		connectionOpenLatch = new CountDownLatch(1);
		sessionAttachLatch = new CountDownLatch(1);
		
		registerCallback(ConnectionProxy.code, ConnectionProxy.code_start, connectionStartCallback);
		registerCallback(ConnectionProxy.code, ConnectionProxy.code_tune, connectionTuneCallback);
		registerCallback(ConnectionProxy.code, ConnectionProxy.code_open_ok, connectionOpenOkCallback);
		
		registerCallback(SessionProxy.code, SessionProxy.code_attached, sessionAttachedCallback);
		registerCallback(SessionProxy.code, SessionProxy.code_command_point, sessionCommandPointCallback);
		registerCallback(SessionProxy.code, SessionProxy.code_completed, sessionCompletedCallback);
		registerCallback(SessionProxy.code, SessionProxy.code_known_completed, sessionKnownCompletedCallback);
		registerCallback(SessionProxy.code, SessionProxy.code_flush, sessionFlushCallback);
		
		registerCallback(MessageProxy.code, MessageProxy.code_transfer, messageTransferCallback);
		
		connectionClassHandler = new ConnectionProxy();
		sessionProxy = new SessionProxy();
	}
	
	public Connection getConnection() {
	    return parent;
	}
	
	public void registerCallback(int messageClass, int methodCode, ProtocolCallback callback) {
        getClassCallbacks(messageClass).put(methodCode, callback);
    }
    
    private Map<Integer, ProtocolCallback> getClassCallbacks(int messageClass) {
        Map<Integer, ProtocolCallback> classCallbacks = callbacks.get(messageClass);
        if (null == classCallbacks) {
            classCallbacks = new HashMap<Integer, ProtocolCallback>();
            callbacks.put(messageClass, classCallbacks);
        }
        
        return classCallbacks;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        e.getCause().printStackTrace();
    }
    public void open(String host, int port) {
		ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		
		bootstrap = new ClientBootstrap(factory);
		
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(/*new LoggingHandler(true),*/
				                         new AMQPVersionFramer(),
										 new AMQPVersionNegotiator(ConnectionImpl.this));
			}
		});
		
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		
		channel = future.awaitUninterruptibly().getChannel();
        if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            bootstrap.releaseExternalResources();
            return;
        }
        
        try {
        	LOG.debug("Awaiting latch");
			connectionOpenLatch.await();
			LOG.debug("Latch cleared");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
	    // Close the connection.  Make sure the close operation ends because
	    // all I/O operations are asynchronous in Netty.
        channel.close().awaitUninterruptibly();

        // Shut down all thread pools to exit.
        bootstrap.releaseExternalResources();
	}
	
	private int getNextChannel() {
	    return nextChannel++;
	}
	
	public SessionImpl createSession(String sessionName) {
	    if(null == sessionName || sessionName.isEmpty()) {
	        sessionName = UUID.randomUUID().toString();
	    }
	    
	    SessionImpl session = new SessionImpl(this);
	    session.state = State.ATTACHING;
	    
	    int channelId = getNextChannel();
	    session.setChannelId(channelId);
	    sessions.put(channelId, session);
	    newSession = session;
	    
	    SessionAttachArguments arguments = new SessionAttachArguments();
	    arguments.setName(sessionName);
	    sessionProxy.attach(arguments, channel);
	    
	    SessionCommandPointArguments outArgs = new SessionCommandPointArguments();
        outArgs.setCommandId(0L);
        outArgs.setCommandOffset(0L);
        sessionProxy.command_point(outArgs, channel);
        
	    try {
	        sessionAttachLatch.await();
	        session.state = State.ATTACHED;
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	    
	    return session;
	}
	
	private ProtocolCallback connectionStartCallback = new ProtocolCallback() {
		public void execute(ProtocolMessage message, Channel channel) {
		    ConnectionStartArguments arguments = (ConnectionStartArguments) message.getArguments();
		    LOG.debug("In connection.start callback");
		    LOG.debug("Got arguments: {}", arguments);
			
			ConnectionStartOkArguments args = new ConnectionStartOkArguments();
			args.setClientProperties(new HashMap<String, Object>());
			args.setMechanism("ANONYMOUS");
			args.setResponse("");
			args.setLocale("en_US");
			
			connectionClassHandler.start_ok(args, channel);
		}
	};
	
	private ProtocolCallback connectionTuneCallback = new ProtocolCallback() {
		public void execute(ProtocolMessage message, Channel channel) {
		    ConnectionTuneArguments arguments = (ConnectionTuneArguments) message.getArguments();
		    
		    LOG.debug("In connection.tune callback");
		    LOG.debug("Got arguments: {}", arguments);
			
			ConnectionTuneOkArguments tuneOkArguments = new ConnectionTuneOkArguments();
			tuneOkArguments.setChannelMax(Math.min(arguments.getChannelMax(), 65536));
			tuneOkArguments.setMaxFrameSize(Math.min(arguments.getMaxFrameSize(), 65536));
			connectionClassHandler.tune_ok(tuneOkArguments, channel);
			
			ConnectionOpenArguments openArguments = new ConnectionOpenArguments();
			openArguments.setVirtualHost("");
			
			connectionClassHandler.open(openArguments, channel);
		};
	};
	
	private ProtocolCallback connectionOpenOkCallback = new ProtocolCallback() {
		public void execute(ProtocolMessage message, Channel channel) {
		    ConnectionOpenOkArguments arguments = (ConnectionOpenOkArguments) message.getArguments();
		    
		    LOG.debug("In connection.open-ok callback");
			LOG.debug("Got arguments: {}", arguments);
			
			connectionOpenLatch.countDown();
		};
	};
	
	private ProtocolCallback sessionAttachedCallback = new ProtocolCallback() {
	    public void execute(ProtocolMessage message, Channel channel) {
	        SessionAttachedArguments arguments = (SessionAttachedArguments) message.getArguments();
	        
	        LOG.debug("In session.attached callback");
	        LOG.debug("Got arguments: {}", arguments);
	    };
	};
	
	private ProtocolCallback sessionCommandPointCallback = new ProtocolCallback() {
	    public void execute(ProtocolMessage message, Channel channel) {
	        SessionCommandPointArguments arguments = (SessionCommandPointArguments) message.getArguments();
	        
	        LOG.debug("In session.command-point callback");
	        LOG.debug("Got arguments: {}", arguments);
	        
	        newSession.setPeerCommandId(arguments.getCommandId());
	        newSession.setCommandByteOffset(arguments.getCommandOffset());
	        
	        newSession = null;
	        
	        sessionAttachLatch.countDown();
	    };
	};
	
	private ProtocolCallback sessionCompletedCallback = new ProtocolCallback() {
        public void execute(ProtocolMessage message, Channel channel) {
            SessionCompletedArguments arguments = (SessionCompletedArguments) message.getArguments();
            
            LOG.debug("In session.completed callback");
            LOG.debug("Got arguments: {}", arguments);
            
            message.getSession().onCompleted(arguments);
        }
    };
    
    private ProtocolCallback sessionFlushCallback = new ProtocolCallback() {
        public void execute(ProtocolMessage message, Channel channel) {
            SessionFlushArguments arguments = (SessionFlushArguments) message.getArguments();
            
            LOG.debug("In session.flush callback");
            LOG.debug("Got arguments: {}", arguments);
            
            message.getSession().onFlush(arguments);
        }
    };
    
    private ProtocolCallback sessionKnownCompletedCallback = new ProtocolCallback() {
        public void execute(ProtocolMessage message, Channel channel) {
            SessionKnownCompletedArguments arguments = (SessionKnownCompletedArguments) message.getArguments();
            
            LOG.debug("In session.known-completed callback");
            LOG.debug("Got arguments: {}", arguments);
            
            message.getSession().onKnownCompleted(arguments);
        }
    };
	
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		LOG.debug("ConnectionImpl :: Message received");
		Frame f = (Frame)e.getMessage();
		String key = f.getChannel() + "." + f.getTrack();
		
		Assembly a = messages.get(key);
		if(f.isFirstFrame() && f.isFirstSegment()) {
			if (a != null) {
				throw new RuntimeException("Message collision - " + key + " already has a partial message!");
			}
			
			a = new Assembly();
			LOG.debug("Adding assembly for key {} to map", key);
			messages.put(key, a);
		}

		a.addFrame(f);
		if(f.isLastFrame() && f.isLastSegment()) {
			processMessage(a, e.getChannel());
			LOG.debug("Removing assembly for key {} to map", key);
			messages.remove(key);
		}
	}
	
	void processMessage(Assembly assembly, Channel channel) {
	    Segment segment = assembly.getSegments().get(0);
        ChannelBuffer cb = segment.getContent();
        
        Byte messageClass = cb.readByte();
        byte method = cb.readByte();
        
        Header header = null;
        if (Type.COMMAND == assembly.getMessageType()) {
            header = new Header();
            header.decode(cb);
            LOG.debug("Received command message");
        } else {
            LOG.debug("Received control message");
        }
        
        LOG.debug("class = {}, method = {}", messageClass, method);
        
        MethodArguments methodArguments = MethodArgumentsFactory.createFromBuffer((int) messageClass, (int) method, cb);
        
        //TODO set header on args if they should have it
        
        if (assembly.getSegments().size() > 1) {
            for (int i = 1; i < assembly.getSegments().size(); i++) {
                Segment s = assembly.getSegments().get(i);
                if (Segment.Type.HEADER == s.getType()) {
                    ChannelBuffer headerBuffer = s.getContent();
                    while(headerBuffer.readable()) {
                        long entrySize = headerBuffer.readUnsignedInt();
                        ChannelBuffer entryBuffer = headerBuffer.readBytes((int)entrySize);
                        short classCode = entryBuffer.readUnsignedByte();
                        short structCode = entryBuffer.readUnsignedByte();
                        
                        Struct struct = StructFactory.createFromBuffer((int)classCode, (int)structCode, entryBuffer);
                        
                        LOG.debug("Decoded struct: {}", struct);
                        methodArguments.setCodedStruct(structCode, struct);
                    }
                } else if (Segment.Type.BODY == s.getType()) {
                    ChannelBuffer bodyBuffer = s.getContent();
                    byte[] body = new byte[bodyBuffer.readableBytes()];
                    bodyBuffer.readBytes(body);
                    methodArguments.setBody(body);
                }
            }
            
        }
        
        int channelId = segment.getFrames().get(0).getChannel();
        SessionImpl amqpSession = sessions.get(channelId);
        
        ProtocolMessage message;
        if (Type.CONTROL == assembly.getMessageType()) {
            message = new ProtocolMessage(messageClass, method, methodArguments, MessageType.CONTROL);
        } else {
            message = new ProtocolMessage(messageClass, method, methodArguments, MessageType.COMMAND, amqpSession.getNextPeerCommandId());
            LOG.debug("Peer command message has id {}", message.getCommandId());
        }
        
        if (amqpSession != null) {
            message.setSession(amqpSession);
        }
        
        Map<Integer, ProtocolCallback> classCallbacks = getClassCallbacks(message.getMessageClass());
        ProtocolCallback methodCallback = classCallbacks.get(message.getMethodCode());
        
        if (methodCallback != null) {
            methodCallback.execute(message, channel);
        }
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		LOG.debug("ConnectionImpl :: Channel disconnected");
	}

    public Channel getChannel() {
        return channel;
    }
    
    private ProtocolCallback messageTransferCallback = new ProtocolCallback() {
        public void execute(ProtocolMessage command, Channel channel) {
            SessionImpl session = command.getSession();
            session.onMessageTransfer(command);
        }
    };
}
