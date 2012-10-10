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

import com.redhat.osas.amqp.client.api.Connection;
import com.redhat.osas.amqp.client.api.Message;
import com.redhat.osas.amqp.client.api.Receiver;
import com.redhat.osas.amqp.client.api.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ReceiveOneMessage implements Runnable {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        new ReceiveOneMessage().run();
    }
    public void run() {
        logger.info("begin");
        Connection c = new Connection("127.0.0.1", (short)5672);
        c.open();
        Session session = c.createSession(UUID.randomUUID().toString());
        Receiver receiver=session.createReceiver("test");

        Message message=receiver.fetch();
        System.out.println(new String(message.getContent()));
        receiver.close();
        session.close();
        logger.info("end");
    }
}
