package com.redhat.osas.amqp.client;

import java.util.ArrayList;
import java.util.List;

public class Assembly {
	List<Segment> segments = new ArrayList<Segment>();
	
	public void addFrame(Frame frame) {
		Segment segment;
		
		if (frame.isFirstFrame()) {
			segment = new Segment(Segment.Type.get(frame.getSegmentType()));
			addSegment(segment);
		} else {
			segment = segments.get(segments.size()-1);
		}
		
		segment.addFrame(frame);
	}
	
	public List<Segment> getSegments() {
		return segments;
	}
	
	public void addSegment(Segment segment) {
		segments.add(segment);
	}
	
	public Segment.Type getMessageType() {
	    return segments.get(0).getType();
	}
}
