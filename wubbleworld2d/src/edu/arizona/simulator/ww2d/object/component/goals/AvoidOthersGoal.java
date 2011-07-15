package edu.arizona.simulator.ww2d.object.component.goals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.AuditoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.MemoryEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.events.player.BehaviorWeightEvent;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.ObstacleAvoidance;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Separation;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Wander;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class AvoidOthersGoal implements Goal {
    private static Logger logger = Logger.getLogger( AvoidOthersGoal.class );

	private GoalEnum _status;
	
	private PhysicsObject _parent;
	
	public AvoidOthersGoal(PhysicsObject obj) { 
		_parent = obj;
		_status = GoalEnum.inactive;
	}
	
	@Override
	public void activate() {
		_status = GoalEnum.active;
		logger.debug(_parent.getName() + " activating run away...");

		// turn on Separation
		EventManager.inst().dispatch(new BehaviorEvent(Separation.class, true, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(Separation.class, 1.0f, _parent));

		// turn on ObstacleAvoidance
		EventManager.inst().dispatch(new BehaviorEvent(ObstacleAvoidance.class, true, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(ObstacleAvoidance.class, 2.0f));

		// turn on Wander
		EventManager.inst().dispatch(new BehaviorEvent(Wander.class, true, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(Wander.class, 0.5f));
	}

	@Override
	public GoalEnum getStatus() {
		return _status;
	}

	@Override
	public GoalEnum process() {
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		space.get(Variable.state).setValue("separate");
		
		LinkedList<Map<String,MemoryEntry>> visual = space.getVisualMemories();
		LinkedList<Map<String,AuditoryEntry>> auditory = space.getAuditoryMemories();

		Set<String> dynamicObjects = new HashSet<String>();
		for (Map<String,MemoryEntry> map : visual) {
			for (MemoryEntry memory : map.values()) { 
				if (memory.obj.getType() == ObjectType.cognitiveAgent ||
					memory.obj.getType() == ObjectType.reactiveAgent)
					dynamicObjects.add(memory.obj.getName());
			}
		}

		for (Map<String,AuditoryEntry> map : auditory) { 
			for (AuditoryEntry memory : map.values()) { 
				if (memory.obj.getBody().getMass() > 0) 
					dynamicObjects.add(memory.obj.getName());
			}
		}

		// we no longer see anything that could hurt us
		if (dynamicObjects.isEmpty()) {
			_status = GoalEnum.completed;
		} 
		
		return _status;
	}

	@Override
	public boolean succeeding() {
		// test to see if we are getting closer to the target
		
		return false;
	}

	@Override
	public void terminate() {
		logger.debug(_parent.getName() + " deactivating run away...");

		// turn off Separation
		EventManager.inst().dispatch(new BehaviorEvent(Separation.class, false, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(Separation.class, 1.0f, _parent));

		// turn off ObstacleAvoidance
		EventManager.inst().dispatch(new BehaviorEvent(ObstacleAvoidance.class, false, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(ObstacleAvoidance.class, 1.0f));

		// turn off Wander
		EventManager.inst().dispatch(new BehaviorEvent(Wander.class, false, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(Wander.class, 1.0f));
	}

	public float desireability() { 
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		BoundedEntry energy = agentSpace.getBounded(Variable.energy);
		float pctEnergy = energy.getValue() / energy.getMax();
		if (pctEnergy > 0.25f)
			return 0;
	
		LinkedList<Map<String,MemoryEntry>> visual = agentSpace.getVisualMemories();
		LinkedList<Map<String,AuditoryEntry>> auditory = agentSpace.getAuditoryMemories();

		Set<String> dynamicObjects = new HashSet<String>();
		for (Map<String,MemoryEntry> map : visual) {
			for (MemoryEntry memory : map.values()) { 
				if (memory.obj.getType() == ObjectType.cognitiveAgent ||
					memory.obj.getType() == ObjectType.reactiveAgent)
					dynamicObjects.add(memory.obj.getName());
			}
		}

		for (Map<String,AuditoryEntry> map : auditory) { 
			for (AuditoryEntry memory : map.values()) { 
				if (memory.obj.getBody().getMass() > 0) 
					dynamicObjects.add(memory.obj.getName());
			}
		}

		// we no longer see anything that could hurt us
		if (dynamicObjects.isEmpty())
			return 0;
		
		return 1 - (pctEnergy*pctEnergy);
	}
}
