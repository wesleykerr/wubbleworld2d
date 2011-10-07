package edu.arizona.simulator.ww2d.blackboard.spaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.ContactPoint;

import edu.arizona.simulator.ww2d.blackboard.AgentHelper;
import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.AuditoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.CollisionEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.DistanceEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.FoodEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.MemoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.system.UpdateEnd;
import edu.arizona.simulator.ww2d.events.system.UpdateStart;
import edu.arizona.simulator.ww2d.logging.FluentStore;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class AgentSpace extends Space {
    private static Logger logger = Logger.getLogger( AgentSpace.class );

    private String _name;
    
    // perception related
	private LinkedList<MemoryEntry>               _self;
	private LinkedList<Map<String,MemoryEntry>>   _visual;
	private LinkedList<Map<String,AuditoryEntry>> _auditory;
	private LinkedList<Map<String,FoodEntry>>     _scent;
	
	private Map<String,Map<String,CollisionEntry>> _collisions;
	
	// more complex calculated
	private Set<String> _approaching;
	private Map<String,Long> _novelSet;
	
	private FluentStore  _fluentStore;
	
	private long _lastSpeedChange;
	
	public AgentSpace(String name) { 
		_name = name;
		
		_lastSpeedChange = 0L;
		_collisions = new HashMap<String,Map<String,CollisionEntry>>();

		_self = new LinkedList<MemoryEntry>();
		_visual = new LinkedList<Map<String,MemoryEntry>>();
		_auditory = new LinkedList<Map<String,AuditoryEntry>>();
		_scent = new LinkedList<Map<String,FoodEntry>>();
		_approaching = new HashSet<String>();
		_novelSet = new HashMap<String,Long>();
		
		if (GameGlobals.record) {  
			_fluentStore = new FluentStore(_name);
		
			// set up some of the default parameters that we want
			// to keep an eye on possibly.
			AgentHelper.recordSystem(this);

			// Recording takes place in the postUpdate method.
			EventManager.inst().registerForAll(UpdateEnd.class, new EventListener() { 
				@Override
				public void onEvent(Event e) { 
					postUpdate();
				}
			});
		}
		
		EventManager.inst().registerForAll(UpdateStart.class, new EventListener() {
			@Override
			public void onEvent(Event e) {
				preUpdate();
			} 
		});		
	}
	
	public FluentStore getFluentStore() { 
		return _fluentStore;
	}
	
	/**
	 * Record the information about ourself so that we can determine
	 * interesting stuff about ourself
	 * @param us
	 */
	public void addSelfExperience(PhysicsObject us) { 
		_self.addFirst(new MemoryEntry(us));
		if (_self.size() > 7) 
			_self.removeLast();
	}
	
	/**
	 * Record the fact that we've "seen" all of the given object
	 * this time step. This set includes ourself
	 * @param objects
	 */
	public void addVisualExperience(PhysicsObject us, Set<PhysicsObject> objects) {
		// grab the object space so that we can make sure to record the distance
		// between ourselves and the other agents that we see
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		Map<String,MemoryEntry> map = new HashMap<String,MemoryEntry>();
		
		for (PhysicsObject obj : objects) { 
			MemoryEntry memory = new MemoryEntry(obj);
			map.put(obj.getName(), memory);

			// make sure to record the distance
			objectSpace.findOrAddDistance(us, obj);
		}

		_visual.addFirst(map);
		
		// Keep the memories a fixed size thanks to George Miller
		if (_visual.size() > 7) { 
			_visual.removeLast();
		}
	}

	/**
	 * Record the fact that we've "heard" all of the given objects this
	 * time step.  This set doesn't include ourself.
	 * @param objects
	 */
	public void addAuditoryExperience(PhysicsObject us, Set<PhysicsObject> objects) { 
		// grab the object space so that we can make sure to record the distance
		// between ourselves and the other agents that we see
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

		Map<String,AuditoryEntry> map = new HashMap<String,AuditoryEntry>();
		
		for (PhysicsObject obj : objects) { 
			if (obj == us) { 
				throw new RuntimeException("This set shouldn't contain us");
			}
			AuditoryEntry memory = new AuditoryEntry(us, obj);
			map.put(obj.getName(), memory);
			
			// make sure to record the distance
			objectSpace.findOrAddDistance(us, obj);
		}

		_auditory.addFirst(map);
		
		// Keep the memories a fixed size thanks to George Miller
		if (_auditory.size() > 7) { 
			_auditory.removeLast();
		}
	}
	
	/**
	 * Record the fact that we've "smelt" all of the given food objects
	 * this time step.
	 * @param us
	 * @param food
	 */
	public void addScentExperience(PhysicsObject us, Set<PhysicsObject> food) { 
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		Map<String,FoodEntry> map = new HashMap<String,FoodEntry>();
		
		for (PhysicsObject obj : food) { 
			FoodEntry foodEntry = new FoodEntry(obj);
			map.put(obj.getName(), foodEntry);
			
			// make sure to record the distance
			objectSpace.findOrAddDistance(us, obj);
		}

		_scent.addFirst(map);
		
		// Keep the memories a fixed size thanks to George Miller
		if (_scent.size() > 7) { 
			_scent.removeLast();
		}		
	}
	
	public void add(CollisionEntry entry) { 
		PhysicsObject obj = entry.getObject1();
		if (obj.getName().equals(_name)) { 
			obj = entry.getObject2();
		}

		// check to see if we already have some collisions with this agent.
		Map<String,CollisionEntry> map = _collisions.get(obj.getName());
		if (map == null) { 
			map = new HashMap<String,CollisionEntry>();
			_collisions.put(obj.getName(), map);
		}
		map.put(entry.getId(), entry);
	}
	
	public void persist(ContactPoint cp) { 
		PhysicsObject obj1 = (PhysicsObject) cp.shape1.getUserData();
		PhysicsObject obj2 = (PhysicsObject) cp.shape2.getUserData();

		PhysicsObject obj = obj1;
		if (obj.getName().equals(_name)) { 
			obj = obj2;
		}

		// TODO validate my assumption is that persist will actually change the position
		// velocity and normal of the collision without needing to generate
		// a new collision point.
		Map<String,CollisionEntry> map = _collisions.get(obj.getName());
		CollisionEntry entry = map.get(cp.id.features.toString());
		entry.update(cp);
	}
	
	public void remove(ContactPoint cp, PhysicsObject obj1, PhysicsObject obj2) { 
		PhysicsObject obj = obj1;
		if (obj.getName().equals(_name)) { 
			obj = obj2;
		}

		// check to see if we already have some collisions with this agent.
		Map<String,CollisionEntry> map = _collisions.get(obj.getName());
		map.remove(cp.id.features.toString());
	}
	
	public CollisionEntry getCollisionEntry(PhysicsObject obj, String id) { 
		Map<String,CollisionEntry> map = _collisions.get(obj.getName());
		return map.get(id);
	}
	
	/**
	 * Return the collection of collisions.  Builds this
	 * list on the fly, so call sparingly.
	 * @return
	 */
	public Collection<CollisionEntry> getCollisions() { 
		List<CollisionEntry> list = new ArrayList<CollisionEntry>();
		for (Map<String,CollisionEntry> map : _collisions.values()) { 
			list.addAll(map.values());
		}
		return list;
	}
	
	public boolean isCollidingWith(PhysicsObject obj) {
		Map<String,CollisionEntry> map = _collisions.get(obj.getName());
		if (map == null)
			return false;
		
		return map.size() > 0;
	}
	
	/**
	 * Called before every update so that we can prepare, i.e. remove
	 * everything from the distance map, since it is a cache map anyways.
	 */
	public void preUpdate() { 
		_approaching.clear();
		
		for (ValueEntry entry : _map.values()) { 
			entry.preUpdate();
		}
		
		for (BoundedEntry entry : _bounded.values()) {
			entry.preUpdate();
		}
	}	
	
	public LinkedList<Map<String,MemoryEntry>> getVisualMemories() { 
		return _visual;
	}
	
	public LinkedList<MemoryEntry> getSelfMemory() { 
		return _self;
	}
	
	public LinkedList<Map<String,AuditoryEntry>> getAuditoryMemories() { 
		return _auditory;
	}
	
	public LinkedList<Map<String,FoodEntry>> getScentMemories() { 
		return _scent;
	}
	
	public Set<String> getApproaching() { 
		return _approaching;
	}
	
	/**
	 * Record the name of the agent that is approaching this
	 * agent.
	 * @param name
	 */
	public void addApproaching(String name) { 
		_approaching.add(name);
	}
	
	/**
	 * Is the given agent approaching us?
	 * @param name
	 * @return
	 */
	public boolean isApproaching(String name) { 
		return _approaching.contains(name);
	}

	/**
	 * Get the novel items from this agent space.
	 * @return
	 */
	public Map<String,Long> getNovelItems() { 
		return _novelSet;
	}
	
	/**
	 * Set the novel items in this agent space
	 * @param novel
	 */
	public void setNovelItems(Map<String,Long> novel) { 
		_novelSet = novel;
	}
	
	/**
	 * Called after every update so that we can record any information 
	 * that needs to be recorded before it is cleared or changed.
	 */
	public void postUpdate() { 
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		Set<String> distanceRecorded = new HashSet<String>();
		Set<String> visibleSet = new HashSet<String>();
		
		MemoryEntry ours = _self.getFirst();
		ours.record(_fluentStore);
		
		_fluentStore.record("goal", ours.obj.getName(), get(Variable.goal).get(String.class));
		_fluentStore.record("state", ours.obj.getName(), get(Variable.state).get(String.class));
		
		for (MemoryEntry memory : _visual.getFirst().values()) { 
			visibleSet.add(memory.obj.getName());
			String name = ours.obj.getName() + " " + memory.obj.getName();
			memory.record(_fluentStore);
			
			// record some relational information
			Vec2 relativeVelocity = ours.velocity.sub(memory.velocity);
			_fluentStore.record("relativeVx", name, relativeVelocity.x);
			_fluentStore.record("relativeVy", name, relativeVelocity.y);
			
			// record the relational position since it may prove useful
			Vec2 relativePosition = ours.position.sub(memory.position);
			_fluentStore.record("relativeX", name, relativePosition.x);
			_fluentStore.record("relativeY", name, relativePosition.y);

			if (!distanceRecorded.contains(name)) {
				DistanceEntry current = objectSpace.findOrAddDistance(ours.obj, memory.obj);
				_fluentStore.record("distance", name, current.getDistance());
				distanceRecorded.add(name);
			}
		}
		
		for (AuditoryEntry memory : _auditory.getFirst().values()) { 
			String name = ours.obj.getName() + " " + memory.obj.getName();
			memory.record(_fluentStore, ours.obj);

			if (!distanceRecorded.contains(name)) {
				DistanceEntry current = objectSpace.findOrAddDistance(ours.obj, memory.obj);
				_fluentStore.record("distance", name, current.getDistance());
				distanceRecorded.add(name);
			}
		}
		
		for (FoodEntry memory : _scent.getFirst().values()) { 
			String name = ours.obj.getName() + " " + memory.obj.getName();
			memory.record(_fluentStore, ours.obj);

			if (!distanceRecorded.contains(name)) {
				DistanceEntry current = objectSpace.findOrAddDistance(ours.obj, memory.obj);
				_fluentStore.record("distance", name, current.getDistance());
				distanceRecorded.add(name);
			}
		}
		
		for (Map.Entry<String,Map<String,CollisionEntry>> entry : _collisions.entrySet()) { 
			Map<String,CollisionEntry> map = entry.getValue();
			if (map.size() == 0)
				continue;
			
			visibleSet.remove(entry.getKey());
			
			String name = ours.obj.getName() + " " + entry.getKey();
			_fluentStore.record("collision", name, true);

			// TODO record the collision details.... somehow...
			for (CollisionEntry ce : entry.getValue().values()) { 
				String id = name + " " + ce.getId().replaceAll("[,]", "");
				_fluentStore.record("collisionX", id, ce.getPosition().x);
				_fluentStore.record("collisionY", id, ce.getPosition().y);
				_fluentStore.record("collisionNormalX", id, ce.getNormal().x);
				_fluentStore.record("collisionNormalY", id, ce.getNormal().y);
				_fluentStore.record("collisionVX", id, ce.getVelocity().x);
				_fluentStore.record("collisionVY", id, ce.getVelocity().y);
			}
		}
		
		// those that we can see that we are not colliding with
		for (String name : visibleSet) {
			_fluentStore.record("collision", ours.obj.getName() + " " + name, false);
		}
		
		for (String name : _approaching) { 
			_fluentStore.record("approaching", ours.obj.getName() + " " + name, true);
		}
		
		// record the values stored in each of the personality traits.
		// additionally record the values stored that dynamically change.
		_fluentStore.record("openness", ours.obj.getName(), get(Variable.openness).get(Float.class));
		_fluentStore.record("conscientiousness", ours.obj.getName(), get(Variable.conscientiousness).get(Float.class));
		_fluentStore.record("extroversion", ours.obj.getName(), get(Variable.extroversion).get(Float.class));
		_fluentStore.record("agreeableness", ours.obj.getName(), get(Variable.agreeableness).get(Float.class));
		_fluentStore.record("neuroticism", ours.obj.getName(), get(Variable.neuroticism).get(Float.class));

		for (String name : _novelSet.keySet()) { 
			_fluentStore.record("novel", ours.obj.getName() + " " + name, true);
		}
		
		_fluentStore.update();
	}
	
	
	/**
	 * Change the modifiers for turning and moving
	 * @param delta
	 * 	  the amount to increase the modifiers
	 * @param min
	 *    the minimum amount that the modifier can take on
	 * @param max
	 *    the max amount that the modifier can take on
	 */
	public void changeSpeed(float delta, float min, float max) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		ValueEntry currentFrame = systemSpace.get(Variable.logicalTime);
//		if (currentFrame.get(Long.class) - _lastSpeedChange < 60)
//			return;
		
		float v = 0;
		
		ValueEntry turn = get(Variable.turnModifier);
		v = turn.get(Float.class) + delta; 
		v = Math.min(v, max);
		v = Math.max(v, min);
		turn.setValue(v); 
		
		ValueEntry move = get(Variable.moveModifier);
		v = move.get(Float.class) + delta; 
		v = Math.min(v, max);
		v = Math.max(v, min);
		move.setValue(v);
		
		// record that we updated the speed.
		_lastSpeedChange = currentFrame.get(Long.class);
	}	
	
	/**
	 * Set the modifiers for the agent.
	 * @param value
	 */
	public void setSpeed(float value) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		ValueEntry currentFrame = systemSpace.get(Variable.logicalTime);
//		if (currentFrame.get(Long.class) - _lastSpeedChange < 60)
//			return;

		ValueEntry turn = get(Variable.turnModifier);
		turn.setValue(value);

		ValueEntry move = get(Variable.moveModifier);
		move.setValue(value);
		// record that we updated the speed.
		_lastSpeedChange = currentFrame.get(Long.class);
	}
}
