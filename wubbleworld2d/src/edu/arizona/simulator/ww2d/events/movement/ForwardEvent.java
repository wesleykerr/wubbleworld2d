package edu.arizona.simulator.ww2d.events.movement;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class ForwardEvent extends Event {
	
	private boolean _state;
	
	public ForwardEvent(boolean state) { 
		_state = state;
	}
	
	public ForwardEvent(boolean state, GameObject... objects) {
		super(objects);
		
		_state = state;
	}
	
	public boolean getState() { 
		return _state;
	}
}
