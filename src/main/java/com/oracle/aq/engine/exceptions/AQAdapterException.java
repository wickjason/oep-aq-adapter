package com.oracle.aq.engine.exceptions;

public class AQAdapterException extends Exception {

	  private static final long serialVersionUID = -1246355274460089403L;

	  public AQAdapterException(final String message) {
	    super(message);
	  }

	  public AQAdapterException(final Throwable cause) {
	    super(cause);
	  }

	  public AQAdapterException(final String message, final Throwable cause) {
	    super(message, cause);
	  }
	}
