package edu.arizona.simulator.ww2d.events.movement;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class StrafeRightEvent extends Event {
	
	private boolean _state;
	
	public StrafeRightEvent(boolean state) { 
		_state = state;
	}
	
	public StrafeRightEvent(boolean state, GameObject... objects) {
		super(objects);
		
		_state = state;
	}
	
	public boolean getState() { 
		return _state;
	}
}
