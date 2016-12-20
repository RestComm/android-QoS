package org.restcomm.app.utillib.Utils;

/**
 * Simple MMCException to be thrown by mmc application
 * @author estebanginez
 *
 */
public class LibException extends Exception {

	private static final long serialVersionUID = 5465192594071893603L;
	/**
	 * Empty constructor
	 */
	public LibException() {
		super();
	}
	
	/**
	 * Creates an object based on a previos exception
	 * @param e the root exception
	 */
	public LibException(Exception e) {
		super(e);
	}
	
	/**
	 * Creates an exception with the given message
	 * @param cause A message identifying the cause of the exception
	 */
	public LibException(String cause) {
		super(cause);
	}
}
