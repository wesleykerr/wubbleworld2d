package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.apache.log4j.Logger;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;

/**
 * Steering behaviors work in the Physics world so remember
 * to use .getPPosition() instead of getPosition() since 
 * getPosition returns the position in Screen coordinates.
 * @author wkerr
 *
 */
public abstract class Behavior {
    private static Logger logger = Logger.getLogger( Behavior.class );

	protected PhysicsObject _entity;
	protected boolean _isOn;
	
	public Behavior(PhysicsObject entity) { 
		_entity = entity;
		_isOn = false;
	}

	public boolean isOn() { 
		return _isOn;
	}

	public abstract void onEvent(BehaviorEvent e);
	public abstract SteeringOutput getSteering(float moveModifier, float turnModifier);
	
	/**
	 * Stub code so that each Behavior has a render method.
	 * If we really would like to see what's happening then
	 * override this method.
	 * @param g
	 */
	public void render(Graphics g) {
		
	}
}
