package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public abstract class Action {
	protected PhysicsObject owner;
	public abstract boolean check();
	public abstract void execute();
	
	public Action(PhysicsObject owner){
		this.owner = owner;
	}
}
