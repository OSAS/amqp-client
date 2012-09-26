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

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class Struct {
//    protected long size;
//    protected short classCode;
//    protected short structCode;
//    protected short pack;
    protected long packFlags;
    
    public abstract void encode(ChannelBuffer buffer);
    public abstract void decode(ChannelBuffer buffer);
    
//    public Struct(short size, short classCode, short structCode, short pack) {
//        this.size = size;
//        this.classCode = classCode;
//        this.structCode = structCode;
//        this.pack = pack;
//    }

//    public long getSize() {
//        return size;
//    }
//
//    public void setSize(long size) {
//        this.size = size;
//    }
//
//    public short getClassCode() {
//        return classCode;
//    }
//
//    public void setClassCode(short classCode) {
//        this.classCode = classCode;
//    }
//
//    public short getStructCode() {
//        return structCode;
//    }
//
//    public void setStructCode(short structCode) {
//        this.structCode = structCode;
//    }
//
//    public short getPack() {
//        return pack;
//    }
//
//    public void setPack(short pack) {
//        this.pack = pack;
//    }

    public long getPackFlags() {
        return packFlags;
    }

    public void setPackFlags(long packFlags) {
        this.packFlags = packFlags;
    }
}
