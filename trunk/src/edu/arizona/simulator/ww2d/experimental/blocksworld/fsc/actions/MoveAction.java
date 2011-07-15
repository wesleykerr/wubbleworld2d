package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Action;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class MoveAction extends Action {
	
	float dx, dy, ax, ay;

	public MoveAction(PhysicsObject owner, float dx, float dy, float ax, float ay) {
		super(owner);
		this.dx = dx;
		this.dy = dy;
		this.ax = ax;
		this.ay = ay;
	}
	
	@Override
	public boolean check() {
		return true;
	}

	@Override
	public void execute() {
		Vec2 pos = owner.getPosition();
		Vec2 newPos = new Vec2();
		
		// Update position
		newPos.x = pos.x + dx;
		newPos.y = pos.y + dy;
		
		// Accelerate
		dx += ax;
		dy += ay;
		
		owner.setPosition(newPos);
	}

}
