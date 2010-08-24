package edu.arizona.simulator.ww2d.blackboard.spaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.blackboard.entry.DistanceEntry;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;

public class ObjectSpace extends Space {
    private static Logger logger = Logger.getLogger( ObjectSpace.class );
	
	private Map<String,GameObject> _gameObjects;
	private Map<String,PhysicsObject> _physicsObjects;
	private Map<String,PhysicsObject> _dynamicObjects;

	private List<PhysicsObject> _cognitiveAgents;
	
	private List<GameObject> _renderObjects;
	
	private LinkedList<Map<String,Map<String,DistanceEntry>>> _distanceMemory;
	
	public ObjectSpace() { 
		_renderObjects = new LinkedList<GameObject>();
		
		_gameObjects = new HashMap<String,GameObject>();
		_physicsObjects = new HashMap<String,PhysicsObject>();
		_dynamicObjects = new HashMap<String,PhysicsObject>();
		_cognitiveAgents = new ArrayList<PhysicsObject>();
		
		_distanceMemory = new LinkedList<Map<String,Map<String,DistanceEntry>>>();
		
		EventManager.inst().registerForAll(EventType.UPDATE_START, new EventListener() {
			@Override
			public void onEvent(Event e) {
				preUpdate();
			} 
		});
	}
	
	/**
	 * Query this space for the object that 
	 * you are looking for.  
	 * @param <T>
	 * @param c
	 * @param name
	 * @return
	 */
	public <T extends GameObject> T query(Class<T> c, String name) { 
		if (c == GameObject.class) { 
			return c.cast(_gameObjects.get(name));
		} else if (c == PhysicsObject.class) { 
			return c.cast(_physicsObjects.get(name));
		} else { 
			throw new RuntimeException("Unknown class: " + c);
		}
	}

	public void add(GameObject obj) { 
//		logger.debug("Adding a game object: " + obj.getName());
		
		// add it to the set of objects that we will be rendering.
		// and resort them just in case.
		_renderObjects.add(obj);
		Collections.sort(_renderObjects, GameObject.render);

		if (_gameObjects.containsKey(obj.getName())) { 
			logger.error("Already added " + obj.getName() + " to the object space");
		} else { 
			_gameObjects.put(obj.getName(), obj);
		}
		
		if (obj instanceof PhysicsObject) { 
			PhysicsObject pobj = (PhysicsObject) obj;
			if (_physicsObjects.containsKey(obj.getName())) { 
				logger.error("Already added " + obj.getName() + " to the object space");
			} else { 
				_physicsObjects.put(obj.getName(), pobj);
			}
			
			// if it's mass is greater than 0 then it
			// is a Dynamic body
			if (pobj.getBody().getMass() > 0) { 
				_dynamicObjects.put(obj.getName(), pobj);
			}
			
			if (pobj.getType() == ObjectType.cognitiveAgent)
				_cognitiveAgents.add(pobj);
		}
	}
	
	public void remove(GameObject obj) { 
		_renderObjects.remove(obj);
		_gameObjects.remove(obj.getName());
		
		// blindly remove it from everything since
		// we don't expect the name to be shared 
		_physicsObjects.remove(obj.getName());
		_dynamicObjects.remove(obj.getName());
	}
	
	public Collection<GameObject> getRenderObjects() { 
		return _renderObjects;
	}
	
	/**
	 * Return all of the dynamic objects in the world.
	 * @return
	 */
	public Collection<PhysicsObject> getDynamicObjects() { 
		return _dynamicObjects.values();
	}	
	
	public Collection<GameObject> getAll() { 
		return _gameObjects.values();
	}
	
	public Collection<PhysicsObject> getPhysicsObjects() { 
		return _physicsObjects.values();
	}
	
	public PhysicsObject getPhysicsObject(String name) { 
		return _physicsObjects.get(name);
	}
	
	public List<PhysicsObject> getCognitiveAgents() { 
		return _cognitiveAgents;
	}
	
	/**
	 * Called before every update so that we can prepare, i.e. remove
	 * everything from the distance map, since it is a cache map anyways.
	 */
	public void preUpdate() { 
		Map<String,Map<String,DistanceEntry>> distances = new HashMap<String,Map<String,DistanceEntry>>();
		for (PhysicsObject obj : _physicsObjects.values()) { 
			distances.put(obj.getName(), new HashMap<String,DistanceEntry>());
		}
		_distanceMemory.addFirst(distances);
		
		if (_distanceMemory.size() > 7) 
			_distanceMemory.removeLast();
	}

	/**
	 * Find the distance between the given objects.  If it doesn't
	 * exist then query it from the world.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public DistanceEntry findOrAddDistance(PhysicsObject obj1, PhysicsObject obj2) { 
		try {
			Map<String,Map<String,DistanceEntry>> distances = _distanceMemory.getFirst();
			Map<String,DistanceEntry> map = distances.get(obj1.getName());
			DistanceEntry entry = map.get(obj2.getName());
			if (entry == null) { 
				entry = new DistanceEntry(obj1, obj2);

				map.put(obj2.getName(), entry);

				map = distances.get(obj2.getName());
				if (map == null) { 
					logger.debug("Unknown object: " + obj2.getName());
				} else { 
					map.put(obj1.getName(), entry);
				}
			} 
			return entry;
		} catch (Exception e) { 
			String str = "Object 1: " + obj1 + " or Object 2: " + obj2 + " is unknown";
			throw new RuntimeException("ERROR: " + str);
		}
	}
	
	public DistanceEntry getDistance(PhysicsObject obj1, PhysicsObject obj2, int index) { 
		Map<String,Map<String,DistanceEntry>> distances = _distanceMemory.get(index);
		Map<String,DistanceEntry> map = distances.get(obj1.getName());
		DistanceEntry entry = map.get(obj2.getName());
		if (entry == null)
			throw new RuntimeException("Distance doesn't exist for " + obj1.getName() + " " + obj2.getName() + " " + index);
		return entry;
	}
}

