package edu.arizona.simulator.ww2d.blackboard.spaces;

import java.util.HashMap;
import java.util.Map;

import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Space {

	protected Map<Variable,ValueEntry> _map;
	protected Map<Variable,BoundedEntry> _bounded;
	
	public Space() { 
		_map = new HashMap<Variable,ValueEntry>();
		_bounded = new HashMap<Variable,BoundedEntry>();
	}

	public void put(Variable key, ValueEntry v) { 
		if (_map.containsKey(key)) { 
			System.err.println("Overriding entry for key: " + key);
		}
		_map.put(key, v);
	}
	
	public void put(Variable key, BoundedEntry b) { 
		if (_bounded.containsKey(key)) { 
			System.err.println("Overriding entry for key: " + key);
		}
		_bounded.put(key, b);
	}
	
	public ValueEntry get(Variable key) { 
		return _map.get(key);
	}
	
	public BoundedEntry getBounded(Variable key) { 
		return _bounded.get(key);
	}
}
