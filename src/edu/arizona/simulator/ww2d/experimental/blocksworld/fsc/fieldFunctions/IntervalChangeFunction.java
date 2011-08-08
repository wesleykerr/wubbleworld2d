package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions;

import java.util.HashMap;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
@Deprecated
public class IntervalChangeFunction extends Function {

	int dInterval;
	
	public IntervalChangeFunction(PhysicsObject owner, int dInterval) {
		super(owner);
		this.dInterval = dInterval;
		outputFields.add("interval");
	}

	@Override
	public void calculate(int elapsed, HashMap<String, Field> fields) {
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		int interval = 0;
		if(ofs.retrieve(owner, "interval") != null){
			interval = (Integer)ofs.retrieve(owner, "interval").getData();
		}
		
		interval += dInterval;
		
		ofs.addTemp(owner, new Field("interval",interval));
	}

}
