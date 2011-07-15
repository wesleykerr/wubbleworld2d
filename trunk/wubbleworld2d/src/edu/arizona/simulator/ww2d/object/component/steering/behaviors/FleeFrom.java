package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;

public class FleeFrom extends Behavior {
    private static Logger logger = Logger.getLogger( FleeFrom.class );

    private Flee _flee;
    private Align _align;
    
	private float _maxPrediction;
	private PhysicsObject _targetObj;
	
	public FleeFrom(PhysicsObject obj) {
		super(obj);
		
		_flee = new Flee(obj, new Vec2(0,0));
		_align = new Align(obj, 0);
		
		_maxPrediction = 0.001f;
	}
	
	/**
	 * Set the target entity that we are pursuing.
	 * @param name
	 */
	public void setTarget(String name) {
		// need to query the blackboard to retrieve
		// the PhysicsObject with the given name
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		_targetObj = objectSpace.query(PhysicsObject.class, name);
	}

	@Override
	public SteeringOutput getSteering(float moveModifier, float turnModifier) {
		Vec2 tPos = _targetObj.getPPosition();
		Vec2 tVel = _targetObj.getBody().getLinearVelocity();
		
		Vec2 direction = _entity.getPPosition().sub(tPos);
		float distance = direction.length();

		float speed = _entity.getBody().getLinearVelocity().length();
		
		float prediction = 0;
		if (speed <= (distance / _maxPrediction)) { 
			prediction = _maxPrediction;
		} else {
			prediction = distance / speed;
		}

		Vec2 target = tPos.add(tVel.mul(prediction));
		_flee.setTarget(target);
		_align.setTarget(_entity.getPPosition().add(direction));
		
		SteeringOutput so = _flee.getSteering(moveModifier, turnModifier);
		if (so != null) { 
			so.blend(_align.getSteering(moveModifier, turnModifier), 1);
		} else { 
			so = _align.getSteering(moveModifier, turnModifier);
		}
		
		if (so == null)
			return new SteeringOutput(new Vec2(0,0), 0);
		return so;
	}
	
	@Override
	public void onEvent(BehaviorEvent e) {
		_isOn = e.getState();
		
		if (!_isOn)
			return;
		
		Object target = e.getTarget();
		if (target instanceof String) { 
			setTarget((String) target);
		} else { 
			throw new RuntimeException("Unknown target type: " + target.getClass());
		}
	}
	
}
