package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Action;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class MoveAction extends Action {

	public MoveAction(PhysicsObject owner) {
		super(owner);
	}

	@Override
	public void execute(int elapsed) {

		// Jeff --- getPosition returns the position of the object in _screen_
		// coordinates
		// whereas you want the position of the object in _world_ coordinates.
		// Vec2 pos = owner.getPosition();
		float fraction = (float)elapsed / 1000;
		Vec2 pos = owner.getPPosition();
		Vec2 newPos = new Vec2();
		
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		
		float dx = 0;
		if(ofs.retrieve(owner, "dx") != null){
			dx = (Float) ofs.retrieve(owner, "dx").getData();
		} else {
			ofs.addTemp(owner, new Field("dx", 0f));
		}
		float dy = 0;
		if(ofs.retrieve(owner, "dy") != null){
			dy = (Float) ofs.retrieve(owner, "dy").getData();
		} else {
			ofs.addTemp(owner, new Field("dy", 0f));
		}
		// Update position
		newPos.x = pos.x + (dx * fraction);
		newPos.y = pos.y + (dy * fraction);

//		// Accelerate
//		dx += (ax * fraction);
//		dy += (ay * fraction);
		ofs.addTemp(owner, new Field("x", newPos.x));
		ofs.addTemp(owner, new Field("y", newPos.y));
		owner.setPosition(newPos);	
	}

}
