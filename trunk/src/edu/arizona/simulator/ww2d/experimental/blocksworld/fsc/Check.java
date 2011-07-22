package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.LinkedList;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public abstract class Check {
	protected PhysicsObject owner;
	
	public Check(PhysicsObject owner){
		this.owner = owner;
	}
	public abstract boolean check(int elapsed);
	
	public abstract void reset();
	
	public LinkedList<String> requiredFields(){
		return new LinkedList<String>();
	}
}
