package edu.arizona.simulator.ww2d.events.spawn;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class RemoveGameObject extends Event {

	private GameObject _object;
	
	public RemoveGameObject(GameObject object) { 
		super();
		
		_object = object;
	}
	
	/**
	 * Return the GameObject we plan on removing.
	 * @return
	 */
	public GameObject getGameObject() { 
		return _object;
	}
}
