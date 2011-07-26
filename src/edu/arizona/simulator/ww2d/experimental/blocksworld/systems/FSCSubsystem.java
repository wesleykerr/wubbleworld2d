package edu.arizona.simulator.ww2d.experimental.blocksworld.systems;

import java.util.Collection;

import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.FSCState;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.StateFieldSpace;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.StateObjectSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.system.Subsystem;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public class FSCSubsystem implements Subsystem {

	public static GameSystem system;
	
	public FSCSubsystem(){
		super();
		Blackboard.inst().addSpace("statefield", new StateFieldSpace());
		Blackboard.inst().addSpace("statespace", new StateObjectSpace());
	}

	@Override
	public SubsystemType getId() {
		return SubsystemType.FSCSubsystem;
	}

	@Override
	public void update(int eps) {
		Collection<PhysicsObject> objs = ((ObjectSpace)Blackboard.inst().getSpace("object")).getPhysicsObjects();
		StateObjectSpace sobjSpace = (StateObjectSpace)Blackboard.inst().getSpace("statespace");
		for(PhysicsObject obj : objs){
			FSCState state = sobjSpace.get(obj);
			if(state != null){
				FSCState newState = state.update(eps);
				if(!newState.equals(state)){
					sobjSpace.transition(obj, newState);
				}
			}
		}
		
		sobjSpace.update(eps);
		((PhysicsSubsystem) FSCSubsystem.system.getSubsystem(SubsystemType.PhysicsSubsystem)).getPhysics().step(0f,0);
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
		((StateObjectSpace)Blackboard.inst().getSpace("statespace")).put(obj, initialState);
	}

}
