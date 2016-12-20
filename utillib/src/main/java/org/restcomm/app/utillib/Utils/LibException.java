/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

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
