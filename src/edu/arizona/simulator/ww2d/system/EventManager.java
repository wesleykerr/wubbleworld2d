package edu.arizona.simulator.ww2d.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.object.GameObject;

/**
 * @author wkerr
 *
 */
public class EventManager {
	   private static Logger logger = Logger.getLogger( EventManager.class.getName() );

		private static EventManager _mgr = null;
		
		private Queue<Event> _eventQueue;

		/** Listen for events of a given type routed to a specific game object */
		private Map<Class<? extends Event>,Map<String,List<EventListener>>> _unique;
		
		/** Listen for any events of a given type. */
		private Map<Class<? extends Event>,List<EventListener>> _all;
		
		private EventManager() { 
			_eventQueue = new LinkedList<Event>();
			_unique = new HashMap<Class<? extends Event>,Map<String,List<EventListener>>>();
			_all = new HashMap<Class<? extends Event>,List<EventListener>>();
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
		public void registerForAll(Class<? extends Event> eventClass, EventListener callback) { 
			List<EventListener> list = _all.get(eventClass);
			if (list == null) { 
				list = new ArrayList<EventListener>();
				_all.put(eventClass, list);
			}
			list.add(0, callback);
		}
		
		/**
		 * Register for a subset of events for a given type.  The subset
		 * includes any events that should be forwarded to the specific GameObject
		 * @param event
		 * @param obj
		 * @param callback
		 */
		public void register(Class<? extends Event> eventClass, GameObject obj, EventListener callback) {
			Map<String,List<EventListener>> map = _unique.get(eventClass);
			if (map == null) { 
				map = new HashMap<String,List<EventListener>>();
				_unique.put(eventClass, map);
			}
			
			List<EventListener> listeners = map.get(obj.getName());
			if (listeners == null) { 
				listeners = new ArrayList<EventListener>();
				map.put(obj.getName(), listeners);
				logger.info("Registering: " + obj.getName() + " " + eventClass.getSimpleName());
			}
			listeners.add(callback);
		}
		
		/**
		 * Unregister for events.  Most likely occurs when an object dies
		 * or is removed from the game.
		 * @param event
		 * @param obj
		 * @param callback
		 */
		public void unregister(Class<? extends Event> eventClass, GameObject obj, EventListener callback) { 
			Map<String,List<EventListener>> map = _unique.get(eventClass);
			if (map == null) { 
				logger.severe("Trying to unregister when you never registered " + eventClass.getSimpleName());
				return;
			}

			List<EventListener> listeners = map.get(obj.getName());
			if (listeners == null) { 
				logger.severe("Trying to unregister when you never registered " + eventClass.getSimpleName() + " - " + obj.getName());
			} else { 
				listeners.remove(callback);
			}
		}
		
		/**
		 * Dispatch an event by adding it to the event queue.
		 * @param e
		 */
		public void dispatch(Event e) { 
			_eventQueue.add(e);
		}
		
		/**
		 * Directly route this message to the people who need it.
		 * Should only be called internally.
		 * @param e
		 */
		public void dispatchImmediate(Event e) { 
			List<EventListener> list = _all.get(e.getClass());
			if (list != null) { 
				for (EventListener callback : list) { 
					callback.onEvent(e);
				}
			}

			// Now dispatch to all of the individuals only interested in
			// this message if it belongs to them.
			List<GameObject> recipients = e.getRecipients();
			Map<String,List<EventListener>> map = _unique.get(e.getClass());
			if (map != null) { 
				for (GameObject obj : recipients) { 
//					logger.debug("\tRecipient: " + obj.getName());
					List<EventListener> listeners = map.get(obj.getName());
					if (listeners != null) { 
						// Send it out to all the callbacks listening to this id.
						for (EventListener callback : listeners) { 
							callback.onEvent(e);
						}
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
//				logger.debug("Dispatching: " + event.getId());
				dispatchImmediate(event);
			}
			
			_eventQueue.addAll(keep);
		}
		
		public void finish() { 
			_mgr = null;
		}
}
