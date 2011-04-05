package edu.arizona.simulator.ww2d.logging;

import java.util.HashMap;
import java.util.Map;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.system.FinishEvent;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.MathUtils;

/**
 * This class will maintain a list of references to fluents
 * that are currently open and do majority of the bookkeeping 
 * for me.
 * @author wkerr
 *
 */
public class FluentStore {
	private StateDatabase                  _db;
	private Map<String,Map<String,Fluent>> _fluentMap;
	private long                           _lastCommit;
	private long                           _deltaValue;

	public FluentStore(String dbName) { 
		_db = new StateDatabase(dbName);
		_fluentMap = new HashMap<String,Map<String,Fluent>>();
		_lastCommit = System.currentTimeMillis();
		_deltaValue = 1000 + MathUtils.random.nextInt(500);
		
		EventManager.inst().registerForAll(FinishEvent.class, new EventListener() {
			@Override
			public void onEvent(Event e) {
				for (Map<String,Fluent> map : _fluentMap.values()) { 
					for (Fluent f : map.values()) { 
						f.close();
					}
				}
				_db.commit();
				_db.disconnect();
			} 
		});
	}
	
	/**
	 * Return the database that is the backend of this FluentStore
	 * @return
	 */
	public StateDatabase getDB() { 
		return _db;
	}
	
	
	/** 
	 * Called once you have finished recording the fluents
	 * that you are interested in.  This sets values to 
	 * unknown when we haven't recorded anything about them.
	 */
	public void update() { 
		for (Map<String,Fluent> map : _fluentMap.values()) { 
			for (Fluent f : map.values()) { 
				f.postUpdate();
			}
		}
		
		long time = System.currentTimeMillis();
		if (time - _deltaValue > _lastCommit) { 
			_db.commit();
			_lastCommit = time;
		}

	}

	/**
	 * Record the latest value for the given fluent and entities.
	 * @param fluentName
	 * @param entitiesName
	 * @param value
	 */
	public void record(String fluentName, String entitiesName, Object value) { 
		record(fluentName, entitiesName, false, value);
	}
	
	/**
	 * Record the latest value for the given fluent and entities.
	 * @param fluentName
	 * @param entitiesName
	 * @param value
	 */
	public void record(String fluentName, String entitiesName, boolean nonChanging, Object value) { 
		recordw(fluentName, entitiesName, nonChanging, value, "unknown");
	}
	
	/**
	 * Record the latest value for the give fluent and entities.  Provide a default
	 * value if you want something specific other than "unknown";
	 * @param fluentName
	 * @param entitiesName
	 * @param value
	 * @param defaultValue
	 */
	public void recordw(String fluentName, String entitiesName, Object value, Object defaultValue) { 
		recordw(fluentName, entitiesName, false, value, defaultValue);
	}
	
	/**
	 * This record will default to a given value when nothing is recorded.  Useful
	 * for the global recording of events.
	 * @param fluentName
	 * @param entitiesName
	 * @param value
	 * @param defaultValue
	 */
	public void recordw(String fluentName, String entitiesName, boolean nonChanging, Object value, Object defaultValue) { 
		Map<String,Fluent> map = _fluentMap.get(fluentName);
		if (map == null) { 
			map = new HashMap<String,Fluent>();
			_fluentMap.put(fluentName, map);
		}
		
		Fluent fluent = map.get(entitiesName);
		// this should always return true, unless you want to
		// register multiple times.  The default value will
		// be unknown.
		if (fluent == null) { 
			fluent = new Fluent(_db, fluentName, entitiesName, nonChanging, value, defaultValue);
			map.put(entitiesName, fluent);
		} else { 
			fluent.update(value);
		}
	}
	
	
}
