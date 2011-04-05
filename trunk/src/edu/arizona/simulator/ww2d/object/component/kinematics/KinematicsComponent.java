package edu.arizona.simulator.ww2d.object.component.kinematics;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.player.KinematicEvent;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.component.Component;
import edu.arizona.simulator.ww2d.system.EventManager;

public class KinematicsComponent extends Component {
    private static Logger logger = Logger.getLogger( KinematicsComponent.class );

	public enum Behavior { 
		seek, flee, wander, arrive
	}
	
	private Vec2 _velocity;
	private float _rotation;
	
	private float _maxSpeed = 20f;
	
	private Behavior _behavior = Behavior.seek;
	private Vec2 _target = new Vec2(0,0);
	
	public KinematicsComponent(GameObject owner) { 
		super(owner);
		
		EventManager.inst().register(KinematicEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				KinematicEvent event = (KinematicEvent) e;
				_target = event.getTarget();
				_behavior = event.getMovement();
				
				logger.debug("New target: " + _target);
				logger.debug("New pattern: " + _behavior);
			} 
		});
	}	
	
	public Vec2 getVelocity() { 
		return _velocity;
	}
	
	public float getRotation() { 
		return _rotation;
	}	
	
	@Override
	public void fromXML(Element e) {
		// I don't plan on loading this from XML.
	}

	@Override
	public void update(int elapsed) {
		switch (_behavior) { 
		case seek:
			doSeek();
			break;
		case flee:
			doFlee();
			break;
		case arrive:
			doArrive();
			break;
		case wander:
			doWander();
			break;
		}
		
		float deltaT = (float) elapsed / 1000.0f;
		_parent.getPosition().addLocal(_velocity.mul(deltaT));
		_parent.setHeading(_parent.getHeading() + (_rotation * deltaT));
	}

	/**
	 * Determine the new orientation based on the new velocity
	 * ... if it is greater than 0.
	 * @param current
	 * @param velocity
	 * @return
	 */
	private float newOrientation(float current, Vec2 velocity) { 
		if (velocity.lengthSquared() > 0) { 
			return (float) Math.atan2(velocity.y, velocity.x);
		} else { 
			return current;
		}
	}
	
	
	private void doSeek() { 
		_velocity = _target.sub(_parent.getPosition());
		_velocity.normalize();
		_velocity.mulLocal(_maxSpeed);
		
		_parent.setHeading(newOrientation(_parent.getHeading(), _velocity));
	}
	
	private void doFlee() { 
		_velocity = _parent.getPosition().sub(_target);
		_velocity.normalize();
		_velocity.mulLocal(_maxSpeed);
		
		_parent.setHeading(newOrientation(_parent.getHeading(), _velocity));
	}
	
	private void doArrive() { 
		float radius = 0.5f;
		float radiusSquared = radius*radius;
		float timeToTarget = 0.25f;
		
		_velocity = _target.sub(_parent.getPosition());
		if (_velocity.lengthSquared() < radiusSquared) { 
			_velocity.set(0,0);
			_rotation = 0f;
			return;
		}
		
		_velocity.mulLocal(1/timeToTarget);
		if (_velocity.lengthSquared() > _maxSpeed*_maxSpeed) {
			_velocity.normalize();
			_velocity.mulLocal(_maxSpeed);
		}
		
		_parent.setHeading(newOrientation(_parent.getHeading(), _velocity));
	}
	
	private void doWander() { 
		float maxRotation = 1f;
		
		float heading = _parent.getHeading();		
		
		_velocity = new Vec2((float) Math.cos(heading), (float) Math.sin(heading));
		_velocity.mulLocal(_maxSpeed);
		
		_rotation = (float) (Math.random() - Math.random()) * maxRotation;		
	}
}