package edu.arizona.simulator.ww2d.object.component.goals;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.CollisionEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.DistanceEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.FoodEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
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

public class DefendGoal implements Goal {
    private static Logger logger = Logger.getLogger( DefendGoal.class );

	private GoalEnum _status;
	
	private PhysicsObject _parent;
	private PhysicsObject _food;
	
	private FSM _fsm;
	
	public DefendGoal(PhysicsObject parent) {
		_parent = parent;
		
		_fsm = buildFSM();
	}
	
	private void findFood() { 
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		LinkedList<Map<String,FoodEntry>> scented = space.getScentMemories();
		Map<String,FoodEntry> map = scented.getFirst();
		
		float closestDistance = 0;
		_food = null;
		for (FoodEntry food : map.values()) { 
			DistanceEntry d = objectSpace.findOrAddDistance(_parent, food.obj);
			if (food.store > 0 && (_food == null || d.getDistance() < closestDistance)) {
				_food = food.obj;
				closestDistance = d.getDistance();
			}
		}
	}
	
	@Override
	public void activate() {
		// the assumption is that when we activate
		// this goal, we can perceive a food plot
		// and our first task is to arrive at it
		findFood();
		if (_food == null) {
			_status = GoalEnum.failed;
			return;
		}
		
		_status = GoalEnum.active;
		_fsm.reset();
		_fsm.activate();
	}

	@Override
	public GoalEnum process() {
		// make sure that there is still food around
		if (_food.getUserData("store", Float.class) <= 0) { 
			_status = GoalEnum.completed;
			return _status;
		}
		
		_fsm.update(0);

		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		space.get(Variable.state).setValue(_fsm.getActiveState().getName());
		
		return _status;
	}

	@Override
	public void terminate() {
		logger.debug("Terminating DefendGoal");
		_food = null;
		_fsm.reset();

		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		space.get(Variable.target).setValue("");
	}

	@Override
	public float desireability() {
		// get the our scents and see if we can smell any food
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		Map<String,FoodEntry> map = space.getScentMemories().getFirst();
		if (map.isEmpty())
			return 0;
		
		float desireability = 0.5f;
		
		// if we are very aggressive, we won't wait for someone to come to us
		desireability += 0.1f * (space.get(Variable.extroversion).get(Float.class) - 0.5f);
		desireability += 0.1f * (space.get(Variable.openness).get(Float.class) - 0.5f);
		desireability += 0.1f * (space.get(Variable.conscientiousness).get(Float.class) - 0.5f);

		// if we are very aroused, then this would be a nice break.
		desireability += 0.25f * space.getBounded(Variable.arousal).getValue();
		
		return desireability;
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
		State arrive = new State("arrive") {
			@Override
			public void enterState(FSM fsm) {
				if (_food == null)
					return;
				
//				logger.debug(_parent.getName() + " [enter] Arrive state within DefendGoal");
				fsm.setUserData("enteredArrive", System.currentTimeMillis());
				
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Arrive.class);
				e.addParameter("status", true);
				e.addParameter("target", _food.getPPosition());
				EventManager.inst().dispatch(e);
				
				e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Align.class);
				e.addParameter("status", true);
				e.addParameter("target", _food.getPPosition());
				EventManager.inst().dispatch(e);
			}

			@Override
			public void exitState(FSM fsm) {
//				logger.debug(_parent.getName() + " [exit] Arrive state within DefendGoal");

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
			public void update(FSM fsm, long delta) { } 
		};
		
		State turn = new State("turn") {
			@Override
			public void enterState(FSM fsm) {
//				logger.debug(_parent.getName() + " [enter] turn state within DefendGoal");
				fsm.setUserData("entered", System.currentTimeMillis());
			}

			@Override
			public void exitState(FSM fsm) {
//				logger.debug(_parent.getName() + " [exit] Turn state within DefendGoal");
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Align.class);
				e.addParameter("status", false);
				EventManager.inst().dispatch(e);
			}

