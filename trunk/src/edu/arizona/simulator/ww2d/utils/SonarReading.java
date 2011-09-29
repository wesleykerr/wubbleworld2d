package edu.arizona.simulator.ww2d.utils;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.PerceptionComponent;

public class SonarReading {	
	private String _name;
	private float _angle;
	private float _distance;
	private PhysicsObject _interacts;
	
	public SonarReading(String name, float angle) { 
		_name = name;
		_angle = angle;
		_distance = PerceptionComponent.SIGHT_RANGE;
		_interacts = null;
	}
	
	public String getName() { 
		return _name;
	}
	
	/**
	 * Returns a line that starts at position pos with orientation
	 * of the angle and as long as you request.
	 * @param pos
	 * @param angle
	 * @param distance
	 * @return
	 */
	public Line2D getLine(Vec2 pos, float angle, float distance) { 
		double a = angle + Math.toRadians(_angle);
		Point2D pa = new Point2D.Float(pos.x,pos.y);
		Point2D pb = new Point2D.Float(
				(float) (pos.x+distance*Math.cos(a)),
				(float) (pos.y+distance*Math.sin(a)));
		
		return new Line2D.Double(pa, pb);
	}
	
	public void reset() { 
		_interacts = null;
		_distance = PerceptionComponent.SIGHT_RANGE;
	}
	
	public float getAngle() { 
		return _angle;
	}
	
	public void setAngle(float angle) { 
		_angle = angle;
	}
	
	public float getDistance() { 
		return _distance;
	}
	
	public void setDistance(float distance) { 
		_distance = distance;
	}
	
	public PhysicsObject getInteracts() { 
		return _interacts;
	}
	
	public void setInteracts(PhysicsObject interacts) { 
		_interacts = interacts;
	}
}
