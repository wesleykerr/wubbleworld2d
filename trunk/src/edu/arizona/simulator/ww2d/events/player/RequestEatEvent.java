package edu.arizona.simulator.ww2d.events.player;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class RequestEatEvent extends Event {
	private GameObject _requestor;
	
	public RequestEatEvent(GameObject requestor) { 
		_requestor = requestor;
	}

	public RequestEatEvent(GameObject requestor, GameObject... objects) { 
		super(objects);
		
		_requestor = requestor;
	}
	
	public GameObject getRequestor() { 
		return _requestor;
	}
}
