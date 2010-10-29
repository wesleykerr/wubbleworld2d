package edu.arizona.simulator.ww2d.object.component;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Constants;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
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
    
    private EventType[] _events;
    private Map<EventType,Boolean> _commands;
      
	public TopDownControl(GameObject obj) { 
		super(obj);
		
		_body = ((PhysicsObject) obj).getBody();

		_events = new EventType[] { 
				EventType.FORWARD_EVENT, EventType.BACKWARD_EVENT,
				EventType.LEFT_EVENT, EventType.RIGHT_EVENT,
				EventType.STRAFE_LEFT_EVENT, EventType.STRAFE_RIGHT_EVENT
		};
		_commands = new HashMap<EventType,Boolean>();
		for (EventType event : _events) 
			_commands.put(event, false);
	}
	
	private void init(boolean individual) { 
		EventListener listener = new EventListener() {
			@Override
			public void onEvent(Event e) {
				boolean state = (Boolean) e.getValue("state");
				_commands.remove(e.getId());
				_commands.put(e.getId(), state);
			} 
		};
		
		for (EventType event : _events) 
			EventManager.inst().register(event, _parent, listener);
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
		boolean forward = _commands.get(EventType.FORWARD_EVENT);
		boolean backward = _commands.get(EventType.BACKWARD_EVENT);
		
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
		boolean left = _commands.get(EventType.LEFT_EVENT);
		boolean right = _commands.get(EventType.RIGHT_EVENT);
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
		boolean sLeft = _commands.get(EventType.STRAFE_LEFT_EVENT);
		boolean sRight = _commands.get(EventType.STRAFE_RIGHT_EVENT);
		
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