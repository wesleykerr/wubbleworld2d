package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.HashMap;
import java.util.LinkedList;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class FSCTransition {
	FSCState next;
	LinkedList<Check> checks;
	LinkedList<Action> transActions;
	PhysicsObject owner;
	LinkedList<Function> functions;
	
	public FSCTransition(PhysicsObject owner){
		next = null;
		checks = new LinkedList<Check>();
		transActions = new LinkedList<Action>();
		this.owner = owner;
		functions = new LinkedList<Function>();
	}
	
	public void addCheck(Check check){
		checks.add(check);
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
		checks = new LinkedList<Check>();
	}
	
	public void purgeActions(){
		transActions = new LinkedList<Action>();
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
	
	@Deprecated
	public void resetChecks(){
		for(Check c : checks){
			c.reset();
		}
	}
	
	// All criteria must be met to continue
	public boolean check(int elapsed){
		for(Check check : checks){
			if(!check.check(elapsed)){
				return false;
			}
		}
		return true;
	}
	
	
	// Do all setup registered to this transition
	@Deprecated
	public void transAction(int elapsed){
		for(Action a : transActions){
			a.execute(elapsed);
		}
	}
	
	public LinkedList<String> requiredDataFields(){
		LinkedList<String> toReturn = new LinkedList<String>();
		for(Check c : checks){
			toReturn.addAll(c.requiredFields());
		}
		
		return toReturn;
	}

	public void addFunctions(LinkedList<Function> functions) {
		this.functions.addAll(functions);
	}
	
	public void addFunction(Function func){
		functions.add(func);
	}

	public void applyTransforms(FSCState prev, int elapsed) {
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		HashMap<String, Field> fields = ofs.getMap(owner);
		// Make sure we don't have straggler data that's no longer relevant
//		if(fields != null && !fields.isEmpty())
//			ofs.purge(owner);
//		else{
//			fields = ofs.getEphemeral().get(owner);
//		}
		
		for(Function f : functions){
			if(fields != null&& f.satisfied(new LinkedList<String>(fields.keySet()))){
				f.calculate(elapsed, fields);
			}
		}
	}
}
