package edu.arizona.simulator.ww2d.system;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.DefaultContactFilter;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.system.CollisionEvent;
import edu.arizona.simulator.ww2d.events.system.ContactEvent;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.DistanceResults;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class PhysicsSubsystem implements Subsystem, ContactListener {
    private static Logger logger = Logger.getLogger( PhysicsSubsystem.class );

    private World         _world;
    
    private Map<String,Joint>         _joints;
    private Map<String,ContactPoint>  _contactMap;
    
    public PhysicsSubsystem() { 
    	addListeners();
    }
    
	@Override
	public SubsystemType getId() {
		return SubsystemType.PhysicsSubsystem;
	}
	
	private void addListeners() { 
	}
	
	/**
	 * Initialize the world based on the parameters
	 * given here.
	 * 	<physics>
	 *		<aabb>
	 *			<min x="0" y="0" />
	 * 			<max x="300" y="300" />
	 *		</aabb>
	 *		<gravity x="0" y="0" />
	 *		<doSleep value="true" />		
	 *  </physics>
	 * @param e
	 */
	public void fromXML(Element e) { 
    	_contactMap = new HashMap<String,ContactPoint>();
    	_joints = new HashMap<String,Joint>();
    	
		Element tmp = e.element("aabb");
		float x1 = Float.parseFloat(tmp.element("min").attributeValue("x"));
		float y1 = Float.parseFloat(tmp.element("min").attributeValue("y"));
		
		float x2 = Float.parseFloat(tmp.element("max").attributeValue("x"));
		float y2 = Float.parseFloat(tmp.element("max").attributeValue("y"));
		
		AABB aabb = new AABB(new Vec2(x1,y1), new Vec2(x2,y2));
		
		float x = Float.parseFloat(e.element("gravity").attributeValue("x"));
		float y = Float.parseFloat(e.element("gravity").attributeValue("y"));
		
		boolean sleep = Boolean.parseBoolean(e.element("doSleep").attributeValue("value"));
		
		_world = new World(aabb, new Vec2(x,y), sleep);
		_world.setContactListener(this);
		_world.setContactFilter(new GameContactFilter());
		
		// Save off the world to the blackboard so that others can join
		// in the fun of accessing it.
		Space systemSpace = Blackboard.inst().getSpace("system");
		systemSpace.put(Variable.physicsWorld, new ValueEntry(_world));
		systemSpace.put(Variable.physicsMin, new ValueEntry(new Vec2(x1,y1)));
		systemSpace.put(Variable.physicsMax, new ValueEntry(new Vec2(x2,y2)));
		systemSpace.put(Variable.physicsGravity, new ValueEntry(new Vec2(x,y)));
	}
    
    /**
     * Now that the world has been initialized it
     * record the value here.
     * @param world
     */
    public void setPhysics(World world) { 
    	_world = world;
    	_world.setContactListener(this);
    }
    
    /**
     * Returns the physics world.  Be careful with this
     * because you can screw up the physics.
     * @return
     */
    public World getPhysics() { 
    	return _world;
    }
    
    /**
     * Update the physics.
     * @param elapsed
     */
    public void update(int elapsed) { 
		_world.step(elapsed / 1000.0f, 10);
    }
    
    /**
     * Return the Axis-Aligned Bounding Box for the
     * physics world.
     * @return
     */
    public AABB getWorldAABB() { 
    	return _world.getWorldAABB();
    }
    
	/**
	 * Add should be called when the contact begins and 
	 * we should make a copy of the contact point based on
	 * the specs discussed in the Box2D manual.  They
	 * may be reusing objects.
	 * @param cp - the contact point
	 */
	public void add(ContactPoint cp) {
		_contactMap.put(cp.id.features.toString(), cp);
		
		// Assumption is that in each Shape's user data is
		// the reference to the PhysicsObject.
		PhysicsObject obj1 = (PhysicsObject) cp.shape1.getUserData();
		PhysicsObject obj2 = (PhysicsObject) cp.shape2.getUserData();
		
		EventManager.inst().dispatch(new CollisionEvent(copy(cp), "add", obj1, obj2));
//		logger.debug("Collision [add]: " + obj1.getName() + " " + obj2.getName() + " " + cp.id.features.toString());
	}

	/**
	 * Called after an add has already been called, so we shouldn't have
	 * to do anything with this method.  
	 */
	public void persist(ContactPoint cp) {
//		logger.debug("ContactPersist: " + cp.shape1.getUserData() + " " + cp.shape2.getUserData());
		PhysicsObject obj1 = (PhysicsObject) cp.shape1.getUserData();
		PhysicsObject obj2 = (PhysicsObject) cp.shape2.getUserData();

		EventManager.inst().dispatch(new CollisionEvent(copy(cp), "persist", obj1, obj2));
//		logger.debug("Collision [persist]: " + obj1.getName() + " " + obj2.getName() + " " + cp.id.features.toString());
	}

	/**
	 * Called when a contact point is being removed from the world.
	 * To test this, make sure that everything that is added is also removed.
	 * @param cp - the contact point to be removed.
	 */
	public void remove(ContactPoint cp) {
		PhysicsObject obj1 = (PhysicsObject) cp.shape1.getUserData();
		PhysicsObject obj2 = (PhysicsObject) cp.shape2.getUserData();

		// TODO: Record the removal of the contact either to the blackboard
		// or notify the physics objects that they are no longer colliding.
		
//		logger.debug(obj1.getName() + " done colliding with " + obj2.getName());
		_contactMap.remove(cp.id.features.toString());
		EventManager.inst().dispatch(new CollisionEvent(copy(cp), "remove", obj1, obj2));
//		logger.debug("Collision [remove]: " + obj1.getName() + " " + obj2.getName() + " " + cp.id.features.toString());
	}

	public void result(ContactResult cp) {
		// hopefully this contains the amount of force transferred between
		// the bastards.
		
		PhysicsObject obj1 = (PhysicsObject) cp.shape1.getUserData();
		PhysicsObject obj2 = (PhysicsObject) cp.shape2.getUserData();
		
		EventManager.inst().dispatch(new ContactEvent(copy(cp), obj1, obj2));
//		logger.debug("Collision [result]: " + obj1.getName() + " " + obj2.getName() + " " + cp.id.features.toString());
	}

	/**
	 * Render the physics information if we deem that to be necessary.
	 * Things like the world bounding box.
	 * @param g
	 */
	public void render(Graphics g) { 

	}
	
	/**
	 * Copy the first contact point into the second.
	 * @param cp1
	 * @param cp2
	 */
	private ContactPoint copy(ContactPoint cp1) { 
		ContactPoint cp2 = new ContactPoint();
		cp2.friction = cp1.friction;
		cp2.id.set(cp1.id);
		cp2.normal.set(cp1.normal);
		cp2.position.set(cp1.position);
		cp2.velocity.set(cp1.velocity);

		cp2.restitution = cp1.restitution;
		cp2.separation = cp1.separation;
		cp2.shape1 = cp1.shape1;
		cp2.shape2 = cp1.shape2;
		
		return cp2;
	}

	/**
	 * Copy the first contact point into the second.
	 * @param cp1
	 * @param cp2
	 */
	private ContactResult copy(ContactResult cp1) { 
		ContactResult cp2 = new ContactResult();
		cp2.id.set(cp1.id);
		cp2.normal.set(cp1.normal);
		cp2.position.set(cp1.position);

		cp2.normalImpulse = cp1.normalImpulse;
		cp2.tangentImpulse = cp1.tangentImpulse;

		cp2.shape1 = cp1.shape1;
		cp2.shape2 = cp1.shape2;
		
		return cp2;
	}	
	public void createDistanceJoint(PhysicsObject obj1, PhysicsObject obj2, DistanceResults results) { 
		Body b1 = obj1.getBody();
		Body b2 = obj2.getBody();
		DistanceJointDef def = new DistanceJointDef();
		def.initialize(b1, b2, results.getCp1(), results.getCp2());
		def.collideConnected = true;
	
		Joint joint = _world.createJoint(def);
		String jointName = "DistanceJoint[" + obj1.getName() + "," + obj2.getName() + "]";
		
		// Currently save it to the physics map, but may need to go to the
		// blackboard.
		_joints.put(jointName, joint);
	}
	
	public void destroyDistanceJoint(PhysicsObject obj1, PhysicsObject obj2) { 
		String jointName = "DistanceJoint[" + obj1.getName() + "," + obj2.getName() + "]";
		Joint joint = _joints.remove(jointName);

		_world.destroyJoint(joint);
		
		// if we decide that we want to store these on the blackboard then
		// we will need to remove them from there as well.
	}
	
	@Override
	public void finish() {
		// probably need to do some clean up here.
		
	}
	
	/**
	 * Test to see if the point lies within the physics world.
	 * @param point
	 * @return
	 */
	public static boolean within(Vec2 point) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		Vec2 min = systemSpace.get(Variable.physicsMin).get(Vec2.class);
		Vec2 max = systemSpace.get(Variable.physicsMax).get(Vec2.class);
		
		logger.debug("Min: " + min + " Max: " + max);
		return point.x > min.x && point.x < max.x && point.y > min.y && point.y < max.y;
	}
}

class GameContactFilter extends DefaultContactFilter {

	@Override
	public boolean rayCollide(Object userData, Shape shape) {
		PhysicsObject obj = (PhysicsObject) shape.getUserData();
		if (obj.getType() == ObjectType.food) { 
			return false;
		}
		
		return super.rayCollide(userData, shape);
	} 
}
