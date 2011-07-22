package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.LinkedList;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public abstract class Action {
	protected LinkedList<Check> conditions;
	protected PhysicsObject owner;
	public abstract void execute(int elapsed);
	
	public final boolean check(int elapsed){
		if(conditions == null || conditions.isEmpty()){
			return true;
		}
		
		for(Check check : conditions){
			if(!check.check(elapsed)){
				return false;
			}
		}
		
		return true;
	}
	
	public Action(PhysicsObject owner){
		this.owner = owner;
		conditions = new LinkedList<Check>();
	}
	
	public void addCheck(Check toAdd){
		conditions.add(toAdd);
	}
}
