package edu.arizona.simulator.ww2d.events.player;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Behavior;

public class BehaviorEvent extends Event {

	private Class<? extends Behavior> _class;
	private boolean _status;

	private Object _target;
	
	public BehaviorEvent(Class<? extends Behavior> bClass, boolean status) { 
		_class = bClass;
		_status = status;
	}

	public BehaviorEvent(Class<? extends Behavior> bClass, boolean status, GameObject... objects) { 
		super(objects);
		
		_class = bClass;
		_status = status;
	}
	
	public void setTarget(Object target) { 
		_target = target;
	}
	

	public Class<? extends Behavior> getBehaviorClass() { 
		return _class;
	}
	
	public boolean getState() { 
		return _status;
	}
	
	public Object getTarget() { 
		return _target;
	}
}
