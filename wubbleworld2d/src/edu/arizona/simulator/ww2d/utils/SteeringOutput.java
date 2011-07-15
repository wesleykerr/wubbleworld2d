package edu.arizona.simulator.ww2d.utils;

import org.jbox2d.common.Vec2;

public class SteeringOutput {

	private Vec2 _velocity;
	private float _angular;

	public SteeringOutput(Vec2 velocity, float angular) {
		_velocity = velocity;
		_angular = angular;
	}

	public Vec2 getVelocity() {
		return _velocity;
	}

	public float getAngular() {
		return _angular;
	}
	
	/**
	 * Add the results of the other SteeringOutput
	 * to this SteeringOuput result.
	 * @param so
	 * @param weight
	 */
	public void blend(SteeringOutput so, float weight) { 
		if (so == null)
			return;
		
		_velocity.addLocal(so._velocity.mul(weight));
		_angular += (so._angular*weight);
	}


	 public void blend(SteeringOutput so) {
		 if (so == null)
			 return;

		 _velocity.addLocal(so._velocity);
		 _angular += so._angular;
	 }
	 
	 public String toString() { 
		 return "force: " + _velocity.toString() + " angular: " + _angular; 
	 }
}
