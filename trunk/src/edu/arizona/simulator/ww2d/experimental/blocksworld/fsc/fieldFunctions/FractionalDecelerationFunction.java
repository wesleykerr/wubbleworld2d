package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions;

import java.util.HashMap;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class FractionalDecelerationFunction extends Function{
	
	private float dxScale;
	private float dyScale;
	
	public FractionalDecelerationFunction(PhysicsObject owner, float dxScale, float dyScale) {
		super(owner);
		this.dxScale = dxScale;
		this.dyScale = dyScale;
		inputFields.add("dx");
		inputFields.add("dy");
		outputFields.add("dx");
		outputFields.add("dy");
	}

	@Override
	public void calculate(int elapsed, HashMap<String, Field> fields) {
		float dx = (Float) fields.get("dx").getData();
		float dy = (Float) fields.get("dy").getData();
		
		dx /= dxScale;
		dy /= dyScale;
		
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		ofs.addTemp(owner, new Field("dx",dx));
		ofs.addTemp(owner, new Field("dy",dy));
		fields.put("dx", new Field("dx",dx));
		fields.put("dy", new Field("dy",dy));
	}

}
