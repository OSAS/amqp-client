package com.amentra.amqp.client;

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
