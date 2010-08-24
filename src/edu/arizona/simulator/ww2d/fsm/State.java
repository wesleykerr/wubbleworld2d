package edu.arizona.simulator.ww2d.fsm;

import java.util.HashMap;
import java.util.Map;

public abstract class State {

	protected String _name;
	protected Map<String,Object> _userData;
	
	public State(String name) { 
		_name = name;
		_userData = new HashMap<String,Object>();
	}
	
	/**
	 * Get the name of this state.
	 * @return
	 */
	public String getName() { 
		return _name;
	}
	
	/**
	 * sets the important content on this state
	 * @param content
	 */
	public void setUserData(String name, Object content) { 
		_userData.put(name, content);
	}
	
	/**
	 * gets the important content from this state
	 * @return
	 */
	public Object getContent(String name) { 
		return _userData.get(name);
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}

	@Override
	public String toString() {
		return _name;
	}

	/**
	 * We are entering this state in the next period.  Convenience
	 * method to update anything that only needs to occur once and
	 * shouldn't occur during the update.
	 */
	public abstract void enterState(FSM fsm);
	
	/**
	 * We are leaving this state so fix anything that needs to be
	 * fixed in here.
	 */
	public abstract void exitState(FSM fsm);
	
	/**
	 * Update the world after transition into this state.
	 * @param delta
	 */
	public abstract void update(FSM fsm, long delta);
}
