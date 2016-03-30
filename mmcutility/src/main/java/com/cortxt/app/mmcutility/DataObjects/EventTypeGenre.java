package com.cortxt.app.mmcutility.DataObjects;

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