			@Override
			public void update(FSM fsm, long delta) {
				// every second we need to turn some random amount.
				long entered = (Long) fsm.getUserData("entered");
				long current = System.currentTimeMillis();
				if (current - 1000 > entered) { 
					float radians = (float) Math.toRadians(MathUtils.random.nextInt(180));
					float desired = _parent.getHeading() + radians;
					
					Event e = new Event(EventType.BEHAVIOR_EVENT);
					e.addRecipient(_parent);
					e.addParameter("name", Align.class);
					e.addParameter("status", true);
					e.addParameter("target", desired);
					EventManager.inst().dispatch(e);
					
					fsm.setUserData("entered", current+1000L);
				}
			} 
		};
		
		State chaseAway = new State("chase-away") { 
			private PhysicsObject _target;

			@Override
			public void enterState(FSM fsm) {
//				logger.debug(_parent.getName() + " [enter] chaseAway state within DefendGoal");
				_target = (PhysicsObject) fsm.getUserData("target");
			}

			@Override
			public void exitState(FSM fsm) {
//				logger.debug(_parent.getName() + " [exit] chaseAway state within DefendGoal");
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Seek.class);
				e.addParameter("status", false);
				EventManager.inst().dispatch(e);
			}

			@Override
			public void update(FSM fsm, long delta) {
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Seek.class);
				e.addParameter("status", true);
				e.addParameter("target", _target.getPPosition());
				EventManager.inst().dispatch(e);
			} 
		};
		
		State backAway = new State("back-away") {
			@Override
			public void enterState(FSM fsm) {
//				logger.debug(_parent.getName() + " [enter] backAway state within DefendGoal");
				Vec2 direction = MathUtils.toVec2((float) Math.PI + _parent.getHeading());
				
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Seek.class);
				e.addParameter("status", true);
				e.addParameter("target", _parent.getPPosition().add(direction.mul(100)));
				EventManager.inst().dispatch(e);
				
				fsm.setUserData("entered", System.currentTimeMillis());
			}

			@Override
			public void exitState(FSM fsm) {
				logger.debug(_parent.getName() + " [exit] backAway state within DefendGoal");
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Seek.class);
				e.addParameter("status", false);
				EventManager.inst().dispatch(e);
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
//					logger.debug(_parent.getName() + " Eating nom nom nom");
					Event e = new Event(EventType.REQUEST_EAT);
					e.addRecipient(_food);
					e.addParameter("requestor", _parent);
					EventManager.inst().dispatch(e);
					
					_lastEaten = time;
				}
			} 
		};
		
		// --------------------------------------------------------------------
		// now that all of the states are constructed, we need to connect
		// them with the proper transitions.
		
		TransitionTest arrivedTest = new TransitionTest() {
						
			@Override
			public boolean test(FSM fsm, State start) {
				float distance = _parent.getPPosition().sub(_food.getPPosition()).length();
				if (distance <= 0.001f)
					return true;
				return false;
			} 
		};		
		
		TransitionTest someoneApproaches = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
				ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
				Set<String> approaching = agentSpace.getApproaching();
				
				if (approaching.isEmpty())
					return false;

				PhysicsObject target = null;
				float distance = 0;
				for (String s : approaching) {
					PhysicsObject obj = objectSpace.getPhysicsObject(s);
					
					// make sure that the thing that is approaching is actually
					// an agent and not a static object we are approaching.
					if (obj.getType() != ObjectType.cognitiveAgent &&
						obj.getType() != ObjectType.reactiveAgent) 
						continue;

					DistanceEntry entry = objectSpace.findOrAddDistance(_parent, obj);
					if (target == null || entry.getDistance() < distance) { 
						target = obj;
						distance = entry.getDistance();
					}
				}
				
				if (target != null) {
					fsm.setUserData("target", target);
					agentSpace.get(Variable.target).setValue(target.getName());
					return true;
				}
				return false;
			}
		};
		
		
		TransitionTest collision = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
				PhysicsObject target = (PhysicsObject) fsm.getUserData("target");
				for (CollisionEntry entry : agentSpace.getCollisions()) { 
					if (entry.getOther(_parent) == target) { 
						return true;
					}
				}
				return false;
			}
		};

		
		TransitionTest tooFar = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
				DistanceEntry d = objectSpace.findOrAddDistance(_parent, _food);

				if (d.getDistance() > 5) { 
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
				if (current - 400 > entered)
					return true;
				return false;
			}
		};

		TransitionTest timeOut = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				long entered = (Long) fsm.getUserData("enteredArrive");
				long current = System.currentTimeMillis();
				if (current - 5000 > entered)
					return true;
				return false;
			}
		};
		
		TransitionTest health = new TransitionTest() { 
			@Override
			public boolean test(FSM fsm, State start) { 
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
				BoundedEntry energy = agentSpace.getBounded(Variable.energy);
				if (energy.getValue() < energy.getMax())
					return true;
				return false;
			}
		};
		
		TransitionTest leaveHealth = new TransitionTest() { 
			@Override
			public boolean test(FSM fsm, State start) { 
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
				BoundedEntry energy = agentSpace.getBounded(Variable.energy);
				logger.debug("Energy: " + energy.getValue() + " " + energy.getMax());
				if (Float.compare(energy.getValue(),energy.getMax()) == 0)
					return true;
				logger.debug("\tReturning false");
				return false;
			}
		};
		
		
		// --------------------------------------------------------------------
		
		FSM fsm = new FSM(_parent.getName() + "-defendfood");
		fsm.addState(arrive);
		fsm.addState(turn);
		fsm.addState(chaseAway);
		fsm.addState(backAway);
		fsm.addState(eat);
		
		fsm.addTransition(arrive, turn, arrivedTest);
		fsm.addTransition(arrive, turn, timeOut);
		fsm.addTransition(arrive, chaseAway, someoneApproaches);
		fsm.addTransition(turn, chaseAway, someoneApproaches);
		fsm.addTransition(turn, eat, health);
		fsm.addTransition(eat, turn, leaveHealth);
		fsm.addTransition(eat, chaseAway, someoneApproaches);
		fsm.addTransition(chaseAway, arrive, tooFar);
		fsm.addTransition(chaseAway, backAway, collision);
		fsm.addTransition(backAway, chaseAway, timing);
		
		fsm.setStartState(arrive);
		return fsm;
	}
	
}
