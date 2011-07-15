package edu.arizona.simulator.ww2d.events.player;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class EnergyEvent extends Event {

	public float _amount;
	
	public EnergyEvent(float amount) { 
		_amount = amount;
	}
	
	public EnergyEvent(float amount, GameObject... objects) { 
		super(objects);
		
		_amount = amount;
	}
	
	public float getAmount() { 
		return _amount;
	}
}
