package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions;

import java.util.HashMap;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class IntervalReboundFunction extends Function{

	public IntervalReboundFunction(PhysicsObject owner) {
		super(owner);
		inputFields.add("dx");
		inputFields.add("dy");
		inputFields.add("collidedObject");
		outputFields.add("interval");
		outputFields.add("dx");
		outputFields.add("dy");
	}

	@Override
	public void calculate(int elapsed, HashMap<String, Field> fields) {
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		float dx = (Float) ofs.retrieve(owner, "dx").getData();
		dx = -dx;
		float dy = (Float) ofs.retrieve(owner, "dy").getData();
		dy = -dy;
		
		ofs.addTemp(owner, new Field("dx",dx));
		ofs.addTemp(owner, new Field("dy",dy));
		
		int interval = (int) ((Math.abs(dx) + Math.abs(dy)) * 100);
		ofs.addTemp(owner, new Field("interval",interval));
	}

}
