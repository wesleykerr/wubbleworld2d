package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;
import java.util.HashMap;
import java.util.LinkedList;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public abstract class Function {
	protected LinkedList<String> inputFields;
	protected LinkedList<String> outputFields;
	protected PhysicsObject owner;
	
	
	public abstract void calculate(int elapsed, HashMap<String, Field> fields);
	
	public Function(PhysicsObject owner){
		this.owner = owner;
		this.inputFields = new LinkedList<String>();
		this.outputFields = new LinkedList<String>();
	}
	
	// Check whether the inputs satisfy out constraints
	public boolean satisfied(LinkedList<String> fields){
		for(String s : inputFields){
			if(!fields.contains(s)){
				return false;
			}
		}
		
		return true;
	}
	
	// Tests whether out outputs satisfy the "quota"
	public boolean satisfies(LinkedList<String> fields){
		for(String s : fields){
			if(!outputFields.contains(s)){
				return false;
			}
		}
		
		return true;
	}
}
