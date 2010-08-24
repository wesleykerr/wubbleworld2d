package edu.arizona.simulator.ww2d.object.component.goals;

import java.util.Set;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.CollisionEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.fsm.FSM;
import edu.arizona.simulator.ww2d.fsm.State;
import edu.arizona.simulator.ww2d.fsm.TransitionTest;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Pursue;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Seek;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Accumulator;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class PursueGoal implements Goal {
    private static Logger logger = Logger.getLogger( PursueGoal.class );

	private GoalEnum _status;
	
	private PhysicsObject _parent;
	private PhysicsObject _target;
	
	private FSM _fsm;
	
	private Accumulator _accumulator;
	
	public PursueGoal(PhysicsObject obj) { 
		_parent = obj;
		_target = null;
		
		_status = GoalEnum.inactive;
		_fsm = buildFSM();
		
		_accumulator = new Accumulator(1000);
	}

	/**
	 * Is there a target in my line of sights?
	 * @return
	 */
	private boolean targetAvailable() { 
		if (_target != null)
			return true;
		
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		ValueEntry attackableEntry = space.get(Variable.attackable);
		if (attackableEntry == null)
			return false;
		
		Set attackable = attackableEntry.get(Set.class);
		if (attackable.isEmpty())
			return false;
		return true;
	}
	
	@Override
	public void activate() {
		logger.debug(_parent.getName() + " activating pursue...");
		
		// select a target
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		ValueEntry attackableEntry = space.get(Variable.attackable);
		if (attackableEntry == null)
			return;
		
		Set attackable = attackableEntry.get(Set.class);
		if (attackable.isEmpty())
			return;

		_target = (PhysicsObject) attackable.iterator().next();
		_status = GoalEnum.active;

		space.get(Variable.target).setValue(_target.getName());
		logger.debug("Target selected: " + _target.getName());

//		Event e = new Event(EventType.BEHAVIOR_EVENT);
//		e.addRecipient(_parent);
//		e.addParameter("name", Pursue.class);
//		e.addParameter("status", true);
//		e.addParameter("target", _target.getName());
//		
//		EventManager.inst().dispatch(e);
		
		_fsm.reset();
		_fsm.activate();
	}

	@Override
	public GoalEnum getStatus() {
		return _status;
	}

	@Override
	public GoalEnum process() {
		if (_target == null) {
			activate();
			_status = GoalEnum.failed;
			return _status;
		}		
		
		// update the accumulator with our success or failure.
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		Set<String> approaching = space.getApproaching();
		if (approaching == null || approaching.size() == 0) 
			_accumulator.record(0);
		else if (approaching.contains(_target.getName())) 
			_accumulator.record(1);
		else
			_accumulator.record(0);
		
		// if we have recorded at least half as many samples as we need then
		// we can see if we need to speed up.
		if (_accumulator.getSize() > 0.95f && _accumulator.getAverage() < 0.5f) { 
			logger.debug("Accumulator size: " + _accumulator.getSize() + " " + _accumulator.getAverage());
			space.increaseSpeed();
			_accumulator.reset();
		}
		
		_fsm.update(0);
		space.get(Variable.state).setValue(_fsm.getActiveState().getName());
		
		// if the health of the agent we are pursuing drops to below 0 then
		// we this goal has succeeded
		float energy = space.getBounded(Variable.energy).getValue();
		if (energy <= 0) {
			logger.debug(_parent.getName() + " target " + _target.getName() + " is dead");
			_target = null;
			_status = GoalEnum.completed;
		}
		
		
		return _status;
	}

	@Override
	public boolean succeeding() {
		// test to see if we are getting closer to the target
		
		return false;
	}

	@Override
	public void terminate() {
		logger.debug(_parent.getName() + " deactivating pursue...");
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		ValueEntry turn = space.get(Variable.turnModifier);
		turn.setValue(1.0f);
			
		ValueEntry move = space.get(Variable.moveModifier);
		move.setValue(1.0f);
		_target = null;
		space.get(Variable.target).setValue("");
		_fsm.getActiveState().exitState(_fsm);
	}

	public float desireability() { 
		if (!targetAvailable())
			return 0.0f;

		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		BoundedEntry energy = space.getBounded(Variable.energy);
		float ourEnergy = space.getBounded(Variable.energy).getValue();
		if (ourEnergy == 0)
			return 0.0f;
		
		// find the target with the least amount of health.
		ValueEntry attackableEntry = space.get(Variable.attackable);
		Set attackable = attackableEntry.get(Set.class);
		PhysicsObject target = null;
		float leastHealth = 0;
		for (Object o : attackable) { 
			PhysicsObject obj = (PhysicsObject) o;
			AgentSpace tmpSpace = Blackboard.inst().getSpace(AgentSpace.class, obj.getName());
			float otherEnergy = space.getBounded(Variable.energy).getValue();
			if (target == null || otherEnergy < leastHealth) { 
				target = obj;
				leastHealth = otherEnergy;
			}
		}

		// start us at a reasonable starting point.
		float desireability = 0.25f;
		
		desireability += 0.1f * (0.5f - space.get(Variable.openness).get(Float.class));
		desireability += 0.1f * (0.5f - space.get(Variable.extroversion).get(Float.class));
		desireability += 0.30f * (space.get(Variable.agreeableness).get(Float.class) - 0.5f);

		desireability += 0.1f * (0.5f - space.getBounded(Variable.valence).getValue());
		desireability += 0.1f * (0.5f - space.getBounded(Variable.valence).getValue());
		
		desireability += 0.15f * ((ourEnergy - leastHealth) / 100);
		desireability -= 0.15f * (1 - energy.getValue() / energy.getMax());

		// TODO: emotional state...
		// if we have a low valence, then we are less likely to attack
		// if we have a hight arousal then we are less likely to attack
		
//		BoundedEntry power = space.getBounded(Variable.power);
//		desireability -= 0.25f * (1 - power.getValue() / power.getMax());
//		desireability = (desireability + 1) / 2;
		
		return desireability;
	}
	
	private FSM buildFSM() { 
		State charge = new State("charge") {
			@Override
			public void enterState(FSM fsm) {
				if (_target == null)
					return;
				
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Pursue.class);
				e.addParameter("status", true);
				e.addParameter("target", _target.getName());
				
				EventManager.inst().dispatch(e);
			}

			@Override
			public void exitState(FSM fsm) {
				Event e = new Event(EventType.BEHAVIOR_EVENT);
				e.addRecipient(_parent);
				e.addParameter("name", Pursue.class);
				e.addParameter("status", false);
				
				EventManager.inst().dispatch(e);
			}

			@Override
			public void update(FSM fsm, long delta) {
				// TODO if we are not gaining on him then we should
				// run faster.
			} 
		};
		
		State backAway = new State("back-away") {
			@Override
			public void enterState(FSM fsm) {
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
		
		// --------------------------------------------------------------------
		// now that all of the states are constructed, we need to connect
		// them with the proper transitions.
		
		TransitionTest collision = new TransitionTest() {
			@Override
			public boolean test(FSM fsm, State start) {
				AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
				for (CollisionEntry entry : agentSpace.getCollisions()) { 
					if (entry.getOther(_parent) == _target) { 
						return true;
					}
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
		
		// --------------------------------------------------------------------
		
		FSM fsm = new FSM(_parent.getName() + "-attack");
		fsm.addState(charge);
		fsm.addState(backAway);
		
		fsm.addTransition(charge, backAway, collision);
		fsm.addTransition(backAway, charge, timing);
		
		fsm.setStartState(charge);
		return fsm;
	}
}
