package com.amentra.amqp.client;

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
