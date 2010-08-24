package edu.arizona.simulator.ww2d.blackboard.updates;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.CollisionEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;

/**
 * This class listens for collision events and adds them to 
 * the blackboard so that they are consolidated in the correct place.
 * 
 * We could also have other listeners do different stuff, but this
 * is like the little boy running into the board room with an update
 * from the front line that needs to be written onto the blackboard
 * @author wkerr
 *
 */
public class CollisionUpdater {
    private static Logger logger = Logger.getLogger( CollisionUpdater.class );

	public CollisionUpdater() { 
		EventManager.inst().registerForAll(EventType.COLLISION_EVENT, new CollisionListener());
		EventManager.inst().registerForAll(EventType.CONTACT_RESULT_EVENT, new ContactResultListener());
	}

	class ContactResultListener implements EventListener {
		@Override
		public void onEvent(Event e) {
			ContactResult cr = (ContactResult) e.getValue("contact-result");

			PhysicsObject obj1 = (PhysicsObject) cr.shape1.getUserData();
			PhysicsObject obj2 = (PhysicsObject) cr.shape2.getUserData();

//			logger.debug("ContactResult::onEvent: " + obj1.getName() + " " + obj2.getName());
			if (Blackboard.inst().spaceExists(obj1.getName())) {
				AgentSpace space1 = Blackboard.inst().getSpace(AgentSpace.class, obj1.getName());
				CollisionEntry entry = space1.getCollisionEntry(obj2, cr.id.features.toString());
				entry.update(cr);
			}

			if (Blackboard.inst().spaceExists(obj2.getName())) {
				AgentSpace space2 = Blackboard.inst().getSpace(AgentSpace.class, obj2.getName());
				CollisionEntry entry = space2.getCollisionEntry(obj1, cr.id.features.toString());
				entry.update(cr);
			}			
		} 
	}
	
	class CollisionListener implements EventListener {
		@Override
		public void onEvent(Event e) {
			// these will be CollisionEvents and they
			// just need to be written to the correct 
			// places on the blackboard.
			ContactPoint cp = (ContactPoint) e.getValue("contact-point");
			String type = (String) e.getValue("type");
			
			PhysicsObject obj1 = (PhysicsObject) cp.shape1.getUserData();
			PhysicsObject obj2 = (PhysicsObject) cp.shape2.getUserData();

//			logger.debug("CollisionListener::onEvent: " + obj1.getName() + " " + obj2.getName());
			CollisionEntry entry = null;
			if ("add".equals(type)) { 
				entry = new CollisionEntry(cp);
			}

//			if (!"persist".equals(type)) 
//				logger.debug("Collision: " + type + " " + obj1.getName() + " " + obj2.getName());

//			if ("agent4".equals(obj1.getName()) || "agent4".equals(obj2.getName())) { 
//				print(obj1, obj2, type, cp);
//			}
			
			if (Blackboard.inst().spaceExists(obj1.getName())) {
				AgentSpace space1 = Blackboard.inst().getSpace(AgentSpace.class, obj1.getName());
				if ("add".equals(type)) 
					space1.add(entry);
				else if ("persist".equals(type))
					space1.persist(cp);
				else if ("remove".equals(type))
					space1.remove(cp);
			}

			if (Blackboard.inst().spaceExists(obj2.getName())) {
				AgentSpace space2 = Blackboard.inst().getSpace(AgentSpace.class, obj2.getName());
				if ("add".equals(type)) 
					space2.add(entry);
				else if ("persist".equals(type))
					space2.persist(cp);
				else if ("remove".equals(type))
					space2.remove(cp);
			}
		}
	}
	
	private void print(PhysicsObject obj1, PhysicsObject obj2, String type, ContactPoint cp) {
		if (obj1.getType() == ObjectType.food || obj2.getType() == ObjectType.food)
			return;
		
		Vec2 v1 = obj1.getBody().getLinearVelocity();
		Vec2 v2 = obj2.getBody().getLinearVelocity();
		
		logger.debug(type + " collision");
		logger.debug("\tObject1: " + obj1.getName() + " pos: " + obj1.getPPosition() + " vel: " + v1 + " speed: " + v1.length());
		logger.debug("\tObject2: " + obj2.getName() + " pos: " + obj2.getPPosition() + " vel: " + v2 + " speed: " + v2.length());
		logger.debug("\t\tpos: " + cp.position + " vel: " + cp.velocity + " speed: " + cp.velocity.length() + " normal: " + cp.normal);		
	}
}
