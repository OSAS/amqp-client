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

public class VersionMessage {
	String protocol;
	short protocolClass;
	short protocolInstance;
	short majorVersionSupported;
	short minorVersionSupported;

	public VersionMessage() {
	}
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public short getProtocolClass() {
		return protocolClass;
	}

	public void setProtocolClass(short protocolClass) {
		this.protocolClass = protocolClass;
	}

	public short getProtocolInstance() {
		return protocolInstance;
	}

	public void setProtocolInstance(short protocolInstance) {
		this.protocolInstance = protocolInstance;
	}

	public short getMajorVersionSupported() {
		return majorVersionSupported;
	}

	public void setMajorVersionSupported(short majorVersionSupported) {
		this.majorVersionSupported = majorVersionSupported;
	}

	public short getMinorVersionSupported() {
		return minorVersionSupported;
	}

	public void setMinorVersionSupported(short minorVersionSupported) {
		this.minorVersionSupported = minorVersionSupported;
	}

}
