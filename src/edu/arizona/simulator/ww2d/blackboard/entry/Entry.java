package edu.arizona.simulator.ww2d.blackboard.entry;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;

/**
 * Every entry in the Blackboard knows whether it has
 * been modified during the current time tick.  It is up
 * to the actual entry to correctly update the boolean 
 * flag, but it is reset to false for everything at the beginning
 * of an update.
 * @author wkerr
 *
 */
public class Entry {
	private int _id;
	private boolean _updated;
	
	public Entry() { 
		_id = Blackboard.inst().getNextId();
	}
	
	public void preUpdate() { 
		_updated = false;
	}
	
	public void updated() { 
		_updated = true;
	}
	
	public boolean hasUpdated() { 
		return _updated;
	}
}
