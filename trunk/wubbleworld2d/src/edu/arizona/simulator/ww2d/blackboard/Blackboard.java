package edu.arizona.simulator.ww2d.blackboard;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.blackboard.control.ConsoleControl;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.blackboard.updates.CollisionUpdater;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Blackboard {
    private static Logger logger = Logger.getLogger( Blackboard.class );

    private static Blackboard _bb;
	
	private int _nextId = 0;
	
	private ConsoleControl    _control;
	private Map<String,Space> _spaces;
	
	private CollisionUpdater  _updates;
		
	private Blackboard() { 
		_control = new ConsoleControl();
		_spaces = new HashMap<String,Space>();
		_updates = new CollisionUpdater();
	}
	
	public static Blackboard inst() { 
		if (_bb == null) {
			logger.debug("Constructing a new blackboard");
			_bb = new Blackboard();
		}
		return _bb;
	}
	
	public int getNextId() { 
		return _nextId++;
	}
	
	public void addSpace(String name, Space s) { 
		if (_spaces.containsKey(name)) { 
			logger.error("Adding an additional space with the same name " + name);
		}
		_spaces.put(name, s);
	}

	/**
	 * Get the space associated with a specific name.
	 * @param name - the name of the space that you would like
	 * @return
	 */
	public Space getSpace(String name) { 
		Space s = _spaces.get(name);
		if (s == null) 
			throw new RuntimeException("Unknown space: " + name);
		return s;
	}
	
	/**
	 * Get the space associated with a specific name.
	 * @param c  - the class you would like the space cast into
	 * @param name - the name of the space you would like
	 * @return
	 */
	public <T extends Space> T getSpace(Class<T> c, String name) { 
		Space s = _spaces.get(name);
		if (s == null) 
			throw new RuntimeException("Unknown space: " + name);
		return c.cast(s);
	}
	
	/**
	 * Determine if this space exists.
	 * @param name
	 * @return
	 */
	public boolean spaceExists(String name) { 
		return _spaces.get(name) != null;
	}
	
	/**
	 * Query the blackboard for the object stored 
	 * @param <T>
	 * @param spaceName
	 * @param v
	 * @param objClass
	 * @return
	 */
	public <T> T query(String spaceName, Variable v, Class<T> objClass) {
		Space s = getSpace(spaceName);
		return s.get(v).get(objClass);
	}

	/**
	 * Update the control and with it the knowledge sources.
	 * @param elapsed
	 */
	public void update(int elapsed) { 
		_control.update(elapsed);
	}

	/**
	 * The easiest way to finish is to just start over.
	 */
	public void finish() { 
		_bb = null;
	}
}
