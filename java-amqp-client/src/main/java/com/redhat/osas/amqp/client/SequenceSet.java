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

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

public class SequenceSet {
    private List<SequencePair> pairs = new ArrayList<SequencePair>();
    
    public void add(SequencePair pair) {
        pairs.add(pair);
    }
    
    public void encode(ChannelBuffer buffer) {
        buffer.writeShort(8 * pairs.size());
        for (SequencePair pair : pairs) {
            buffer.writeInt((int)pair.getBegin());
            buffer.writeInt((int)pair.getEnd());
        }
    }
    
    public void decode(ChannelBuffer buffer) {
        int count = buffer.readUnsignedShort() / 8;
        
        for (int i = 0; i < count; i++) {
            int begin = buffer.readInt();
            int end = buffer.readInt();
            SequencePair pair = new SequencePair(begin, end);
            add(pair);
        }
    }
    
    @Override
    public String toString() {
        String s = "SequenceSet[";
        for (SequencePair pair : pairs) {
            s += "(" + pair.getBegin() + ", " + pair.getEnd() + "), ";
        }
        s += "]";
        return s;
    }
    
    public List<SequencePair> getPairs() {
        return pairs;
    }
}
