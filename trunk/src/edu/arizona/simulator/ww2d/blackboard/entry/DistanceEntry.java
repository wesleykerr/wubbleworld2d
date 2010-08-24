package edu.arizona.simulator.ww2d.blackboard.entry;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.MathUtils;

public class DistanceEntry extends Entry {

	private PhysicsObject _obj1;
	private PhysicsObject _obj2;
	
	private Vec2 _closest1;
	private Vec2 _closest2;
	
	private float _distance;
	
	public DistanceEntry(PhysicsObject obj1, PhysicsObject obj2) { 
		_obj1 = obj1;
		_obj2 = obj2;
		
		_closest1 = new Vec2();
		_closest2 = new Vec2();
		
		_distance = MathUtils.distance(obj1, obj2, _closest1, _closest2);
	}
	
	public PhysicsObject getObject(PhysicsObject obj) { 
		if (_obj1 == obj)
			return _obj2;
		return _obj1;
	}
	
	/**
	 * Return the closest point that is on the given object.
	 * @param obj
	 * @return
	 */
	public Vec2 getPoint(PhysicsObject obj) { 
		if (_obj1 == obj)
			return _closest1;
		return _closest2;
	}
	
	/**
	 * Return the distance between the two agents.
	 * @return
	 */
	public float getDistance() { 
		return _distance;
	}
}
