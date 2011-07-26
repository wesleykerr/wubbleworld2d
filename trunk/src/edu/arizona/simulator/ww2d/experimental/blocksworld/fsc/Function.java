package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;
import java.util.LinkedList;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public abstract class Function {
	LinkedList<String> inputFields;
	LinkedList<String> outputFields;
	PhysicsObject owner;
	
	
	public abstract void calculate(LinkedList<Field> fields, FSCState state);
	
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
