package edu.arizona.simulator.ww2d.object;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class PhysicsObject extends GameObject {

	protected Body _body;
	
	public PhysicsObject(String name, ObjectType type, int renderPriority) { 
		super(name, type, renderPriority);
	}

	/**
	 * Sets the physical body of this GameObject
	 * @param b
	 */
	public void setBody(Body b) { 
		_body = b;
	}
	
	/**
	 * Returns the physical body of this GameObject
	 * @return
	 */
	public Body getBody() { 
		return _body;
	}
	
	/**
	 * Return the position in physics coordinates
	 * useful for things.
	 * @return
	 */
	public Vec2 getPPosition() { 
		return _body.getPosition();
	}

	/**
	 * Return the position of the Object in Screen Coordinates
	 */
	@Override
	public Vec2 getPosition() {
		Space systemSpace = Blackboard.inst().getSpace("system");
		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);
		
		return _body.getPosition().mul(scale);
	}

	@Override
	public void setPosition(Vec2 v) {
		_body.setXForm(v, getHeading());
	}

	@Override
	public float getHeading() {
		return _body.getAngle();
	}

	@Override
	public void setHeading(float heading) {
		_body.setXForm(getPosition(), heading);
	}
	
	/**
	 * Called when this PhysicsObject is being
	 * removed from the world.
	 */
	public void finish() { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		World world = systemSpace.get(Variable.physicsWorld).get(World.class);
		
		world.destroyBody(_body);
	}
}
