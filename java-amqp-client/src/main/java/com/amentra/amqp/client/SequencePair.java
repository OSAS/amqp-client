package com.amentra.amqp.client;

public class SequencePair {
    private long begin;
    private long end;
    
    public SequencePair(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
