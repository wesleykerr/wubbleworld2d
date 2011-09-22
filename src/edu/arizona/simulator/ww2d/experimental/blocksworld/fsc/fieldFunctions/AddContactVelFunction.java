package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions;

import java.util.HashMap;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.ContactPoint;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class AddContactVelFunction extends Function {

	public AddContactVelFunction(PhysicsObject owner) {
		super(owner);
		inputFields.add("contactPoint");
		inputFields.add("dx");
		inputFields.add("dy");
		inputFields.add("collideObject");
		outputFields.add("dx");
		outputFields.add("dy");
	}

	@Override
	public void calculate(int elapsed, HashMap<String, Field> fields) {
		// TODO Auto-generated method stub
		ContactPoint cp = (ContactPoint)fields.get("contactPoint").getData();
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		PhysicsObject collision = (PhysicsObject)fields.get("collideObject").getData();
		HashMap<String,Field> collideData = ofs.getMap(collision);
		
		Vec2 vel = new Vec2((Float)fields.get("dx").getData(),(Float)fields.get("dy").getData());
		Vec2 collideVel = new Vec2((Float)collideData.get("dx").getData(),(Float)collideData.get("dy").getData());
		
		vel.addLocal(collideVel);
		ofs.addTemp(owner, new Field("dx",vel.x));
		ofs.addTemp(owner, new Field("dy",vel.y));
	}

}
