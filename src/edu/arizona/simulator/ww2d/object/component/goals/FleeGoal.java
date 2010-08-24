package edu.arizona.simulator.ww2d.object.component.goals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.AuditoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.MemoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.FleeFrom;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.ObstacleAvoidance;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Accumulator;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class FleeGoal implements Goal {
    private static Logger logger = Logger.getLogger( FleeGoal.class );

	private GoalEnum _status;
	
	private PhysicsObject _parent;
	private PhysicsObject _target;

	private Accumulator _accumulator;
	
	public FleeGoal(PhysicsObject obj) { 
		_parent = obj;
		_status = GoalEnum.inactive;
		
		_accumulator = new Accumulator(100);
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
		Event e = new Event(EventType.BEHAVIOR_EVENT);
		e.addRecipient(_parent);
		e.addParameter("name", FleeFrom.class);
		e.addParameter("status", true);
		e.addParameter("target", _target.getName());

		EventManager.inst().dispatch(e);

		e = new Event(EventType.BEHAVIOR_WEIGHT_EVENT);
		e.addRecipient(_parent);
		e.addParameter("name", FleeFrom.class);		
		e.addParameter("weight", 1.0f);

		EventManager.inst().dispatch(e);

		// turn on ObstacleAvoidance
		e = new Event(EventType.BEHAVIOR_EVENT);
		e.addRecipient(_parent);
		e.addParameter("name", ObstacleAvoidance.class);
		e.addParameter("status", true);
		
		EventManager.inst().dispatch(e);

		e = new Event(EventType.BEHAVIOR_WEIGHT_EVENT);
		e.addRecipient(_parent);
		e.addParameter("name", ObstacleAvoidance.class);		
		e.addParameter("weight", 2.0f);

		EventManager.inst().dispatch(e);
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
		
		if (space.isCollidingWith(_target)) {
			space.increaseSpeed();
		}
		
		// update the accumulator with our success or failure.
		Set<String> approaching = space.getApproaching();
		if (approaching == null || approaching.size() == 0) 
			_accumulator.record(1);
		else if (approaching.contains(_target.getName())) 
			_accumulator.record(0);
		else
			_accumulator.record(1);
		
		// if we have recorded at least half as many samples as we need then
		// we can see if we need to speed up.
		if (_accumulator.getSize() > 0.95f && _accumulator.getAverage() < 0.5f) { 
			logger.debug("Accumulator size: " + _accumulator.getSize() + " " + _accumulator.getAverage());
			space.increaseSpeed();
			_accumulator.reset();
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

		// turn on Separation
		Event e = new Event(EventType.BEHAVIOR_EVENT);
		e.addRecipient(_parent);
		e.addParameter("name", FleeFrom.class);
		e.addParameter("status", false);
		
		EventManager.inst().dispatch(e);
		
		e = new Event(EventType.BEHAVIOR_WEIGHT_EVENT);
		e.addRecipient(_parent);
		e.addParameter("name", FleeFrom.class);		
		e.addParameter("weight", 1.0f);

		EventManager.inst().dispatch(e);

		// turn on ObstacleAvoidance
		e = new Event(EventType.BEHAVIOR_EVENT);
		e.addRecipient(_parent);
		e.addParameter("name", ObstacleAvoidance.class);
		e.addParameter("status", false);
		
		EventManager.inst().dispatch(e);
		
		e = new Event(EventType.BEHAVIOR_WEIGHT_EVENT);
		e.addRecipient(_parent);
		e.addParameter("name", ObstacleAvoidance.class);		
		e.addParameter("weight", 1.0f);

		EventManager.inst().dispatch(e);		
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
