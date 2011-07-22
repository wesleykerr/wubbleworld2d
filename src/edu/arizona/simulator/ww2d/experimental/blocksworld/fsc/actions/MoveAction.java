package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Action;
import edu.arizona.simulator.ww2d.experimental.blocksworld.systems.FSCSubsystem;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public class MoveAction extends Action {

	float dx, dy, ax, ay;

	public MoveAction(PhysicsObject owner, float dx, float dy, float ax,
			float ay) {
		super(owner);
		this.dx = dx;
		this.dy = dy;
		this.ax = ax;
		this.ay = ay;
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

		// Update position
		newPos.x = pos.x + (dx * fraction);
		newPos.y = pos.y + (dy * fraction);

		// Accelerate
		dx += (ax * fraction);
		dy += (ay * fraction);

		owner.setPosition(newPos);
		
	}

}