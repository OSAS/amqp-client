package com.amentra.amqp.client;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

public interface MethodArguments {
    void encode(ChannelBuffer buffer);
    void decode(ChannelBuffer buffer);
    List<Struct> getHeaders();
    boolean hasBody(); //TODO find a better way to do this
    byte[] getBody();
    void setBody(byte[] body);
    Struct getCodedStruct(short code);
    void setCodedStruct(short code, Struct struct);
}
