package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.HashMap;

import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class StateObjectSpace  extends Space {
	HashMap<PhysicsObject, FSCState> stateMap = new HashMap<PhysicsObject, FSCState>();
	HashMap<PhysicsObject, FSCState> ephemeral = new HashMap<PhysicsObject, FSCState>();
	
	public void put(PhysicsObject obj, FSCState state){
		stateMap.put(obj, state);
	}
	
	public FSCState get(PhysicsObject obj){
		return stateMap.get(obj);
	}
	
	public void transition(PhysicsObject obj, FSCState state){
		ephemeral.put(obj, stateMap.get(obj));
		stateMap.put(obj, state);
	}
	
	public FSCState getPrev(PhysicsObject obj){
		return ephemeral.get(obj);
	}
	
	public void update(int elapsed){
		ephemeral = new HashMap<PhysicsObject, FSCState>();
	}
}
