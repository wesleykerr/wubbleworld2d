package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.LinkedList;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class FSCState {
	protected LinkedList<FSCTransition> transitions;
	protected LinkedList<Action> actions;
	protected PhysicsObject owner;
	
	public FSCState(PhysicsObject owner){
		transitions = new LinkedList<FSCTransition>();
		actions = new LinkedList<Action>();
		this.owner = owner;
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
	
	public FSCState enter(FSCState prev, FSCTransition trans){
		// Initiate
		return this;
	}
	
	public FSCState exit(FSCState next, FSCTransition trans){
		// Finish up
		trans.transAction();
		return next.enter(this, trans);
	}
	
	public void action(){
		for(Action action : actions){
			if(action.check()){
				action.execute();
			}
		}
	}
	
	public FSCTransition check(){
		return null;
	}
	
	public FSCState update(int elapsed){
		FSCTransition trans = check();
		if(trans == null){
			action();
			return this;
		} else {
			return exit(trans.getNextState(),trans);
		}
	}
}
