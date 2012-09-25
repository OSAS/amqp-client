package com.amentra.amqp.client;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amentra.amqp.client.api.Connection;
import com.amentra.amqp.client.api.Message;
import com.amentra.amqp.client.api.Sender;
import com.amentra.amqp.client.api.Session;
import com.amentra.amqp.client.impl.MessageImpl;

public class ConnectionTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void doIt() throws Exception {
		System.out.println("doIt :: begin");
		Connection c = new Connection("192.168.1.115", (short)5672);
		c.open();
		Session session = c.createSession(UUID.randomUUID().toString());
		
		Sender sender = session.createSender("");
		sender.setCapacity(10000);
		Message message = new MessageImpl();
		message.setSubject("foo");
//		message.setTtl(86400L);
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
		System.out.println("Syncing");
		session.sync();
		long end = System.nanoTime();
		System.out.println("Done syncing");
		
		System.out.println(end - start);
		double duration = end - start;
		duration /= 1000000000;
		double messagesPerSecond = numMessages / (duration);
		System.out.println(messagesPerSecond);
//		Receiver receiver = session.createReceiver("foo");
		
//		message = receiver.fetch();
		
//		receiver.setCapacity(1);
//		MessageTransferArguments messageTransferArguments = receiver.get();
//		session.acknowledge(0);
		
		
//		receiver.close();
		session.close();
		
		System.out.println("doIt :: end");
	}
}
