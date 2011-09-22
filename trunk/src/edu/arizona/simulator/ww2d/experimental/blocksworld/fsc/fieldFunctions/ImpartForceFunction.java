package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions;

import java.util.HashMap;

import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class ImpartForceFunction extends Function {

	public ImpartForceFunction(PhysicsObject owner) {
		super(owner);
		inputFields.add("dx");
		inputFields.add("dy");
	}

	@Override
	public void calculate(int elapsed, HashMap<String, Field> fields) {
		// TODO Auto-generated method stub
		// Dot product between velocity and contact point;
	}

}
