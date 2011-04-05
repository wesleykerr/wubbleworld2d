package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Separation extends Behavior {
    private static Logger logger = Logger.getLogger( Separation.class );

	private static final float K = 600f;
	private static final float THRESHOLD = 20;
	
	private Seek _seek;
	private Align _align;
		
	public Separation(PhysicsObject entity) { 
		super(entity);
		
		_seek = new Seek(entity, new Vec2());
		_align = new Align(entity, 0);
	}
	
	@Override
	public SteeringOutput getSteering(float moveModifier, float turnModifier) {
//		logger.debug(_entity.getName() + "'s position is: " + _entity.getPosition());

		Space systemSpace = Blackboard.inst().getSpace("system");
		float maxAcceleration = systemSpace.get(Variable.maxAcceleration).get(Float.class);
		
		// we need to gather all of the neighbors that we know about.
		// We could use the AgentSpace and it's memories, but there are so
		// few agents, we can just access them directly
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		boolean foundOne = false;
		SteeringOutput so = new SteeringOutput(new Vec2(), 0);
		for (PhysicsObject obj : objectSpace.getDynamicObjects()) { 
			if (obj == _entity)
				continue;
			
			Vec2 direction = _entity.getPPosition().sub(obj.getPPosition());
			float distance = direction.normalize();
			
//			logger.debug(_entity.getName() + "d istance: " + distance);
			if (distance < THRESHOLD) { 
				foundOne = true;
				float strength = Math.min(K*(1 / (distance*distance)), maxAcceleration*moveModifier);
				so.blend(new SteeringOutput(direction.mul(strength), 0), 1);
			}
		}
		if (!foundOne)
			return doSeek(moveModifier, turnModifier);

		if (so.getVelocity().length() > maxAcceleration*moveModifier) { 
			so.getVelocity().normalize();
			so.getVelocity().mulLocal(maxAcceleration*moveModifier);
			
		}
		
		// let's go ahead and face the direction that we would like to 
		// move towards.
		_align.setTarget(_entity.getPPosition().add(so.getVelocity()));
		so.blend(_align.getSteering(moveModifier, turnModifier), 1);
		
//		logger.debug(_entity.getName() + " - " + so);
		return so;
	}
	
	/**
	 * We don't sense anything so we should just continue in the
	 * current direction
	 * @return
	 */
	private SteeringOutput doSeek(float moveModifier, float turnModifier) { 
		Vec2 position = _entity.getPPosition();
		_seek.setTarget(position.add(MathUtils.toVec2(_entity.getHeading())));
		return _seek.getSteering(moveModifier, turnModifier);
	}

	@Override
	public void onEvent(BehaviorEvent e) {
		_isOn = e.getState();
	}

}
