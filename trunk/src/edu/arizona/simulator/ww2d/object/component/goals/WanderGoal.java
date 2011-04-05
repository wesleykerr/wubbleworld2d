package edu.arizona.simulator.ww2d.object.component.goals;

import org.apache.commons.math.stat.StatUtils;
import org.apache.log4j.Logger;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.events.player.BehaviorWeightEvent;
import edu.arizona.simulator.ww2d.fsm.FSM;
import edu.arizona.simulator.ww2d.fsm.State;
import edu.arizona.simulator.ww2d.fsm.TransitionTest;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Align;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.ObstacleAvoidance;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Wander;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class WanderGoal implements Goal {
    private static Logger logger = Logger.getLogger( WanderGoal.class );

	private PhysicsObject _parent;
	private GoalEnum _status;

	private double[] _xQueue;
	private double[] _yQueue;
	private int _size;
	private int _count;
	
	private FSM _fsm;
	
	public WanderGoal(PhysicsObject obj) { 
		_parent = obj;
		_status = GoalEnum.inactive;
		
		_fsm = buildFSM();

		_size = 10;
		_xQueue = new double[_size];
		_yQueue = new double[_size];
	}
	
	@Override 
	public GoalEnum getStatus() { 
		return _status;
	}
	
	@Override
	public void activate() {
		logger.debug("activate");
		_status = GoalEnum.active;
		
		_fsm.activate();
	}

	@Override
	public GoalEnum process() {
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		space.get(Variable.state).setValue("wander");
		
		int index = _count % _size;
		_xQueue[index] = _parent.getPosition().x;
		_yQueue[index] = _parent.getPosition().y;		

		++_count;
		
		// now update the FSM
		_fsm.update(0);
		
		return GoalEnum.active;
	}

	@Override
	public void terminate() {		
		_fsm.reset();
	}
	
	@Override
	public boolean succeeding() { 
		return true;
	}
	
	@Override 
	public float desireability() { 
		return 0.05f;
	}
	
	
	private FSM buildFSM() { 
		State wander = new State("wander") {
			@Override
			public void enterState(FSM fsm) {
				AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
				space.get(Variable.turnModifier).setValue(1.0f);
				space.get(Variable.moveModifier).setValue(1.0f);

				EventManager.inst().dispatch(new BehaviorEvent(Wander.class, true, _parent));
				EventManager.inst().dispatch(new BehaviorEvent(ObstacleAvoidance.class, true, _parent));

				EventManager.inst().dispatch(new BehaviorWeightEvent(ObstacleAvoidance.class, 5.0f, _parent));
				_count = 0;
			}

			@Override
			public void exitState(FSM fsm) {
				logger.debug("deactivate");
				_status = GoalEnum.inactive;

				EventManager.inst().dispatch(new BehaviorEvent(Wander.class, false, _parent));
				EventManager.inst().dispatch(new BehaviorEvent(ObstacleAvoidance.class, false, _parent));
				
				EventManager.inst().dispatch(new BehaviorWeightEvent(ObstacleAvoidance.class, 1.0f, _parent));
			}

			@Override
			public void update(FSM fsm, long delta) { 

			} 
		};
		
		State turnAround = new State("turnAround") {
			@Override
			public void enterState(FSM fsm) {
				// we need to align ourselves in the complete opposite direction	
				BehaviorEvent event = new BehaviorEvent(Align.class, true, _parent);
				event.setTarget(_parent.getHeading() + (float) Math.PI);
				EventManager.inst().dispatch(event);
			}

			@Override
			public void exitState(FSM fsm) { 
				EventManager.inst().dispatch(new BehaviorEvent(Align.class, false, _parent));
			}

			@Override
			public void update(FSM fsm, long delta) {

			} 
		};
		
		// --------------------------------------------------------------------
		// now that all of the states are constructed, we need to connect
		// them with the proper transitions.
		
		TransitionTest movingTest = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				if (_count >= _size) { 
					double xVariance = StatUtils.variance(_xQueue);
					double yVariance = StatUtils.variance(_yQueue);
					if (xVariance < 0.02 && yVariance < 0.02) { 
						return true;
					}
				}
				return false;
			} 
		};		
		
		TransitionTest aligned = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				Object userData = _parent.getUserData("align");
				if (userData == null)
					return false;
				
				return (Boolean) userData;
			}
		};

		// --------------------------------------------------------------------
		
		FSM fsm = new FSM(_parent.getName() + "-wander");
		fsm.addState(wander);
		fsm.addState(turnAround);
		
		fsm.addTransition(wander, turnAround, movingTest);
		fsm.addTransition(turnAround, wander, aligned);
		
		fsm.setStartState(wander);
		return fsm;
	}	
}
