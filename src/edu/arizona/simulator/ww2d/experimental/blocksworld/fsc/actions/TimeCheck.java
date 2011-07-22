package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Check;
import edu.arizona.simulator.ww2d.object.PhysicsObject;


public class TimeCheck extends Check {
	int interval;
	int currTime;
	public TimeCheck(PhysicsObject owner, int interval) {
		super(owner);
		currTime = 0;
		this.interval = interval;
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean check(int elapsed) {
		currTime += elapsed;
		return currTime > interval;
	}
	
	public void reset(){
		currTime = 0;
	}

}
