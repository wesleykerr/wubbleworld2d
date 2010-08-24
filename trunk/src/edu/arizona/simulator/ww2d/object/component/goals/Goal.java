package edu.arizona.simulator.ww2d.object.component.goals;

import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;


public interface Goal {

	public void activate();
	public GoalEnum process();
	public void terminate();
	
	public GoalEnum getStatus();
	
	public float desireability();
	public boolean succeeding();
}
