package edu.arizona.simulator.ww2d.blackboard.ks;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.DistanceEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.logging.FluentStore;
import edu.arizona.simulator.ww2d.logging.StateDatabase;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.SonarReading;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class ReplayKnowledgeSource implements KnowledgeSource {

	private FluentStore _fluentStore;
	private boolean _init;
	
	public ReplayKnowledgeSource() { 
		_fluentStore = new FluentStore("replay");
		_init = true;
		
		EventListener createListener = new EventListener() {
			@Override
			public void onEvent(Event e) {
				Space systemSpace = Blackboard.inst().getSpace("system");
				long time = systemSpace.get(Variable.logicalTime).get(Long.class);
				
				String xml = ((Element) e.getValue("element")).asXML();

				StateDatabase db = _fluentStore.getDB();
				db.recordEvent(e.getId().toString(), xml, time);
			} 
		};
		
		EventManager.inst().registerForAll(EventType.CREATE_GAME_OBJECT, createListener);
		EventManager.inst().registerForAll(EventType.CREATE_PHYSICS_OBJECT, createListener);
		
		EventManager.inst().registerForAll(EventType.REMOVE_OBJECT_EVENT, new EventListener() {
			@Override
			public void onEvent(Event e) {
				Space systemSpace = Blackboard.inst().getSpace("system");
				long time = systemSpace.get(Variable.logicalTime).get(Long.class);

				GameObject obj = (GameObject) e.getValue("object");
				
				StateDatabase db = _fluentStore.getDB();
				db.recordEvent(e.getId().toString(), obj.getName(), time);
			} 
		});
		
		EventManager.inst().registerForAll(EventType.FINISH, new EventListener() { 
			@Override
			public void onEvent(Event e) {
				Space systemSpace = Blackboard.inst().getSpace("system");
				long time = systemSpace.get(Variable.logicalTime).get(Long.class);

				StateDatabase db = _fluentStore.getDB();
				db.recordEvent(e.getId().toString(), time+"", 0);
			} 
		});
	}
	
	private void init() { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);

		_fluentStore.getDB().recordParameter("scale", scale+"");
		_init = false;
	}
	
	@Override
	public void update() { 		
		if (_init) 
			init();

		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);

		// For all of the objects we record the location in the world.  
		// For some objects (such as the background) this is meaningless but 
		// I can imagine something visual that needs to be recorded, so I'm doing it
		// anyways.
		for (GameObject obj : objectSpace.getAll()) { 
			Vec2 pos = obj.getPosition();
			
			_fluentStore.record("x", obj.getName(), pos.x);
			_fluentStore.record("y", obj.getName(), pos.y);
			_fluentStore.record("heading", obj.getName(), obj.getHeading());
		}
		
		// For each of the physics objects we need to record some information about
		// distances since it's harder to recreate later.
		List<PhysicsObject> physics = new ArrayList<PhysicsObject>(objectSpace.getPhysicsObjects());
		for (int i = 0; i < physics.size(); ++i) { 
			PhysicsObject obj = physics.get(i);
			if (obj.getType() == ObjectType.cognitiveAgent || obj.getType() == ObjectType.reactiveAgent) { 
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, obj.getName());
				BoundedEntry energy = agentSpace.getBounded(Variable.energy);
				BoundedEntry arousal = agentSpace.getBounded(Variable.arousal);
				BoundedEntry valence = agentSpace.getBounded(Variable.valence);

				_fluentStore.record("energy", obj.getName(), energy.getValue());
				_fluentStore.record("energyMax", obj.getName(), energy.getMax());

				_fluentStore.record("arousal", obj.getName(), arousal.getValue());
				_fluentStore.record("arousalMax", obj.getName(), arousal.getMax());
				
				_fluentStore.record("valence", obj.getName(), valence.getValue());
				_fluentStore.record("valenceMax", obj.getName(), valence.getMax());

				
				_fluentStore.record("goal", obj.getName(), agentSpace.get(Variable.goal).get(String.class));
				_fluentStore.record("state", obj.getName(), agentSpace.get(Variable.state).get(String.class));

				// TODO record arousal and our valence levels.  
				
				// now we need to loop over all of the sonars and record their values....
				List<SonarReading> sonar = agentSpace.get(Variable.sonar).get(List.class);
				for (int j = 0; j < sonar.size(); j+=10) { 
					SonarReading reading = sonar.get(j);
					_fluentStore.record("sonar_" + reading.getName(), obj.getName(), reading.getDistance()*scale);
				}
			}
			
			// Now record distances
			for (int j = i+1; j < physics.size(); ++j) { 
				PhysicsObject other = physics.get(j);
				DistanceEntry de = objectSpace.findOrAddDistance(obj, other);
				_fluentStore.record("distance", obj.getName() + " " + other.getName(), de.getDistance());
			}
		}
		
		
		
		for (GameObject obj : objectSpace.getAll()) { 
			Vec2 pos = obj.getPosition();
			
			_fluentStore.record("x", obj.getName(), pos.x);
			_fluentStore.record("y", obj.getName(), pos.y);
			_fluentStore.record("heading", obj.getName(), obj.getHeading());
			
			
			
		}
		_fluentStore.update();
	}
}
