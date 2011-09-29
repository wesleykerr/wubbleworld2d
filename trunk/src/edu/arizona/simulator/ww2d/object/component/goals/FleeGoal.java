package edu.arizona.simulator.ww2d.object.component.goals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.AuditoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.MemoryEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.events.player.BehaviorWeightEvent;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.PerceptionComponent;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.FleeFrom;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.ObstacleAvoidance;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Accumulator;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class FleeGoal implements Goal {
    private static Logger logger = Logger.getLogger( FleeGoal.class );

	private GoalEnum _status;
	
	private PhysicsObject _parent;
	private PhysicsObject _target;

	private Accumulator _accumulator;
	private Accumulator _allClear;
	
	private int _delay;
	
	public FleeGoal(PhysicsObject obj) { 
		_parent = obj;
		_status = GoalEnum.inactive;
		
		_accumulator = new Accumulator(30);
		_allClear = new Accumulator(160);
		_delay = 0;
	}
	
	@Override
	public void activate() {
		_status = GoalEnum.active;
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

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

		logger.debug(_parent.getName() + " activating run away: " + dynamicObjects.size());
		_target = objectSpace.getPhysicsObject((String) dynamicObjects.iterator().next());
		
		// turn on Separation
		BehaviorEvent event = new BehaviorEvent(FleeFrom.class, true, _parent);
		event.setTarget(_target.getName());
		EventManager.inst().dispatch(event);
		EventManager.inst().dispatch(new BehaviorWeightEvent(FleeFrom.class, 1.0f, _parent));
		
		// turn on ObstacleAvoidance
		EventManager.inst().dispatch(new BehaviorEvent(ObstacleAvoidance.class, true, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(ObstacleAvoidance.class, 1.25f, _parent));
		
		// we have entered the flee state, so we need to make sure that we have 
		// successfully fleed before we give up on it.
		_allClear.reset();
	}

	@Override
	public GoalEnum getStatus() {
		return _status;
	}

	@Override
	public GoalEnum process() {
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		ObjectSpace object = Blackboard.inst().getSpace(ObjectSpace.class, "object");
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
		
		// This section runs a couple of tests to determine if we need to make 
		// any modifications to our flee strategy.  Should we run faster, slower
		// etc.
		
		if (dynamicObjects.isEmpty()) {
			// we no longer see anything that could hurt us
			_allClear.record(1);
			space.changeSpeed(-0.15f, 1, 2.5f);
		} else { 
			++_delay;
			
			// if the chaser is too close, then we boost...
			float d = object.findOrAddDistance(_parent, _target).getDistance();
			System.out.println("Distance: " + d);
			if (d < PerceptionComponent.SMELL_RANGE && _delay >= 10) { 
				space.changeSpeed(0.15f, 1f, 2.5f);
				_delay = 0;
			} else { 
				space.changeSpeed(-0.15f, 1, 2.5f);
			}
		}

		if (_allClear.getSize() > 0.95f && _allClear.getAverage() > 0.5) 
			_status = GoalEnum.completed;

		
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

		EventManager.inst().dispatch(new BehaviorEvent(FleeFrom.class, false, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(FleeFrom.class, 1.0f, _parent));

		EventManager.inst().dispatch(new BehaviorEvent(ObstacleAvoidance.class, false, _parent));
		EventManager.inst().dispatch(new BehaviorWeightEvent(ObstacleAvoidance.class, 1.0f, _parent));
	}

	public float desireability() { 
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
	
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
		
		return 1;
	}
}
