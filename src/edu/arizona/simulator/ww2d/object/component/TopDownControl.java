package edu.arizona.simulator.ww2d.object.component;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.newdawn.slick.Input;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
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
    private boolean[]     _commands;
      
	public TopDownControl(GameObject obj) { 
		super(obj);
		
		_body = ((PhysicsObject) obj).getBody();
		_commands = new boolean[6];
	}
	
	private void init(boolean individual) { 
		EventListener keyPressed = new EventListener() { 
			public void onEvent(Event e) { 
				int key = (Integer) e.getValue("key");
				switch (key) { 
				case Input.KEY_W: _commands[0] = true; break;
				case Input.KEY_S: _commands[1] = true; break;
				case Input.KEY_A: _commands[2] = true; break;
				case Input.KEY_D: _commands[3] = true; break;
				case Input.KEY_Q: _commands[4] = true; break;
				case Input.KEY_E: _commands[5] = true; break;
				}
			}
		};
		
		EventListener keyReleased = new EventListener() { 
			public void onEvent(Event e) { 
				int key = (Integer) e.getValue("key");
				switch (key) { 
				case Input.KEY_W: _commands[0] = false; break;
				case Input.KEY_S: _commands[1] = false; break;
				case Input.KEY_A: _commands[2] = false; break;
				case Input.KEY_D: _commands[3] = false; break;
				case Input.KEY_Q: _commands[4] = false; break;
				case Input.KEY_E: _commands[5] = false; break;
				}
			}
		};
		
		if (individual) {
			EventManager.inst().register(EventType.KEY_PRESSED_EVENT, _parent, keyPressed);
			EventManager.inst().register(EventType.KEY_RELEASED_EVENT, _parent, keyReleased);
		} else { 
			EventManager.inst().registerForAll(EventType.KEY_PRESSED_EVENT, keyPressed);
			EventManager.inst().registerForAll(EventType.KEY_RELEASED_EVENT, keyReleased);
		}
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
		if (_commands[0] && _commands[1])
			return;

		//TODO: may want to actually handle this specially.  We could
		// slow the agent down.
		if (!_commands[0] && !_commands[1]) {
			return;
		}
		
		float x = 0;	
		if (_commands[0]) {
			// TODO: handle powerups 
			x = Constants.MAX_ACCELERATION;  //*ae.getMoveModifier();
		}
		
		if (_commands[1]) {
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
		if (_commands[2] && _commands[3])
			return;
		
		if (!_commands[2] && !_commands[3]) {
			// Slow down the angular velocity of the PhysicsObject
			float old = _body.getAngularVelocity();
			_body.setAngularVelocity(0.5f * old);
			return;
		}
		
		// TODO: be able to handle power ups
		Space systemSpace = Blackboard.inst().getSpace("system");
		float x = systemSpace.get(Variable.maxAngularAcceleration).get(Float.class);
		if (_commands[2]) 
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
		// If we are trying strafe both left and right
		// then the net force is the big goose egg.
		if (_commands[4] && _commands[5])
			return;

		// If neither strafing command is active then
		// we turn off strafing and continue about our business.
		if (!_commands[4] && !_commands[5]) {
			return;
		}
		
		float x = 0;	
		if (_commands[4]) {
			// TODO: handle powerups
			x = -Constants.MAX_ACCELERATION; //*_entity.getMoveModifier();
		}
		
		if (_commands[5]) {
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