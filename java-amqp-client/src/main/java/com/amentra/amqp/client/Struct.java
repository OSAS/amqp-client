package com.amentra.amqp.client;

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
