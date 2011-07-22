package edu.arizona.simulator.ww2d.events.system;

import org.jbox2d.dynamics.contacts.ContactResult;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class ContactEvent extends Event {

	private ContactResult _contactResult;
	
	private PhysicsObject _obj1;
	private PhysicsObject _obj2;
	
	public ContactEvent(ContactResult contactResult, PhysicsObject obj1, PhysicsObject obj2) { 
		super();
		
		addRecipient(obj1);
		addRecipient(obj2);
		
		_contactResult = contactResult;
		
		_obj1 = obj1;
		_obj2 = obj2;
	}
	

	public ContactResult getContactResult() { 
		return _contactResult;
	}
	
	public PhysicsObject getPhysicsObject1() { 
		return _obj1;
	}
	
	public PhysicsObject getPhysicsObject2() { 
		return _obj2;
	}
}
