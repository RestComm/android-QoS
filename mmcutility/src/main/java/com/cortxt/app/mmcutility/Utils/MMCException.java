package com.cortxt.app.mmcutility.Utils;

/**
 * Simple MMCException to be thrown by mmc application
 * @author estebanginez
 *
 */
public class MMCException extends Exception {

	private static final long serialVersionUID = 5465192594071893603L;
	/**
	 * Empty constructor
	 */
	public MMCException() {
		super();
	}
	
	/**
	 * Creates an object based on a previos exception
	 * @param e the root exception
	 */
	public MMCException(Exception e) {
		super(e);
	}
	
	/**
	 * Creates an exception with the given message
	 * @param cause A message identifying the cause of the exception
	 */
	public MMCException(String cause) {
		super(cause);
	}
}
