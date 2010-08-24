package edu.arizona.simulator.ww2d.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.enums.EventType;

/**
 * @author wkerr
 *
 */
public class EventManager {
    private static Logger logger = Logger.getLogger( EventManager.class );

	private static EventManager _mgr = null;
	
	private Queue<Event> _eventQueue;

	/** Listen for events of a given type routed to a specific game object */
	private Map<EventType,Map<String,List<EventListener>>> _unique;
	
	/** Listen for any events of a given type. */
	private Map<EventType,List<EventListener>> _all;
	
	private EventManager() { 
		_eventQueue = new LinkedList<Event>();
		_unique = new HashMap<EventType,Map<String,List<EventListener>>>();
		_all = new HashMap<EventType,List<EventListener>>();
		
		for (EventType type : EventType.values()) { 
			_unique.put(type, new TreeMap<String,List<EventListener>>());
			_all.put(type, new ArrayList<EventListener>());
		}
	}
	
	public static EventManager inst() {
		if (_mgr == null)
			_mgr = new EventManager();
		return _mgr;
	}
	
	/**
	 * Register for all events of a given type.
	 * @param event
	 * @param callback
	 */
	public void registerForAll(EventType event, EventListener callback) { 
		_all.get(event).add(0, callback);
	}
	
	/**
	 * Register for a subset of events for a given type.  The subset
	 * includes any events that should be forwarded to the specific GameObject
	 * @param event
	 * @param obj
	 * @param callback
	 */
	public void register(EventType event, GameObject obj, EventListener callback) {
		List<EventListener> listeners = _unique.get(event).get(obj.getName());
		if (listeners == null) { 
			listeners = new ArrayList<EventListener>();
			_unique.get(event).put(obj.getName(), listeners);
//			logger.debug("Registering: " + obj.getName() + " " + event);
		}
		listeners.add(callback);
	}
	
	public void unregister(EventType event, GameObject obj, EventListener callback) { 
		List<EventListener> listeners = _unique.get(event).get(obj.getName());
		if (listeners == null) { 
			logger.error("Trying to unregister when you never registered");
		}
	}
	
	public void dispatch(Event e) { 
		_eventQueue.add(e);
	}
	
	/**
	 * Directly route this message to the people who need it.
	 * Should only be called internally.
	 * @param e
	 */
	public void dispatchImmediate(Event e) { 
		for (EventListener callback : _all.get(e.getId())) { 
			callback.onEvent(e);
		}

		// Now dispatch to all of the individuals only interested in
		// this message if it belongs to them.
		List<GameObject> recipients = e.getRecipients();
		Map<String,List<EventListener>> map = _unique.get(e.getId());
		for (GameObject obj : recipients) { 
//			logger.debug("\tRecipient: " + obj.getName());
			List<EventListener> listeners = map.get(obj.getName());
			if (listeners != null) { 
				// Send it out to all the callbacks listening to this id.
				for (EventListener callback : listeners) { 
					callback.onEvent(e);
				}
			}
		}
	}
	
	/**
	 * Dispatch all of the messages that we've received ... that should go out.
	 * @param millis
	 */
	public void update(int millis) { 
		
		List<Event> copy = new LinkedList<Event>(_eventQueue);
		
		// remove everything from the queue in case new events are generated.
		_eventQueue.clear();
		
		List<Event> keep = new LinkedList<Event>();
		for (Event event : copy) { 
			// if we need to delay this event add it to the keep list.
//			logger.debug("Dispatching: " + event.getId());
			dispatchImmediate(event);
		}
		
		_eventQueue.addAll(keep);
	}
	
	public void finish() { 
		_mgr = null;
	}
}
