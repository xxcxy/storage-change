package com.customtime.data.storagechange.service.exception;

public class OperatorException extends Exception{
	private static final long serialVersionUID = 1L;

	public OperatorException() {
		super();
	}

	public OperatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public OperatorException(String message) {
		super(message);
	}

	public OperatorException(Throwable cause) {
		super(cause);
	}
}
