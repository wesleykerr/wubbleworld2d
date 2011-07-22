package edu.arizona.simulator.ww2d.blackboard.updates;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.CollisionEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.system.CollisionEvent;
import edu.arizona.simulator.ww2d.events.system.ContactEvent;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
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
		EventManager.inst().registerForAll(CollisionEvent.class, new CollisionListener());
		EventManager.inst().registerForAll(ContactEvent.class, new ContactResultListener());
	}

	class ContactResultListener implements EventListener {
		@Override
		public void onEvent(Event e) {
			ContactEvent event = (ContactEvent) e;
			ContactResult cr = event.getContactResult();

			// update the generic collision information.
			String key = CollisionEntry.key(cr, event.getPhysicsObject1(), event.getPhysicsObject2());
			ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
			CollisionEntry entry = objectSpace.getCollision(key);
			entry.update(cr);
		} 
	}
	
	class CollisionListener implements EventListener {
		@Override
		public void onEvent(Event e) {
			// these will be CollisionEvents and they just need to be written to the correct 
			// places on the blackboard.  We will be using shared references in order
			// to speed things up.  On persist, you only need to update the
			// the ObjectSpace collision entry.
			CollisionEvent event = (CollisionEvent) e;
			ContactPoint cp = event.getContactPoint();
			String type = event.getType();
			
			if ("add".equals(type))  
				add(event);
			else if ("persist".equals(type)) 
				persist(event);
			else if ("remove".equals(type))
				remove(event);
		}
			
		private void add(CollisionEvent event) { 
			ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
			
			PhysicsObject obj1 = event.getPhysicsObject1();
			PhysicsObject obj2 = event.getPhysicsObject2();

			CollisionEntry entry = new CollisionEntry(event.getContactPoint(), obj1, obj2);

			objectSpace.addCollision(entry);
			if (Blackboard.inst().spaceExists(obj1.getName())) {
				AgentSpace space1 = Blackboard.inst().getSpace(AgentSpace.class, obj1.getName());
				space1.add(entry);
			}
			
			if (Blackboard.inst().spaceExists(obj2.getName())) {
				AgentSpace space2 = Blackboard.inst().getSpace(AgentSpace.class, obj2.getName());
				space2.add(entry);
			}
		}
		
		/**
		 * Only need to up the object space contact point since the others are
		 * shared references.
		 * @param cp
		 */
		private void persist(CollisionEvent event) { 
			ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
			PhysicsObject obj1 = event.getPhysicsObject1();
			PhysicsObject obj2 = event.getPhysicsObject2();

			String key = CollisionEntry.key(event.getContactPoint(), obj1, obj2);
			CollisionEntry entry = objectSpace.getCollision(key);
			entry.update(event.getContactPoint());
		}
		
		private void remove(CollisionEvent event) { 
			ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
			
			PhysicsObject obj1 = event.getPhysicsObject1();
			PhysicsObject obj2 = event.getPhysicsObject2();
			String key = CollisionEntry.key(event.getContactPoint(), obj1, obj2);

			objectSpace.removeCollision(key, obj1, obj2);
			
			if (Blackboard.inst().spaceExists(obj1.getName())) {
				AgentSpace space1 = Blackboard.inst().getSpace(AgentSpace.class, obj1.getName());
				space1.remove(event.getContactPoint(), obj1, obj2);
			}
			
			if (Blackboard.inst().spaceExists(obj2.getName())) {
				AgentSpace space2 = Blackboard.inst().getSpace(AgentSpace.class, obj2.getName());
				space2.remove(event.getContactPoint(), obj1, obj2);
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
