package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Arrive extends Behavior {
    private static Logger logger = Logger.getLogger( Arrive.class );

    private Vec2  _target;
	
	private float _targetRadius;
	private float _slowRadius;
	
	private float _timeToTarget;
	
	public Arrive(PhysicsObject entity, Vec2 target) { 
		super(entity);
		_target = target;
		
		_targetRadius = 0.001f;
		_slowRadius = 1f;
		_timeToTarget = 0.1f;
	}
	
	public void setTargetRadius(float targetRadius) { 
		_targetRadius = targetRadius;
	}
	
	public void setSlowRadius(float slowRadius) {
		_slowRadius = slowRadius;
	}

	public void setTarget(Vec2 target) { 
		_target = target;
	}
	
	public SteeringOutput getSteering(float moveModifier, float turnModifier) {
		float maxSpeed = Blackboard.inst().query("system", Variable.maxSpeed, Float.class);
		float maxAcceleration = Blackboard.inst().query("system", Variable.maxAcceleration, Float.class);
				
		Vec2 direction = _target.sub(_entity.getPPosition());
		float distance = direction.length();
		
		if (distance < _targetRadius) {
			// trip some sort of flag so that we know that we have
			// finished the alignment.  Would send messages, but that
			// is overkill for this flag as is the blackboard.
			_entity.setUserData("arrive", true);
			return null;
		}
		_entity.setUserData("arrive", false);
		
		float targetSpeed = 0f;
		if (distance > _slowRadius) { 
			targetSpeed = maxSpeed*moveModifier;
		} else { 
			targetSpeed = (maxSpeed*moveModifier) * distance / _slowRadius;
		}
		
		Vec2 targetVelocity = new Vec2(direction.x, direction.y);
		targetVelocity.normalize();
		targetVelocity.mulLocal(targetSpeed);
		
		// Acceleration tries to get to the target velocity.
		Vec2 linear = targetVelocity.sub(_entity.getBody().getLinearVelocity());
		linear.mulLocal(1.0f / _timeToTarget);

		if (linear.length() > (maxAcceleration*moveModifier)) { 
			linear.normalize();
			linear.mulLocal(maxAcceleration*moveModifier);
		}
		
		return new SteeringOutput(linear, 0);
	}

	@Override
	public void onEvent(Event e) {
		_isOn = (Boolean) e.getValue("status");
		
		if (!_isOn)
			return;
		
		Object target = e.getValue("target");
		if (target instanceof Vec2) { 
			setTarget((Vec2) target);
		} else { 
			throw new RuntimeException("Unknown target type: " + target.getClass());
		}
	}
}
