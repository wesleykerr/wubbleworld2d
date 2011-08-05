package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions;

import java.util.HashMap;
import java.util.LinkedList;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class MaintainDataFunction extends Function {

	private LinkedList<String> names;

	public MaintainDataFunction(PhysicsObject owner, LinkedList<String> fields) {
		super(owner);
		this.names = fields;
		for(String s : fields){
			outputFields.add(s);
		}
	}

	@Override
	public void calculate(int elapsed, HashMap<String, Field> fields) {
		for(String name : this.names){
			if(fields.containsKey(name)){
				((ObjectFieldSpace) Blackboard.inst().getSpace("objectfield")).addTemp(owner,fields.get(name));
			}
		}
		
	}

}
