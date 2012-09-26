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
