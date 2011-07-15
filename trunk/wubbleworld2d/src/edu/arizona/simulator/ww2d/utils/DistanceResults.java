package edu.arizona.simulator.ww2d.utils;

import org.jbox2d.collision.Distance;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.XForm;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class DistanceResults {

	private PhysicsObject _e1;
	private PhysicsObject _e2;
	
	private Vec2 _cp1;
	private Vec2 _cp2;
	
	private float _distance;
	
	public DistanceResults(PhysicsObject e1, PhysicsObject e2) { 
		_e1 = e1;
		_e2 = e2;
		
		update();
	}
	
	public void update() { 
		_cp1 = new Vec2();
		_cp2 = new Vec2();
		
		Shape s1 = _e1.getBody().getShapeList();
		XForm x1 = _e1.getBody().getXForm();

		Shape s2 = _e2.getBody().getShapeList();
		XForm x2 = _e2.getBody().getXForm();

		Distance d = new Distance();
		_distance = d.distance(_cp1, _cp2, s1, x1, s2, x2);
	}
	
	public Vec2 getCp1() { 
		return _cp1;
	}
	
	public Vec2 getCp2() { 
		return _cp2;
	}
	
	public float getDistance() { 
		return _distance;
	}
}
