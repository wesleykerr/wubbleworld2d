package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Align extends Behavior {
    private static Logger logger = Logger.getLogger( Align.class );
	
	private static final float ONE_DEGREE = 0.0174532925f; // 1 degree in radians

	private float _target;

	private float _targetRadius;
	private float _slowRadius;
	
	private float _timeToTarget;
	
	/**
	 * 
	 * @param entity
	 * @param target - in radians
	 */
	public Align(PhysicsObject entity, float target) { 
		super(entity);
		_isOn = false;
		_target = target;
		
		_timeToTarget = 0.1f;
		_targetRadius = 3*ONE_DEGREE;
		_slowRadius = 15*ONE_DEGREE;		
		
	}
	
	/**
	 * Sets the target that we are after.
	 * @param target
	 */
	public void setTarget(float target) { 
		_target = target;
	}
	
	/**
	 * Called when the target is a point in 
	 * space that we want to align to.
	 */
	public void setTarget(Vec2 target) { 
		Vec2 difference = target.sub(_entity.getPPosition());
		difference.normalize();
		float angle = (float) Math.atan2(difference.y, difference.x);
		setTarget(angle);
	}
	
	public SteeringOutput getSteering(float moveModifier, float turnModifier) {
		Space systemSpace = Blackboard.inst().getSpace("system");
		float maxRotation = systemSpace.get(Variable.maxRotation).get(Float.class);
		float maxAcceleration = systemSpace.get(Variable.maxAngularAcceleration).get(Float.class);
		
		float rotation = MathUtils.angleDiff(_entity.getHeading(), _target);
		float rotationSize = Math.abs(rotation);

		if (rotationSize < _targetRadius) { 
			// trip some sort of flag so that we know that we have
			// finished the alignment.  Would send messages, but that
			// is overkill for this flag as is the blackboard.
			_entity.setUserData("align", true);
			return null;
		}
		
		_entity.setUserData("align", false);

		float targetRotation = 0;
		if (rotationSize > _slowRadius) { 
			targetRotation = (maxRotation*turnModifier);
		} else { 
			targetRotation = (maxRotation*turnModifier) * rotationSize / _slowRadius;
		}
			
		targetRotation *= (rotation/rotationSize);
		float angular = (targetRotation - _entity.getBody().getAngularVelocity()) / _timeToTarget;
		float angularAcceleration = Math.abs(angular);

		if (angularAcceleration > (maxAcceleration*turnModifier)) {
			angular /= angularAcceleration;
			angular *= (maxAcceleration*turnModifier);
		}
		
//		logger.debug(_entity.getName() + " " + _entity.getBody().getAngularVelocity() + " " + targetRotation + " " + angular);
	
		return new SteeringOutput(new Vec2(), angular);
	}

	@Override
	public void onEvent(Event e) {
		_isOn = (Boolean) e.getValue("status");
		
		if (!_isOn)
			return;
		
		Object target = e.getValue("target");
		if (target instanceof Float) { 
			setTarget((Float) target);
		} else if (target instanceof Vec2) { 
			setTarget((Vec2) target);
		} else { 
			throw new RuntimeException("Unknown target type: " + target.getClass());
		}
	}
	
	@Override
	public void render(Graphics g) { 
		Vec2 pos = _entity.getPosition();
		Vec2 dir = MathUtils.toVec2(_target).mul(20);
		
		g.setColor(Color.cyan);
		g.fillOval(pos.x + dir.x - 1, pos.y + dir.y - 1, 2, 2);
	}
}
