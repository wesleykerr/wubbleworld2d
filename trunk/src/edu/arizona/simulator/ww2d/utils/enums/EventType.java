package edu.arizona.simulator.ww2d.utils.enums;

public enum EventType {
	/**
	 * ENERGY_EVENT
	 * 	Payload:
	 * 		amount - the amount of energy to add or remove.
	 */
	ENERGY_EVENT,
	
	/**
	 * REMOVE_OBJECT_EVENT
	 * 	Payload:
	 *      object - the actual object we would like to remove
	 */
	REMOVE_OBJECT_EVENT,
	
	/**
	 * COLLISION_EVENT
	 * 	Payload:
	 * 		contact-point - the contact information stored as a ContactPoint
	 *      type - add,persist,remove
	 */
	COLLISION_EVENT,

	/**
	 * CONTACT_RESULT_EVENT
	 *  Payload:
	 *  	contact-result - the result information stored as a ContactResult
	 */
	CONTACT_RESULT_EVENT,

	/**
	 * KINEMATIC_EVENT
	 *  Payload:
	 *      target - the target location
	 *      movement - The kinematic movement class
	 */
	KINEMATIC_EVENT,
	
	/**
	 * FORWARD_EVENT
	 *   Payload:
	 *     state - start or end event
	 */
	FORWARD_EVENT,

	/**
	 * BACKWARD_EVENT
	 *   Payload:
	 *     state - start or end event
	 */
	BACKWARD_EVENT,
	
	/**
	 * LEFT_EVENT
	 *   Payload:
	 *     state - start or end event
	 */
	LEFT_EVENT,
	
	/**
	 * RIGHT_EVENT
	 *   Payload:
	 *     state - start or end event
	 */
	RIGHT_EVENT,
	
	/**
	 * STRAFE_LEFT_EVENT
	 *   Payload:
	 *     state - start or end event
	 */
	STRAFE_LEFT_EVENT,
	
	/**
	 * STRAFE_RIGHT_EVENT
	 *   Payload:
	 *     state - start or end event
	 */
	STRAFE_RIGHT_EVENT,
	
	/**
	 * CHANGE_CONTROL_EVENT
	 *   Payload: 
	 */
	CHANGE_CONTROL_EVENT,
	
	/**
	 * BEHAVIOR_EVENT
	 *   Payload:
	 *     name - the name of the class
	 *     status - turn on or turn off
	 *     target - contains the target to align towards
	 */
	BEHAVIOR_EVENT,
	
	/**
	 * BEHAVIOR_WEIGHT_EVENT
	 *   Payload:
	 *     name - the name of the class
	 *     weight - the weight value
	 */
	BEHAVIOR_WEIGHT_EVENT,
	
	/**
	 * UPDATE_START
	 *   Payload:
	 */
	UPDATE_START,
	
	/**
	 * UPDATE_END
	 *   Payload:
	 */
	UPDATE_END,
	
	/**
	 * FINISH
	 *   Payload:
	 */
	FINISH,
	
	/**
	 * REQUEST_EAT
	 *   Payload:
	 *     requestor - the physics object requesting to eat
	 */
	REQUEST_EAT,
	
	/**
	 * CREATE_GAME_OBJECT
	 *   Payload:
	 *     element - the XML element that contains the initialization information
	 */
	CREATE_GAME_OBJECT,

	/**
	 * CREATE_PHYSICS_OBJECT
	 *   Payload:
	 *     element - the XML element that contains the initialization information
	 */
	CREATE_PHYSICS_OBJECT,
	
	/**
	 * CREATE_WALL
	 *   Payload:
	 *      name - the name of the wall
	 *      position - the x,y location of the wall
	 *      dimensions - the width and height of the wall
	 */
	CREATE_WALL,

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
	
	/**
	 * SET_WAYPOINTS
	 *   Payload:
	 *     list of waypoints.
	 */
	SET_WAYPOINTS,
	
	/**
	 * SET_WAYPOINTS_AND_TIMES
	 *   Payload:
	 *     list of waypoints.
	 *     list of the amount of time to spend at each waypiont
	 */
	SET_WAYPOINTS_AND_TIMES,
}
