package edu.arizona.simulator.ww2d.blackboard.ks;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.AuditoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.CollisionEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.DistanceEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.FoodEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.MemoryEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.system.UpdateEnd;
import edu.arizona.simulator.ww2d.logging.FluentStore;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

/**
 * The GlobalKS is responsible for a bird's eye view of the 
 * simulator.  Even though each agent has a limited perceptual 
 * system, this overwrites it since we assume that we have a 
 * third party watching the activity taking place.
 * @author wkerr
 *
 */
public class GlobalKS implements KnowledgeSource {

	private FluentStore _fluentStore;
	private boolean _init;
	
	public GlobalKS() { 
		_fluentStore = new FluentStore("global");
		_init = true;
		
		EventManager.inst().registerForAll(UpdateEnd.class, new EventListener() { 
			@Override
			public void onEvent(Event e) { 
				postUpdate();
			}
		});
	}
	
	@Override
	public void update() { 		
		// do nothing since it is all handled in the post update....
	}
	
	public void postUpdate() {

		// For all of the objects we record the location in the world.  
		// For some objects (such as the background) this is meaningless but 
		// I can imagine something visual that needs to be recorded, so I'm doing it
		// anyways.
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		List<PhysicsObject> physics = new ArrayList<PhysicsObject>(objectSpace.getPhysicsObjects());
		for (int i = 0; i < physics.size(); ++i) { 
			PhysicsObject obj = physics.get(i);
			Vec2 pos = obj.getPPosition();
			
			_fluentStore.record("x", obj.getName(), pos.x);
			_fluentStore.record("y", obj.getName(), pos.y);
			_fluentStore.record("heading", obj.getName(), obj.getHeading());
			
			// record information between pairs of objects.... ordered by id (not the best way I admit)
			for (int j = i+1; j < physics.size(); ++j) { 
				PhysicsObject other = physics.get(j);
				String name = obj.getName() + " " + other.getName();
				
				DistanceEntry de = objectSpace.findOrAddDistance(obj, other);
				_fluentStore.record("distance", name, de.getDistance());

				Vec2 relativeVelocity = obj.getBody().getLinearVelocity().sub(other.getBody().getLinearVelocity());
				_fluentStore.record("relativeVx", name, relativeVelocity.x);
				_fluentStore.record("relativeVy", name, relativeVelocity.y);
				
				// record the relational position since it may prove useful
				Vec2 relativePosition = pos.sub(other.getPPosition());
				_fluentStore.record("relativeX", name, relativePosition.x);
				_fluentStore.record("relativeY", name, relativePosition.y);

			}

			if (obj.getType() == ObjectType.cognitiveAgent || obj.getType() == ObjectType.reactiveAgent) { 
				recordCognitive(obj);
			}
		}
		
		for (CollisionEntry entry : objectSpace.getCollisions()) { 
			_fluentStore.recordw("collision", entry.getName(), true, false);
		}
		
		_fluentStore.update();
	}
	
	private void recordCognitive(PhysicsObject obj) { 
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, obj.getName());
		_fluentStore.record("goal", obj.getName(), agentSpace.get(Variable.goal).get(String.class));
		_fluentStore.record("state", obj.getName(), agentSpace.get(Variable.state).get(String.class));
		
		// let's record whose visible, within hearing range, and within scent range.

		// There is a buffer of visual memories but we are only concerned with the
		// most recent for the time being.
		for (MemoryEntry memory : agentSpace.getVisualMemories().getFirst().values()) { 
			String name = obj.getName() + " " + memory.obj.getName();
			_fluentStore.recordw("visible", name, true, false);
		}
		
		for (AuditoryEntry memory : agentSpace.getAuditoryMemories().getFirst().values()) { 
			String name = obj.getName() + " " + memory.obj.getName();
			_fluentStore.recordw("heard", name, true, false);
		}
		
		for (FoodEntry memory : agentSpace.getScentMemories().getFirst().values()) { 
			String name = obj.getName() + " " + memory.obj.getName();
			_fluentStore.recordw("smelt", name, true, false);
		}
	}
}
