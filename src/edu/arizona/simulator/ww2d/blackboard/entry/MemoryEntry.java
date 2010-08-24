package edu.arizona.simulator.ww2d.blackboard.entry;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.logging.FluentStore;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class MemoryEntry extends Entry {
	public static final float PI2 = (float) (2f * Math.PI);
	
	public long recorded;
	
	public PhysicsObject obj;
	
	public Vec2   position;
	public float  orientation;

	public Vec2   velocity;
	public float  rotation;

	public float  energy;
	public float  arousal;
	public float  valence;
	
	/**
	 * Construct a memory of this Entity
	 * TODO restrict access to this through some 
	 * sort of shared blackboard, then we could reuse
	 * the memories
	 * @param e
	 */
	public MemoryEntry(PhysicsObject e) { 
		obj = e;
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		recorded = systemSpace.get(Variable.logicalTime).get(Long.class);

		Vec2 tmp = e.getPosition();
		position = new Vec2(tmp.x, tmp.y);
		orientation = e.getHeading();
		
		while (orientation < 0) { 
			orientation += PI2;
		}
		
		while (orientation > PI2) {
			orientation -= PI2;
		}

		Body b = e.getBody();
		Vec2 v = b.getLinearVelocity();
		velocity = new Vec2(v.x, v.y);
		rotation = b.getAngularVelocity();
		
		if (e.getType() == ObjectType.cognitiveAgent || e.getType() == ObjectType.reactiveAgent) { 
			// these have health values.
			Space agentSpace = Blackboard.inst().getSpace(AgentSpace.class, e.getName());
			energy = agentSpace.getBounded(Variable.energy).getValue();
			arousal = agentSpace.getBounded(Variable.arousal).getValue();
			valence = agentSpace.getBounded(Variable.valence).getValue();
		} 
	}
	
	public void record(FluentStore fluentStore) { 
		fluentStore.record("x", obj.getName(), position.x);
		fluentStore.record("y", obj.getName(), position.y);
		fluentStore.record("heading", obj.getName(), orientation);

		fluentStore.record("speed", obj.getName(), velocity.length());
		fluentStore.record("vx", obj.getName(), velocity.x);
		fluentStore.record("vy", obj.getName(), velocity.y);
		fluentStore.record("rotation", obj.getName(), rotation);
		
		fluentStore.record("turningLeft", obj.getName(), rotation < 0);
		fluentStore.record("turningRight", obj.getName(), rotation > 0);
		
		fluentStore.record("moving", obj.getName(), velocity.lengthSquared() > 0);
		
		if (obj.getType() == ObjectType.cognitiveAgent || obj.getType() == ObjectType.reactiveAgent) {
			fluentStore.record("energy", obj.getName(), energy);
			fluentStore.record("arousal", obj.getName(), arousal);
			fluentStore.record("valence", obj.getName(), valence);
		}
	}
}
