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
