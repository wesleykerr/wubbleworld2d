package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Check;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;


public class TimeCheck extends Check {
	
	private int interval;

	public TimeCheck(PhysicsObject owner, int interval) {
		super(owner);
		this.interval = interval;
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean check(int elapsed) {
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		int currTime = 0;
		if(ofs.retrieve(owner, "currTime") != null){
			currTime = (Integer)ofs.retrieve(owner, "currTime").getData();
		}
		currTime += elapsed;
		
		ofs.addTemp(owner, new Field("currTime",currTime));
		if(ofs.retrieve(owner, "interval") == null){
			return false;
		}
		return currTime > (Integer)ofs.retrieve(owner,"interval").getData() || currTime > interval && interval > -1;
	}
	
	public void reset(){
//		currTime = 0;
//		interval /= 2;
	}

}
