package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions;

import java.util.HashMap;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.ContactPoint;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class CollisionReboundFunction extends Function {

	public CollisionReboundFunction(PhysicsObject owner) {
		super(owner);
		inputFields.add("dx");
		inputFields.add("dy");
		inputFields.add("collideObject");
		inputFields.add("contactPoint");
		outputFields.add("dx");
		outputFields.add("dy");
	}

	@Override
	public void calculate(int elapsed, HashMap<String, Field> fields) {
		PhysicsObject collision = (PhysicsObject) fields.get("collideObject").getData();
		float dx = (Float)fields.get("dx").getData();
		float dy = (Float)fields.get("dy").getData();
		ContactPoint cp = (ContactPoint) fields.get("contactPoint").getData();
		
		Vec2 norm = cp.normal;
		
		Vec2 vel = new Vec2(dx,dy);
		float dot = Vec2.dot(vel, norm) / Vec2.dot(norm, norm);
		Vec2 u = new Vec2(norm.x * dot, norm.y * dot);
		Vec2 w = vel.sub(u);
		
		dx = w.x * cp.friction - u.x *  cp.restitution;
		dy = w.y * cp.friction - u.y *  cp.restitution;
		
//		dx = -dx;
//		dy = -dy;
		
//		float collideAngle = collision.getBody().getAngle();
//		
//		dx = (float) (Math.cos(collideAngle) * dx + -Math.sin(collideAngle) * dx);
//		dy = (float) (Math.sin(collideAngle)	* dy + Math.cos(collideAngle) * dy);
//		System.out.println(dx + " " + dy);
		//dx = -dx;
		//dy = -dy;
		
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		ofs.addTemp(owner, new Field("dx",dx));
		ofs.addTemp(owner, new Field("dy",dy));
		fields.put("dx", new Field("dx",dx));
		fields.put("dy", new Field("dy",dy));
	}

}
