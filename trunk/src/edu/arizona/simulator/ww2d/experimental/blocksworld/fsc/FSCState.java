package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.LinkedList;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class FSCState {
	protected LinkedList<FSCTransition> transitions;
	protected LinkedList<Action> actions;
	protected PhysicsObject owner;
	private FSCTransition nullTrans;
	
	public FSCState(PhysicsObject owner){
		transitions = new LinkedList<FSCTransition>();
		actions = new LinkedList<Action>();
		this.owner = owner;
		nullTrans = new FSCTransition(owner);
	}

	public PhysicsObject getOwner(){
		return owner;
	}
	
	
	public void addTransition(FSCTransition trans){
		transitions.add(trans);
	}
	
	public void removeTransition(FSCTransition trans){
		transitions.remove(trans);
	}
	
	public void purgeTransitions(){
		transitions = new LinkedList<FSCTransition>();
	}
	
	public void addAction(Action action){
		actions.add(action);
	}
	
	public void removeAction(Action action){
		actions.remove(action);
	}
	
	public void purgeActions(){
		actions = new LinkedList<Action>();
	}
	
	public FSCState enter(FSCState prev, FSCTransition trans, int elapsed){
		// Initiate
		for(Action action : actions){
			action.execute(elapsed);
		}
		return this;
	}
	
	public FSCState exit(FSCState next, FSCTransition trans, int elapsed){
		// Finish up
		trans.transAction(0);
		trans.resetChecks();
		trans.migrateRequiredData(this);
		return next.enter(this, trans, elapsed);
	}
	
	public void action(int elapsed){
		for(Action action : actions){
			if(action.check(elapsed)){
				action.execute(elapsed);
			}
		}
	}
	
	public FSCTransition check(int elapsed){
		for(FSCTransition trans : transitions){
			if(trans.check(elapsed)){
				return trans;
			}
		}
		
		return null;
	}
	
	public FSCState update(int elapsed){
		FSCTransition trans = check(elapsed);
		if(trans == null){
			return this.enter(this, nullTrans, elapsed);
		} else {
			return exit(trans.getNextState(),trans, elapsed);
		}
	}
}
