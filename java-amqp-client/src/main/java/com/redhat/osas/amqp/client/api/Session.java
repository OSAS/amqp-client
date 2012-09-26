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

package com.redhat.osas.amqp.client.api;

public interface Session {
    void close();
    void commit();
    void rollback();
    void acknowledge();
    void acknowledge(boolean sync);
    void acknowledge(Message message);
    void acknowledge(Message message, boolean sync);
    void acknowledgeThrough(Message message);
    void acknowledgeThrough(Message message, boolean sync);
    void reject(Message message);
    void release(Message message);
    void sync();
    void sync(boolean block);
    long getReceivable();
    long getUnsettledAcks();
    Receiver nextReceiver();
    Receiver nextReceiver(long timeout);
    Sender createSender(String address);
    Receiver createReceiver(String address);
    Sender getSender(String address);
    Receiver getReceiver(String address);
    Connection getConnection();
    boolean hasError();
    void checkError();
}
