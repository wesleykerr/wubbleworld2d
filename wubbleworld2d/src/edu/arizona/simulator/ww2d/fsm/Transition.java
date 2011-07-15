package edu.arizona.simulator.ww2d.fsm;


public class Transition {

	protected State _start;
	protected State _end;
	
	protected TransitionTest _test;
	
	/**
	 * Construct an transition with initial start and end states.
	 * @param start
	 * @param end
	 * @param test
	 */
	public Transition(State start, State end, TransitionTest test) { 
		_start = start;
		_end = end;
		
		_test = test;
	}
	
	/**
	 * Gets the end state of this transition.
	 * @return
	 */
	public State getStartState() { 
		return _start;
	}
	
	/**
	 * Gets the start state of this transition.
	 * @return
	 */
	public State getEndState() { 
		return _end;
	}
	
	/**
	 * Test this transition by calling the testing function.
	 * @param g
	 * @return
	 */
	public boolean test(FSM fsm) { 
		return _test.test(fsm, _start);
	}
}
