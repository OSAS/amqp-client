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

public class Frame {
	boolean firstSegment;
	boolean lastSegment;
	boolean firstFrame;
	boolean lastFrame;
	int segmentType;
	int frameSize;
	int track;
	int channel;
	ChannelBuffer payload;
	
	public Frame() {
	}
	
	public boolean isFirstSegment() {
		return firstSegment;  
	}
	
	public void setFirstSegment(boolean firstSegment) {
		this.firstSegment = firstSegment;
	}

	public boolean isLastSegment() {
		return lastSegment;
	}

	public void setLastSegment(boolean lastSegment) {
		this.lastSegment = lastSegment;
	}

	public boolean isFirstFrame() {
		return firstFrame;
	}

	public void setFirstFrame(boolean firstFrame) {
		this.firstFrame = firstFrame;
	}

	public boolean isLastFrame() {
		return lastFrame;
	}

	public void setLastFrame(boolean lastFrame) {
		this.lastFrame = lastFrame;
	}

	public int getSegmentType() {
		return segmentType;
	}

	public void setSegmentType(int segmentType) {
		this.segmentType = segmentType;
	}

	public int getFrameSize() {
		return frameSize;
	}

	public void setFrameSize(int frameSize) {
		this.frameSize = frameSize;
	}

	public int getTrack() {
		return track;
	}

	public void setTrack(int track) {
		this.track = track;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public ChannelBuffer getPayload() {
		return payload;
	}

	public void setPayload(ChannelBuffer payload) {
		this.payload = payload;
	}
}
