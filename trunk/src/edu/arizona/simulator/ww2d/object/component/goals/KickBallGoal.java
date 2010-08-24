package edu.arizona.simulator.ww2d.object.component.goals;

import java.util.Map;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.MemoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.fsm.FSM;
import edu.arizona.simulator.ww2d.fsm.State;
import edu.arizona.simulator.ww2d.fsm.TransitionTest;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Align;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Arrive;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Seek;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class KickBallGoal implements Goal {
    private static Logger logger = Logger.getLogger( KickBallGoal.class );

	private GoalEnum _status;
	
	private PhysicsObject _parent;
	private PhysicsObject _ball;
	
	private FSM _fsm;
	
	public KickBallGoal(PhysicsObject parent) {
		_parent = parent;
		_fsm = buildFSM();
	}
	
	@Override
	public void activate() {
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		Map<String,MemoryEntry> map = space.getVisualMemories().getFirst();
		if (map.isEmpty()) {
			_status = GoalEnum.failed;
		}
		
		// see if there is a ball within these memories
		_ball = null;
		for (MemoryEntry entry : map.values()) { 
			if (entry.obj.getType() == ObjectType.dynamic || entry.obj.getType() == ObjectType.obstacle) {
				_ball = entry.obj;
				break;
			}
		}
		
		if (_ball == null) {
			_status = GoalEnum.failed;
			return;
		}
		
//		logger.debug("Activiting KickBall");
		// There is no reason to do a reset there since we should
		// have always kept things cleaned up.
		_status = GoalEnum.active;
		_fsm.activate();
		space.get(Variable.target).setValue(_ball.getName());
	}

	@Override
	public GoalEnum process() {
		_fsm.update(0);

		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		space.get(Variable.state).setValue(_fsm.getActiveState().getName());
		
		return _status;
	}

	@Override
	public void terminate() {
//		logger.debug("Terminating KickBallGoal");
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		ValueEntry turn = space.get(Variable.turnModifier);
		turn.setValue(1.0f);
			
		ValueEntry move = space.get(Variable.moveModifier);
		move.setValue(1.0f);

		_status = GoalEnum.inactive;
		_ball = null;
		_fsm.reset();
		space.get(Variable.target).setValue("");
	}

	@Override
	public float desireability() {
		// get the our scents and see if we can smell any food
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		Map<String,MemoryEntry> map = space.getVisualMemories().getFirst();
		if (map.isEmpty())
			return 0;
		
		// see if there is a ball within these memories
		boolean found = false;
		for (MemoryEntry entry : map.values()) { 
			if (entry.obj.getType() == ObjectType.dynamic || entry.obj.getType() == ObjectType.obstacle)
				found = true;
		}
		
		if (!found)
			return 0;
		
		// 1 - arousal.... 
		BoundedEntry energy = space.getBounded(Variable.energy);
		float pctPower = energy.getValue() / energy.getMax();
		if (pctPower < 0.1) { 
			// we have less than 10% power, we should eat and
			// stop goofing off.
			return 0;
		}
		
		// TODO - our desire is proportional to our arousal
		// the lower our arousal the more likely we are to kick the ball.
		float arousal = space.getBounded(Variable.arousal).getValue();
		return 1f - (arousal*arousal);
	}

	@Override
	public GoalEnum getStatus() {
		return _status;
	}

	@Override
	public boolean succeeding() {
		return true;
	}
	
	private FSM buildFSM() { 
		State start = new State("start") {
			@Override
			public void enterState(FSM fsm) {

			}

			@Override
			public void exitState(FSM fsm) {

			}

			@Override
			public void update(FSM fsm, long delta) {

			} 
		};
		
		State prepare = new State("prepare") {
			@Override
			public void enterState(FSM fsm) {
				// put some distance between us and the ball
				// so that we can get a running start.
				Vec2 direction = MathUtils.toVec2((float) Math.PI + _parent.getHeading());
				Vec2 bPos = _ball.getPPosition();
				float distance = bPos.sub(_parent.getPPosition()).length();
						
				// the desired distance is 10 so that we can start running
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Arrive.class);
				e.addParameter("status", true);
				e.addParameter("target", _parent.getPPosition().add(direction.mul(10-distance)));
				EventManager.inst().dispatch(e);
				
				e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Align.class);
				e.addParameter("status", true);
				e.addParameter("target", bPos);
				EventManager.inst().dispatch(e);

				fsm.setUserData("entered", System.currentTimeMillis());
			}

			@Override
			public void exitState(FSM fsm) {
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Arrive.class);
				e.addParameter("status", false);
				EventManager.inst().dispatch(e);

				e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Align.class);
				e.addParameter("status", false);
				EventManager.inst().dispatch(e);
			}

			@Override
			public void update(FSM fsm, long delta) {
			} 
		};
		
		State seek = new State("seek") {
			@Override
			public void enterState(FSM fsm) {
//				logger.debug(_parent.getName() + " [enter-KickBall] seek");
				AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());

				ValueEntry turn = space.get(Variable.turnModifier);
				turn.setValue(2.0f);
					
				ValueEntry move = space.get(Variable.moveModifier);
				move.setValue(2.0f);
			}

			@Override
			public void exitState(FSM fsm) {
//				logger.debug(_parent.getName() + " [exit-KickBall] seek");

				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Seek.class);
				e.addParameter("status", false);
				EventManager.inst().dispatch(e);
				
				e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Align.class);
				e.addParameter("status", false);
				EventManager.inst().dispatch(e);
				

				AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
				ValueEntry turn = space.get(Variable.turnModifier);
				turn.setValue(2.0f);
					
				ValueEntry move = space.get(Variable.moveModifier);
				move.setValue(2.0f);
			}

			@Override
			public void update(FSM fsm, long delta) { 
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Seek.class);
				e.addParameter("status", true);
				e.addParameter("target", _ball.getPPosition());
				EventManager.inst().dispatch(e);
				
				e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Align.class);
				e.addParameter("status", true);
				e.addParameter("target", _ball.getPPosition());
				EventManager.inst().dispatch(e);
			} 
		};
		
		State watch = new State("watch") {
			@Override
			public void enterState(FSM fsm) {
//				logger.debug(_parent.getName() + " [enter-KickBall] watch");
				fsm.setUserData("entered", System.currentTimeMillis());
			}

			@Override
			public void exitState(FSM fsm) { 
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Align.class);
				e.addParameter("status", false);
				EventManager.inst().dispatch(e);
			}

			@Override
			public void update(FSM fsm, long delta) {
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Align.class);
				e.addParameter("status", true);
				e.addParameter("target", _ball.getPPosition());
				EventManager.inst().dispatch(e);
			} 
		};
		
		State finished = new State("finished") {
			@Override
			public void enterState(FSM fsm) { 
				_status = GoalEnum.completed;
			}

			@Override
			public void exitState(FSM fsm) { }

			@Override
			public void update(FSM fsm, long delta) { } 
		};
		
		// --------------------------------------------------------------------
		// now that all of the states are constructed, we need to connect
		// them with the proper transitions.
		
		TransitionTest seekTest = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				Vec2 bPos = _ball.getPPosition();
				float distance = bPos.sub(_parent.getPPosition()).length();
				if (distance >= 10) 
					return true;
				return false;
			} 
		};

		TransitionTest prepareTest = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				Vec2 bPos = _ball.getPPosition();
				float distance = bPos.sub(_parent.getPPosition()).length();
				if (distance < 10) 
					return true;
				return false;
			} 
		};

		TransitionTest arrivedTest = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				Object userData = _parent.getUserData("arrive");
				if (userData == null)
					return false;
				
				return (Boolean) userData;
			}
		};

		TransitionTest watchTest = new TransitionTest() {
			private boolean _touched = false;
			
			@Override
			public boolean test(FSM fsm, State start) {
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
				if (agentSpace.isCollidingWith(_ball)) {
					// TODO: update our arousal....
					
					_touched = true;
				} else if (_touched) { 
					// we were in contact and now we are no longer in contact.
					_touched = false;
					return true;
				}
				return false;
			} 
		};		
		
		TransitionTest timing = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				long entered = (Long) fsm.getUserData("entered");
				long current = System.currentTimeMillis();
				if (current - 1000 > entered)
					return true;
				return false;
			}
		};

		TransitionTest timeout = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				long entered = (Long) fsm.getUserData("entered");
				long current = System.currentTimeMillis();
				if (current - 2000 > entered)
					return true;
				return false;
			}
		};
		
		// --------------------------------------------------------------------
		
		FSM fsm = new FSM(_parent.getName() + "-kickball");
		fsm.addState(start);
		fsm.addState(prepare);
		fsm.addState(seek);
		fsm.addState(watch);
		fsm.addState(finished);
		
		fsm.addTransition(start, prepare, prepareTest);
		fsm.addTransition(start, seek, seekTest);
		fsm.addTransition(prepare, seek, arrivedTest);
		fsm.addTransition(prepare, seek, timeout);
		fsm.addTransition(seek, watch, watchTest);
		fsm.addTransition(watch, finished, timing);
		
		fsm.setStartState(start);
		return fsm;
	}
	
}
