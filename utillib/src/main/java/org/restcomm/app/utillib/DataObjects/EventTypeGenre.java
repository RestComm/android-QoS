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

package org.restcomm.app.utillib.DataObjects;

/**
 * All event types can belong to any one of a number of genres. This enum
 * encapsulates those genres.
 * @author Abhin
 *
 */
public enum EventTypeGenre {
	/**
	 * This is the genre of event types that exist all by themselves.
	 */
	SINGLETON,
	
	/**
	 * This is the genre of events that exist in a couple and form its starting section.
	 */
	START_OF_COUPLE,
	
	/**
	 * This is the genre of events that exist in a couple and form its ending section.
	 */
	END_OF_COUPLE
}
