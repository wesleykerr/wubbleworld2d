package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions;

import java.util.HashMap;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class AccelerateFunction extends Function {
	
	private float ax;
	private float ay;

	public AccelerateFunction(PhysicsObject owner, float ax, float ay) {
		super(owner);
		inputFields.add("dx");
		inputFields.add("dy");
		outputFields.add("dx");
		outputFields.add("dy");
		this.ax = ax;
		this.ay = ay;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void calculate(int elapsed, HashMap<String, Field> fields) {
		float inMillis = (float)elapsed/1000;
		float dx = (Float)fields.get("dx").getData();
		float dy = (Float)fields.get("dy").getData();
		
		dx += ax * inMillis;
		dy += ay * inMillis;
		
		
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		ofs.addTemp(owner, new Field("dx",dx));
		ofs.addTemp(owner, new Field("dy",dy));
	}

}
