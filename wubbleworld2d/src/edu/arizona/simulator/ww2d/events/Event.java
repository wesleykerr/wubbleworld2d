package edu.arizona.simulator.ww2d.events;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.simulator.ww2d.object.GameObject;

/**
 * This is the base class for all events.  These will be
 * passed around when different things occur.
 * @author wkerr
 *
 */
public class Event {
	private List<GameObject> _recipients;
	
	// TODO We may want to dispatch an event in the future.
	// private long _dispatchTime;
	
	public Event() { 
		_recipients = new ArrayList<GameObject>();
	}
	
	public Event(GameObject... objects) { 
		this();
		
		for (GameObject obj : objects)
			_recipients.add(obj);
	}
	
	/**
	 * Add a recipient to the list.s
	 * @param obj
	 */
	public void addRecipient(GameObject obj) { 
		_recipients.add(obj);
	}
	
	public List<GameObject> getRecipients() { 
		return _recipients;
	}
}
