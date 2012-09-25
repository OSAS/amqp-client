package com.amentra.amqp.client;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class Segment {
	public enum Type {
		CONTROL(0),
		COMMAND(1),
		HEADER(2),
		BODY(3);
		
		private int value;
		
		Type(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		private static final Map<Integer,Type> lookup = new HashMap<Integer,Type>();

		static {
			for (Type t : EnumSet.allOf(Type.class))
				lookup.put(t.getValue(), t);
		}
		
		public static Type get(int value) {
			return lookup.get(value);
		}
	}
	
	List<Frame> frames = new ArrayList<Frame>();
	Type type;
	
	public Segment() {
	}
	
	public Segment(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public void addFrame(Frame frame) {
		frames.add(frame);
	}
	
	public ChannelBuffer getContent() {
		ChannelBuffer cb = ChannelBuffers.dynamicBuffer();
		for (Frame frame : frames) {
			cb.writeBytes(frame.getPayload(), frame.getFrameSize() - 12);
		}
		return cb;
	}

	public List<Frame> getFrames() {
		return frames;
	}

	public void setFrames(List<Frame> frames) {
		this.frames = frames;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
