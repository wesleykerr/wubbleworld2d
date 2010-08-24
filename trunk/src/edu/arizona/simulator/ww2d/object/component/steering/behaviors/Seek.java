package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Seek extends Behavior{

	private Vec2 _target;
	
	public Seek(PhysicsObject entity, Vec2 targetPosition) { 
		super(entity);
		_target = targetPosition;
	}

	public void setTarget(Vec2 targetPosition) { 
		_target = targetPosition;
	}
	
	public SteeringOutput getSteering(float moveModifier, float turnModifier) {
		float maxAcceleration = Blackboard.inst().query("system", Variable.maxAcceleration, Float.class);

		Vec2 linear = _target.sub(_entity.getPPosition());
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
	
	@Override
	public void render(Graphics g) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);

		Vec2 tmp = _target.mul(scale);
		g.setColor(Color.blue);
		g.drawOval(tmp.x-1.5f, tmp.y-1.5f, 3, 3);
	}
}
