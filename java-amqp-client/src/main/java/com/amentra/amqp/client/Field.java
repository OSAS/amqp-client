package com.amentra.amqp.client;

public class Field {
	String name;
	Type type;
	boolean required;

	public Field() {
	}
	
	public Field(String name, Type type, boolean required) {
		this.name = name;
		this.type = type;
		this.required = required;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
}
