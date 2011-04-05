package edu.arizona.simulator.ww2d.object.component;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.movement.BackwardEvent;
import edu.arizona.simulator.ww2d.events.movement.ForwardEvent;
import edu.arizona.simulator.ww2d.events.movement.LeftEvent;
import edu.arizona.simulator.ww2d.events.movement.RightEvent;
import edu.arizona.simulator.ww2d.events.movement.StrafeLeftEvent;
import edu.arizona.simulator.ww2d.events.movement.StrafeRightEvent;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Constants;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

/**
 * The MovementModifier will attach itself to an Entity
 * and provide the necessary hooks in order to move using
 * key inputs.
 * @author wkerr
 *
 */
public class TopDownControl extends Component {
    private static Logger logger = Logger.getLogger( TopDownControl.class );

    private Body          _body;
    private boolean[]     _commands;
      
	public TopDownControl(GameObject obj) { 
		super(obj);
		
		_body = ((PhysicsObject) obj).getBody();
		_commands = new boolean[6];
	}
	
	private void init(boolean individual) { 
		EventManager.inst().register(ForwardEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				_commands[0] = ((ForwardEvent) e).getState();
			} 
		});
		
		EventManager.inst().register(BackwardEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				_commands[1] = ((BackwardEvent) e).getState();
			} 
		});
		
		EventManager.inst().register(LeftEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				_commands[2] = ((LeftEvent) e).getState();
			} 
		});

		EventManager.inst().register(RightEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				_commands[3] = ((RightEvent) e).getState();
			} 
		});

		EventManager.inst().register(StrafeLeftEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				_commands[4] = ((StrafeLeftEvent) e).getState();
			} 
		});
		EventManager.inst().register(StrafeRightEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				_commands[5] = ((StrafeRightEvent) e).getState();
			} 
		});

	}
	
	/**
	 * Update this modifier for the elapsed seconds
	 * @param elapsed - the milliseconds that have
	 * 	elapsed.
	 */
	@Override
	public void update(int elapsed) {
		updateFB(elapsed);
		updateLR(elapsed);
		updateStrafe(elapsed);
		
//		logger.debug("Position: " + _body.getPosition());
		_body.wakeUp();
	}
	
	/**
	 * Update the entity in case it is moving forward or backward.
	 * @param elapsed
	 */
	private void updateFB(float elapsed) { 
		boolean forward = _commands[0];
		boolean backward = _commands[1];
		
		if (forward && backward)
			return;

		//TODO: may want to actually handle this specially.  We could
		// slow the agent down.
		if (!forward && !backward) {
			return;
		}
		
		float x = 0;	
		if (forward) {
			// TODO: handle powerups 
			x = Constants.MAX_ACCELERATION;  //*ae.getMoveModifier();
		}
		
		if (backward) {
			// TODO: handle powerups
			x = -Constants.MAX_ACCELERATION; //*ae.getMoveModifier();
		} 		
		float mass = _body.getMass();
		float angle = _body.getAngle();
		
		Vec2 direction = new Vec2((float) Math.cos(angle), (float) Math.sin(angle));
		direction.normalize();
		direction.mulLocal(x*mass);
		_body.applyForce(direction, _body.getPosition());
	}
	
	/**
	 * Update the entity in case it is turning left or right.
	 * @param elapsed
	 */
	private void updateLR(float elapsed) { 
		boolean left = _commands[2];
		boolean right = _commands[3];
		if (left && right)
			return;
		
		if (!left && !right) {
			// Slow down the angular velocity of the PhysicsObject
			float old = _body.getAngularVelocity();
			_body.setAngularVelocity(0.5f * old);
			return;
		}
		
		// TODO: be able to handle power ups
		Space systemSpace = Blackboard.inst().getSpace("system");
		float x = systemSpace.get(Variable.maxAngularAcceleration).get(Float.class);
		if (left) 
			x *= -1;  //Constants.MAX_ANGULAR_ACCELERATION; //*ae.getTurnModifier();

		_body.applyTorque(_body.getMass()*x);
		
		float omega = _body.getAngularVelocity();
//		if (omega < -Constants.MAX_TURN) {
//			_body.setAngularVelocity(-Constants.MAX_TURN);
//		}
//		
//		if (omega > Constants.MAX_TURN) {
//			_body.setAngularVelocity(Constants.MAX_TURN);
//		}
	}
	
	/**
	 *  Update the entity when it is strafing left or
	 *  right.
	 * @param elapsed
	 */
	private void updateStrafe(float elapsed) { 
		boolean sLeft = _commands[4];
		boolean sRight = _commands[5];
		
		// If we are trying strafe both left and right
		// then the net force is the big goose egg.
		if (sLeft && sRight)
			return;

		// If neither strafing command is active then
		// we turn off strafing and continue about our business.
		if (!sLeft && !sRight) {
			return;
		}
		
		float x = 0;	
		if (sLeft) {
			// TODO: handle powerups
			x = -Constants.MAX_ACCELERATION; //*_entity.getMoveModifier();
		}
		
		if (sRight) {
			x = Constants.MAX_ACCELERATION; //*_entity.getMoveModifier();
		} 

		float angle = _body.getAngle() + (float) (Math.PI * 0.5f);
		Vec2 direction = new Vec2((float) Math.cos(angle), (float) Math.sin(angle));
		direction.normalize();
		direction.mulLocal(x*_body.getMass());
		_body.applyForce(direction, _body.getPosition());		
	}

	@Override
	public void fromXML(Element e) {
		init(Boolean.parseBoolean(e.attributeValue("individual")));
	}
}