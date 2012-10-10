amqp-client
===========

This is the source code for a Java-based AMQP client, nominally targeted at
[Apache Qpid](http://qpid.apache.org). It is not based on JMS, and is suitable for use
on the Android platform.

An example of creating a queue receiver can be found in
[ReceiveOneMessage.java](https://github.com/jottinger/amqp-client/blob/master/java-amqp-client/src/test/java/com/redhat/osas/amqp/client/ReceiveOneMessage.java):

    import com.redhat.osas.amqp.client.api.Connection;
    import com.redhat.osas.amqp.client.api.Message;
    import com.redhat.osas.amqp.client.api.Receiver;
    import com.redhat.osas.amqp.client.api.Session;

    ...

    Connection c = new Connection("127.0.0.1", (short)5672);
    c.open();
    Session session = c.createSession(UUID.randomUUID().toString());
    Receiver receiver=session.createReceiver("test");

    Message message=receiver.fetch();
    // message.getContent() contains the body content of the message.

    receiver.close();
    session.close();

Sending a message is similar, and code for this exists in
[SendOneMessage.java](https://github.com/jottinger/amqp-client/blob/master/java-amqp-client/src/test/java/com/redhat/osas/amqp/client/SendOneMessage.java):

    import com.redhat.osas.amqp.client.api.Connection;
    import com.redhat.osas.amqp.client.api.Message;
    import com.redhat.osas.amqp.client.api.Sender;
    import com.redhat.osas.amqp.client.api.Session;
    import com.redhat.osas.amqp.client.impl.MessageImpl;

    ...

    Connection c = new Connection(qpidServerIp, (short) 5672);
    c.open();
    Session session = c.createSession(sessionId);

    Sender sender = session.createSender("");
    sender.setCapacity(10000);
    Message message = new MessageImpl();
    message.setSubject(queueName);
    message.setTtl(86400L);

    // could be a byte[] as well
    message.setContent("hello, world");

    sender.send(message);
    session.sync();

    session.close();

It's fully conceivable that a wrapper could be written to isolate much of this away, much as this is
a wrapper for the underlying AMQP protocol.

There is a script in `java-amqp-client/src/test/scripts/qpidd-test-server.sh` which provides for a full
end-to-end test of the AMQP client. To use it, follow this process, in the project home directory:

`**$** mvn -DskipTests=true package dependency:copy-dependencies`
`**$** cd java-amqp-client/src/test/scripts`
`**$** ./qpidd-test-server.sh`

This will build the AMQP client, including copying the dependencies into the build structure. It then
starts `qpidd` as a daemon, creates a queue, then executes the two test classes in sequence (receive,
then send); then it shuts down the container.