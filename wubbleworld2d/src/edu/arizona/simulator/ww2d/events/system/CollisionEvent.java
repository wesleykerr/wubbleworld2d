package edu.arizona.simulator.ww2d.events.system;

import org.jbox2d.dynamics.contacts.ContactPoint;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class CollisionEvent extends Event {
	
	private ContactPoint _contactPoint;
	private String _type;
	
	public CollisionEvent(ContactPoint cp, String type) { 
		super();
		
		_contactPoint = cp;
		_type = type;
	}
	
	public CollisionEvent(ContactPoint cp, String type, GameObject... objects) { 
		super(objects);
		
		_contactPoint = cp;
		_type = type;
	}
	
	public ContactPoint getContactPoint() { 
		return _contactPoint;
	}
	
	public String getType() { 
		return _type;
	}
}
