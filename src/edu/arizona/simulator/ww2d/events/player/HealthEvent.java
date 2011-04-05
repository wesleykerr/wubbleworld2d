package edu.arizona.simulator.ww2d.events.player;

import edu.arizona.simulator.ww2d.events.Event;

public class HealthEvent extends Event {

	public float _amount;
	
	public HealthEvent(float amount) { 
		_amount = amount;
	}
	
	public float getAmount() { 
		return _amount;
	}
}
