package com.redhat.osas.amqp.client;

public class KeyType {
	public String key;
	public Type type;
	
	public KeyType(String key, Type type) {
		this.key = key;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return key + "[" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof KeyType)) {
			return false;
		}
		KeyType other = (KeyType) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}
}	
