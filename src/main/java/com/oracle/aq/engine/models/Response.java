package com.oracle.aq.engine.models;

import org.springframework.stereotype.Component;

@Component
public class Response {

	private String code;
	private String message;
	private String description;

	public Response() {

	}

	public Response(String code, String message, String description) {
		super();
		this.code = code;
		this.message = message;
		this.description = description;
	}

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Response { code=" + code + ", message=" + message + ", description=" + description + "}";
	}



}
