package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.LinkedList;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class FSCTransition {
	FSCState next;
	LinkedList<Action> checks;
	LinkedList<Action> transActions;
	PhysicsObject owner;
	
	public FSCTransition(PhysicsObject owner){
		next = null;
		checks = new LinkedList<Action>();
		transActions = new LinkedList<Action>();
		this.owner = owner;
	}
	
	public void addCheck(Action action){
		checks.add(action);
	}
	
	public void addAction(Action action){
		transActions.add(action);
	}
	
	public void removeCheck(Action action){
		checks.remove(action);
	}
	
	public void removeAction(Action action){
		transActions.remove(action);
	}
	
	public void purgeChecks(){
		checks = new LinkedList<Action>();
	}
	
	public void purgeActions(){
		transActions = new LinkedList<Action>();
	}
	
	public void addBoth(Action action){
		addCheck(action);
		addAction(action);
	}
	
	public void removeBoth(Action action){
		removeCheck(action);
		removeAction(action);
	}
	
	public void purgeBoth(){
		purgeChecks();
		purgeActions();
	}
	
	public void setNextState(FSCState next){
		this.next = next;
	}
	
	public FSCState getNextState(){
		return next;
	}
	
	public PhysicsObject getOwner(){
		return owner;
	}
	
	// All criteria must be met to continue
	public boolean check(){
		for(Action a : checks){
			if(!a.check()){
				return false;
			}
		}
		return true;
	}
	
	
	// Do all setup registered to this transition
	public void transAction(){
		for(Action a : transActions){
			a.execute();
		}
	}
}
