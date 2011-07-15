package edu.arizona.simulator.ww2d.fsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.utils.MathUtils;

public class FSM {
    private static Logger logger = Logger.getLogger( FSM.class );

    private String _name;
    
    private State _startState;
	private State _activeState;
	
	private Map<State,List<Transition>> _stateMap;
	
	private Map<String,Object> _userData;
	
	public FSM(String name) { 
		_name = name;
		
		_activeState = null;
		_stateMap = new HashMap<State,List<Transition>>();
		_userData = new HashMap<String,Object>();
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
	 * Get the given name of this FSM
	 * @return
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * gets the user object for this  FSM
	 * @return
	 */
	public Object getUserData(String name) { 
		return _userData.get(name);
	}
	
	/**
	 * stores a object that may be useful to the FSM
	 * @param userObject
	 */
	public void setUserData(String name, Object userObject) { 
		_userData.remove(name);
		_userData.put(name, userObject);
	}
	
	/**
	 * Return the active state of this FSM.
	 * @return
	 */
	public State getActiveState() {
		return _activeState;
	}
	
	/**
	 * Sets up the start state for the FSM.
	 * @param s
	 */
	public void setStartState(State s) { 
		_startState = s;
		_activeState = s;
	}
	
	/**
	 * Called when we are ready to start processing
	 * this FSM.
	 */
	public void activate() { 
		_activeState.enterState(this);
	}
	
	/**
	 * Reset the finite state machine so that we are back
	 * at the start state.
	 */
	public void reset() { 
		_activeState.exitState(this);
		_activeState = _startState;
	}
	
	/**
	 * Adds a new state to the FSM being defined.
	 * @param s
	 */
	public void addState(State s) { 
		if (_stateMap.containsKey(s)) { 
			logger.warn("State already added: " + s);
			return;
		}
		logger.debug(_name + " Adding State [" + s.getName() + "]");
		_stateMap.put(s, new LinkedList<Transition>());
	}
	
	/**
	 * Adds a transition to the FSM being defined.
	 * @param t
	 */
	public void addTransition(State s, State e, TransitionTest tt) { 
		if (!_stateMap.containsKey(s)) {
			logger.warn("Adding a transition without first adding the state: " + s);
			addState(s);
		}
		
		if (!_stateMap.containsKey(e)) { 
			logger.warn("Adding a transition without first adding the state: " + e);
			addState(e);
		}
		
		logger.debug(_name + " Adding Transition [" + s.getName() + " -> " + e.getName() + "]");
		Transition t = new Transition(s, e, tt);
		_stateMap.get(s).add(t);
	}
	
	/**
	 * Updates the FSM.
	 * @param delta - the number of milliseconds since last update
	 */
	public void update(long delta) { 
		if (_activeState == null) { 
			logger.warn("Calling update on a FSM without an active state");
			return;
		}
		
		_activeState.update(this, delta);
		List<Transition> transitions = _stateMap.get(_activeState);
		List<Transition> activeList = new ArrayList<Transition>();
		for (Transition t : transitions) { 
			if (t.test(this)) { 
				logger.debug(_name + " **** " + _activeState.getName() + "->" + t.getEndState().getName());
				activeList.add(t);
			}
		}

		if (activeList.size() == 0)
			return;
		
		int index = 0;
		if (activeList.size() > 1)  
			index = MathUtils.random.nextInt(activeList.size());

//		logger.debug("[FSM] exiting " + _activeState.getName());
		_activeState.exitState(this);
		_activeState = activeList.get(index).getEndState();
		_activeState.enterState(this);
//		logger.debug("[FSM] entering " + _activeState.getName());
	}
}
