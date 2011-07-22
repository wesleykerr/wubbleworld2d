package edu.arizona.simulator.ww2d.events.system;

import org.jbox2d.dynamics.contacts.ContactPoint;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class CollisionEvent extends Event {
	
	private ContactPoint _contactPoint;
	private String _type;
	
	private PhysicsObject _obj1;
	private PhysicsObject _obj2;
	
	public CollisionEvent(ContactPoint cp, String type, PhysicsObject obj1, PhysicsObject obj2) { 
		super();
		
		addRecipient(obj1);
		addRecipient(obj2);
		
		_contactPoint = cp;
		_type = type;
		
		_obj1 = obj1;
		_obj2 = obj2;
	}
	
	public ContactPoint getContactPoint() { 
		return _contactPoint;
	}
	
	public String getType() { 
		return _type;
	}
	
	public PhysicsObject getPhysicsObject1() { 
		return _obj1;
	}
	
	public PhysicsObject getPhysicsObject2() { 
		return _obj2;
	}
}
