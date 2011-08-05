package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Check;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class ZeroVelocityCheck extends Check{
	
	boolean gracePeriod = true;

	public ZeroVelocityCheck(PhysicsObject owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean check(int elapsed) {
		if(gracePeriod){
			gracePeriod = false;
			return false;
		}
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		if(ofs.retrieve(owner, "dx") == null || ofs.retrieve(owner, "dy") == null){
			return false;
		} else if(0 == Math.floor((Float)ofs.retrieve(owner, "dx").getData()) && 0 == Math.floor((Float)ofs.retrieve(owner, "dy").getData())){
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		gracePeriod = true;
		// TODO Auto-generated method stub
		
	}

}
