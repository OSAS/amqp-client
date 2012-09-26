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

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.osas.amqp.client.api.Connection;
import com.redhat.osas.amqp.client.api.Message;
import com.redhat.osas.amqp.client.api.Sender;
import com.redhat.osas.amqp.client.api.Session;
import com.redhat.osas.amqp.client.impl.MessageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessageTest {
    Logger logger= LoggerFactory.getLogger(this.getClass());

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void doIt() throws Exception {
		logger.info("begin");
		Connection c = new Connection("192.168.1.115", (short)5672);
		c.open();
		Session session = c.createSession(UUID.randomUUID().toString());
		
		Sender sender = session.createSender("");
		sender.setCapacity(10000);
		Message message = new MessageImpl();
		message.setSubject("test");
		message.setTtl(86400L);
//		message.setContent("hello, world");
		byte[] content = new byte[1024];
		for(int i = 0; i < 1024; i++) {
		    content[i] = 'x';
		}
		message.setContent(content);
		
		long start = System.nanoTime();
		double numMessages = 25000.0;
		for (int i = 0; i < numMessages; i++) {
		    sender.send(message);
		}
		logger.info("Syncing");
		session.sync();
		long end = System.nanoTime();
		logger.info("Done syncing");
		
		logger.info("Time: " + (end - start));
		double duration = end - start;
		duration /= 1000000000;
		double messagesPerSecond = numMessages / (duration);
		logger.info("Messages per second: "+messagesPerSecond);
//		Receiver receiver = session.createReceiver("foo");
		
//		message = receiver.fetch();
		
//		receiver.setCapacity(1);
//		MessageTransferArguments messageTransferArguments = receiver.get();
//		session.acknowledge(0);
		
		
//		receiver.close();
		session.close();
		
		logger.info("end");
	}
}
