package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Check;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
@Deprecated
public class IntervalCheck extends Check {

	public IntervalCheck(PhysicsObject owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean check(int elapsed) {
		ObjectFieldSpace ofs = (ObjectFieldSpace) Blackboard.inst().getSpace("objectfield");
		return ofs.retrieve(owner, "interval") != null && (Integer)ofs.retrieve(owner, "interval").getData() <=0;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
