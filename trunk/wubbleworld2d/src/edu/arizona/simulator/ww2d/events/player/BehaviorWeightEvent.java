package edu.arizona.simulator.ww2d.events.player;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Behavior;

public class BehaviorWeightEvent extends Event {
	private Class<? extends Behavior> _class;
	private float _weight;

	public BehaviorWeightEvent(Class<? extends Behavior> bClass, float weight) { 
		_class = bClass;
		_weight = weight;
	}

	public BehaviorWeightEvent(Class<? extends Behavior> bClass, float weight, GameObject... objects) { 
		super(objects);

		_class = bClass;
		_weight = weight;
	}
	
	public Class<? extends Behavior> getBehaviorClass() { 
		return _class;
	}
	
	public float getWeight() { 
		return _weight;
	}
}
