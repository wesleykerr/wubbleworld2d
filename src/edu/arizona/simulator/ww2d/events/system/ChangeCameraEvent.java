package edu.arizona.simulator.ww2d.events.system;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class ChangeCameraEvent extends Event {
	private PhysicsObject _fromObject;
	private PhysicsObject _toObject;
	
	public ChangeCameraEvent(PhysicsObject from, PhysicsObject to) { 
		super();
		
		_fromObject = from;
		_toObject = to;
	}
	
	public PhysicsObject getFromObject() { 
		return _fromObject;
	}
	
	public PhysicsObject getToObject() { 
		return _toObject;
	}
}
