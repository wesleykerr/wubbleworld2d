package edu.arizona.simulator.ww2d.utils.enums;

public enum EventType {
	/**
	 * KINEMATIC_EVENT
	 *  Payload:
	 *      target - the target location
	 *      movement - The kinematic movement class
	 */
	KINEMATIC_EVENT,
			
	/**
	 * CONSOLE_MESSAGE
	 *   Payload:
	 *   	object - the game object that the message pertains to
	 *   	message - the string containing the message
	 */
	CONSOLE_MESSAGE,

	/**
	 * CHANGE_CAMERA_FOLLOWING
	 * 	 Payload:
	 * 		previous-object - the old object that the camera was centered on
	 * 		new-object - the new object that the camera will be centered on
	 */
	CHANGE_CAMERA_FOLLOWING,
	
}
