package edu.arizona.simulator.ww2d.blackboard.entry;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.logging.FluentStore;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class AuditoryEntry extends Entry {
	public long recorded;
	
	// The object's name and type do not change
	// over the course of a simulation
	public PhysicsObject obj;
	public float  distance;
	public Vec2   point;
		
	/**
	 * Construct a memory of this Entity
	 * TODO restrict access to this through some 
	 * sort of shared blackboard, then we could reuse
	 * the memories
	 * @param e
	 */
	public AuditoryEntry(PhysicsObject us, PhysicsObject them) { 
		obj = them;
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		recorded = systemSpace.get(Variable.logicalTime).get(Long.class);
		
		// point will be filled in by the distance function
		point = new Vec2();
		distance = MathUtils.distance(us, them, new Vec2(), point);
	}
	
	/**
	 * 
	 * @param fluentStore
	 * @param pobj - the object who is doing the hearing.
	 */
	public void record(FluentStore fluentStore, PhysicsObject pobj) { 
		fluentStore.record("heard", pobj.getName() + " " + obj.getName(), true);
	}
}
