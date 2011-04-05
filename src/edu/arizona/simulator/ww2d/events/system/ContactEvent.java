package edu.arizona.simulator.ww2d.events.system;

import org.jbox2d.dynamics.contacts.ContactResult;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class ContactEvent extends Event {

	private ContactResult _contactResult;
	
	public ContactEvent(ContactResult contactResult) { 
		_contactResult = contactResult;
	}
	
	public ContactEvent(ContactResult contactResult, GameObject... objects) { 
		super(objects);
		
		_contactResult = contactResult;
	}

	public ContactResult getContactResult() { 
		return _contactResult;
	}
}
