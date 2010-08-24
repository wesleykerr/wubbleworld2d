package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Flee extends Behavior {

	private Vec2 _target;
	
	public Flee(PhysicsObject entity, Vec2 targetPosition) { 
		super(entity);
		_target = targetPosition;
	}

	public void setTarget(Vec2 targetPosition) { 
		_target = targetPosition;
	}
		
	public SteeringOutput getSteering(float moveModifier, float turnModifier) {
		float maxAcceleration = Blackboard.inst().query("system", Variable.maxAcceleration, Float.class);

		Vec2 linear = new Vec2();
		linear = _entity.getPPosition().sub(_target);

		linear.normalize();
		linear.mulLocal(maxAcceleration*moveModifier);
		
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
