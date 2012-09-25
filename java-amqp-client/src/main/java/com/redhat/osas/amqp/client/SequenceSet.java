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
