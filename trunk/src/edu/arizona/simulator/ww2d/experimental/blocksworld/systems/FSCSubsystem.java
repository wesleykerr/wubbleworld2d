package edu.arizona.simulator.ww2d.experimental.blocksworld.systems;

import java.util.Collection;
import java.util.HashMap;

import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.FSCState;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.Subsystem;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public class FSCSubsystem implements Subsystem {

	HashMap<PhysicsObject,FSCState> statemap = new HashMap<PhysicsObject,FSCState>();
	
	public FSCSubsystem(){
		super();
	}

	@Override
	public SubsystemType getId() {
		return SubsystemType.FSCSubsystem;
	}

	@Override
	public void update(int eps) {
		Collection<PhysicsObject> objs = ((ObjectSpace)Blackboard.inst().getSpace("object")).getPhysicsObjects();
		for(PhysicsObject obj : objs){
			if(statemap.get(obj) != null){
				statemap.put(obj, statemap.get(obj).update(eps));
			}
		}
	}

	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}
	
	public void put(PhysicsObject obj, FSCState initialState){
		statemap.put(obj, initialState);
	}

}
