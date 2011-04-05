package edu.arizona.simulator.ww2d.object.component.goals;

import java.util.Random;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Align;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class IdleGoal implements Goal {

	private PhysicsObject _parent;
	private GoalEnum _status;

	private long _delay;
	private long _start;
	
	public IdleGoal(PhysicsObject obj) { 
		_parent = obj;
		_status = GoalEnum.inactive;
	}
	
	@Override
	public GoalEnum getStatus() { 
		return _status;
	}
	
	@Override
	public void activate() {
		_status = GoalEnum.active;
		_start = System.currentTimeMillis();
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		Random r = systemSpace.get(Variable.random).get(Random.class);
		_delay = (r.nextInt(4)+1) * 500;
	}

	@Override
	public GoalEnum process() {
		long current = System.currentTimeMillis();
		if (current - _start > _delay) { 
			_start = current;
			
			Space systemSpace = Blackboard.inst().getSpace("system");
			Random r = systemSpace.get(Variable.random).get(Random.class);
			_delay = (r.nextInt(4)+1) * 500;
			
			int randomAngle = r.nextInt(360);
			float radians = (float) Math.toRadians(randomAngle);
			
			BehaviorEvent event = new BehaviorEvent(Align.class, true, _parent);
			event.setTarget(radians);
			EventManager.inst().dispatch(event);

			AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
			space.get(Variable.state).setValue("turning");
		} else { 
			AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
			space.get(Variable.state).setValue("idle");
		}
		
		return GoalEnum.active;
	}

	@Override
	public void terminate() {
		_status = GoalEnum.inactive;
		EventManager.inst().dispatch(new BehaviorEvent(Align.class, false, _parent));
	}

	@Override
	public boolean succeeding() { 
		return true;
	}
	
	@Override
	public float desireability() { 
		return 0.005f;
	}
}
