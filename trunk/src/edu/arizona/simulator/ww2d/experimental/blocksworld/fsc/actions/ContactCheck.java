package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.system.CollisionEvent;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Check;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;

public class ContactCheck extends Check {
	
	protected boolean collision;

	public ContactCheck(PhysicsObject owner) {
		super(owner);
		collision = false;
		CollisionListener cl = new CollisionListener();
		EventManager.inst().register(CollisionEvent.class, owner, cl);
		// TODO Auto-generated constructor stub
	}

	
	private class CollisionListener implements EventListener{

		@Override
		public void onEvent(Event e) {
			CollisionEvent ce = (CollisionEvent)e;
			if(ce.getType().equals("add")){
				collision = true;
			} else if(ce.getType().equals("remove")){
				collision = false;
			}
		}
		
	}
	@Override
	public boolean check(int elapsed) {
		/*
		 * ObjectSpace objSpace = (ObjectSpace) Blackboard.inst().getSpace(
		 * "object"); // Generally, checking all collisions is cheaper than
		 * checking all objects; though in some cases could get more expensive
		 * for(CollisionEntry obj : objSpace.getCollisions()){
		 * if(obj.getObject1().equals(owner) || obj.getObject2().equals(owner)){
		 * return true; } }
		 */

		/*ObjectSpace objSpace = (ObjectSpace) Blackboard.inst().getSpace(
				"object");
		Collection<PhysicsObject> objs = objSpace.getPhysicsObjects();
		for (PhysicsObject obj : objs) {
			if (!obj.equals(owner)) {
				if (obj.getBody().isTouching(owner.getBody())) {
					return true;
				}
			}
		}
		return false;*/
		return collision;
	}

	public void reset() {
		/*ObjectSpace objSpace = (ObjectSpace) Blackboard.inst().getSpace(
				"object");
		for (CollisionEntry obj : objSpace.getCollisions()) {
			if (obj.getObject1().equals(owner)
					|| obj.getObject2().equals(owner)) {
				CollisionEvent ce = new CollisionEvent(new ContactPoint(),
						"remove", owner, obj.getOther(owner));
				EventManager.inst().dispatch(ce);
			}
		}*/
		collision = false;
	}

}
