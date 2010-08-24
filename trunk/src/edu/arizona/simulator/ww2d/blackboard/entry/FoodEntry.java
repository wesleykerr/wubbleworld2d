package edu.arizona.simulator.ww2d.blackboard.entry;

import edu.arizona.simulator.ww2d.logging.FluentStore;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class FoodEntry extends Entry {
	public long recorded;
	
	public PhysicsObject obj;
	public float store;
	
	public FoodEntry(PhysicsObject obj) {
		this.obj = obj;
		store = obj.getUserData("store", Float.class);
	}
	
	/**
	 * 
	 * @param fluentStore
	 * @param pobj - the object that is doing the smelling
	 */
	public void record(FluentStore fluentStore, PhysicsObject pobj) { 
		fluentStore.record("smelled", pobj.getName() + " " + obj.getName(), true);
	}
}
