package edu.arizona.simulator.ww2d.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.utils.enums.EventType;

/**
 * This is the base class for all events.  These will be
 * passed around when different things occur.
 * @author wkerr
 *
 */
public class Event {

	private EventType _id;	
	private List<GameObject> _recipients;
	
	
	// TODO We may want to dispacth an event in the future.
	// private long _dispatchTime;
	
	private Map<String,Object> _parameters;
	
	public Event(EventType id) { 
		_id = id;
		_recipients = new ArrayList<GameObject>();
		_parameters = new HashMap<String,Object>();
	}
	
	/**
	 * Return the ID of this event.
	 * @return
	 */
	public EventType getId() { 
		return _id;
	}
	
	/**
	 * Return the string representation of this Event
	 */
	public String toString() { 
		StringBuffer buf = new StringBuffer();
		buf.append(_id + "\n");
		for (GameObject obj : _recipients) { 
			buf.append("\t" + obj.getName() + "\n");
		}
		
		for (Map.Entry<String,Object> entry : _parameters.entrySet()) { 
			buf.append("\t\t" + entry.getKey() + " : " + entry.getValue() + "\n");
		}
		return buf.toString();
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
	
	/**
	 * Add the parameter to the list of parameters
	 * for this event.
	 * @param name
	 * @param obj
	 */
	public void addParameter(String name, Object obj) { 
		_parameters.put(name, obj);
	}
	
	/**
	 * Get the value associated with given parameter.
	 * @param name
	 * @return
	 */
	public Object getValue(String name) {
		return _parameters.get(name);
	}
}
