package edu.arizona.simulator.ww2d.object.component.goals;

import java.util.Map;

import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.FoodEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.events.player.RequestEatEvent;
import edu.arizona.simulator.ww2d.fsm.FSM;
import edu.arizona.simulator.ww2d.fsm.State;
import edu.arizona.simulator.ww2d.fsm.TransitionTest;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Align;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Arrive;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

/**
 * EatGoal
 * 
 *  Step 1. go to food
 *  Step 2. once we are colliding with food, begin eating it
 *  Step 3. once we are full, then the goal is complete
 * @author wkerr
 *
 */
public class EatGoal implements Goal {
    private static Logger logger = Logger.getLogger( EatGoal.class );

	private GoalEnum _status;
	
	private PhysicsObject _obj;
	private PhysicsObject _food;
	
	private FSM _fsm;
	
	public EatGoal(PhysicsObject obj) { 
		_obj = obj;
		_fsm = buildFSM();
		_status = GoalEnum.inactive;
	}

	@Override
	public float desireability() {
		// we only have the goal to eat when we see food
		// otherwise we are just wandering
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
		Map<String,FoodEntry> map = agentSpace.getScentMemories().getFirst();
		if (map == null || map.size() == 0) { 
			// we don't smell anything
			return 0;
		}
		
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
		BoundedEntry bounded = space.getBounded(Variable.energy);
		float pct = bounded.getValue() / bounded.getMax();
		return 1 - (pct*pct);
	}
	
	private void findFood() { 
		// we will move towards the closest food plot in case there are multiple.
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
		Map<String,FoodEntry> map = agentSpace.getScentMemories().getFirst();
		if (map == null || map.size() == 0) { 
			logger.error("Should not happen");
			return;
		}

		_food = null;
		float distance = 0;
		
		for (FoodEntry entry : map.values()) { 
			float d = _obj.getPPosition().sub(entry.obj.getPPosition()).length();
			if (_food == null || d < distance) { 
				_food = entry.obj;
				distance = d;
			}
		}
	}
	
	@Override
	public void activate() {
//		logger.debug("We are beginning to eat some food...");
		findFood();
		_status = GoalEnum.active;
		_fsm.reset();
		_fsm.activate();
	}

	@Override
	public GoalEnum process() {
		if (_food == null || _food.getUserData("store", Float.class) <= 0) {
			return GoalEnum.completed;
		}
		
		_fsm.update(0);

		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
		space.get(Variable.state).setValue(_fsm.getActiveState().getName());
		
		return _status;
	}

	@Override
	public void terminate() {
		_food = null;
		_fsm.reset();
	}
	
	@Override
	public boolean succeeding() {
		return true;
	}

	@Override
	public GoalEnum getStatus() {
		return _status;
	}

	private FSM buildFSM() { 
		State approach = new State("approach") {
			@Override
			public void enterState(FSM fsm) {
				fsm.setUserData("entered", System.currentTimeMillis());
				BehaviorEvent event = new BehaviorEvent(Arrive.class, true, _obj);
				event.setTarget(_food.getPPosition());
				EventManager.inst().dispatch(event);

				event = new BehaviorEvent(Align.class, true, _obj);
				event.setTarget(_food.getPPosition());
				EventManager.inst().dispatch(event);
				
				// Eventually if we fail to reach the food
				// then we need to plan a path to the food
			}

			@Override
			public void exitState(FSM fsm) {
				EventManager.inst().dispatch(new BehaviorEvent(Arrive.class, false, _obj));
				EventManager.inst().dispatch(new BehaviorEvent(Align.class, false, _obj));
			}

			@Override
			public void update(FSM fsm, long delta) {
			} 
		};
		
		State eat = new State("eat") {
			private long _lastEaten;
			private long _delay = 1500;
			
			@Override
			public void enterState(FSM fsm) {

			}

			@Override
			public void exitState(FSM fsm) {

			}

			@Override
			public void update(FSM fsm, long delta) {
				long time = System.currentTimeMillis();
				if (_lastEaten == 0 || time - _delay > _lastEaten) { 
//					logger.debug(_obj.getName() + " Eating nom nom nom");
					EventManager.inst().dispatch(new RequestEatEvent(_obj, _food));
					_lastEaten = time;
				}
			} 
		};
		
		State finish = new State("finish") {
			@Override
			public void enterState(FSM fsm) {
				_status = GoalEnum.completed;
			}

			@Override
			public void exitState(FSM fsm) {

			}

			@Override
			public void update(FSM fsm, long delta) {

			} 
		};
		
		// --------------------------------------------------------------------
		// now that all of the states are constructed, we need to connect
		// them with the proper transitions.
		TransitionTest arrivedTest = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				float distance = _obj.getPPosition().sub(_food.getPPosition()).length();
				
				if (distance <= 0.001f)
					return true;
				return false;
			} 
		};
		
		TransitionTest timeOut = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				long entered = (Long) fsm.getUserData("entered");
				long current = System.currentTimeMillis();
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
				if (agentSpace.isCollidingWith(_food) && current - 2000 > entered) {
					return true;
				}
				return false;
			} 
		};
		
		TransitionTest notTouching = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
				if (!agentSpace.isCollidingWith(_food))
					return true;

				return false;
			} 
		};
		
		TransitionTest isFull = new TransitionTest() { 
			@Override
			public boolean test(FSM fsm, State start) {
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
				BoundedEntry energy = agentSpace.getBounded(Variable.energy);
				if (Float.compare(energy.getValue(), energy.getMax()) == 0)
					return true;

				return false;
			} 
		};
		
		// --------------------------------------------------------------------
		
		FSM fsm = new FSM(_obj.getName() + "-eat");
		fsm.addState(approach);
		fsm.addState(eat);
		
		fsm.addTransition(approach, eat, arrivedTest);
		fsm.addTransition(approach, eat, timeOut);
		fsm.addTransition(eat, approach, notTouching);
		fsm.addTransition(eat, finish, isFull);
		
		fsm.setStartState(approach);
		return fsm;
	}
}
