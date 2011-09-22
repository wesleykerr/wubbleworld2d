package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.lwjgl.util.vector.Vector2f;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.system.CollisionEvent;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Check;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;

public class BehindContactCheck extends Check{
	private boolean collision;

	public BehindContactCheck(PhysicsObject owner) {
		super(owner);
		collision = false;
		CollisionListener cl = new CollisionListener();
		EventManager.inst().register(CollisionEvent.class, owner, cl);
	}
	
	private class CollisionListener implements EventListener {

		@Override
		public void onEvent(Event e) {
			CollisionEvent ce = (CollisionEvent) e;
			ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst()
					.getSpace("objectfield");
			if (ce.getType().equals("add") /*|| ce.getType().equals("persist")*/) {
				if (ce.getPhysicsObject1().equals(owner)) {
					ofs.addTemp(owner,
							new Field("collideObject", ce.getPhysicsObject2()));
				} else {
					ofs.addTemp(owner,
							new Field("collideObject", ce.getPhysicsObject1()));
				}
				
				ofs.addTemp(owner, new Field("contactPoint",ce.getContactPoint()));
				// collision = true;
			} else if (ce.getType().equals("remove")) {
				// collision = false;
				// ofs.remove(owner, "collideObject");
			}
		}

	}

	@Override
	public boolean check(int elapsed) {
		if(collision){
			ContactPoint cp = (ContactPoint) ((ObjectFieldSpace)Blackboard.inst().getSpace("objectfield")).getMap(owner).get("contactPoint").getData();
			float dx = (Float)((ObjectFieldSpace)Blackboard.inst().getSpace("objectfield")).getMap(owner).get("dx").getData();
			float dy = (Float)((ObjectFieldSpace)Blackboard.inst().getSpace("objectfield")).getMap(owner).get("dy").getData();
			Vec2 vel = new Vec2(dx,dy);
			if(Vec2.dot(cp.normal, vel) < 0){
				System.out.println("hi");
				return true;
			}
		}
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
