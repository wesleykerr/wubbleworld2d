package edu.arizona.simulator.ww2d.experimental.blocksworld.objects;

import org.dom4j.Element;
import org.jbox2d.dynamics.World;
import org.newdawn.slick.geom.Vector2f;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.spawn.CreatePhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class RampFactory {

	int _count;
	TriangleFactory triFact;
	BlockFactory blockFact;

	public RampFactory() {
		_count = 0;
		triFact = new TriangleFactory();
		blockFact = new BlockFactory();

	}

	// Rectangular is non-functional
	public void create(Vector2f start, Vector2f end, boolean triangle,
			float linearDamping, float angularDamping, float density,
			float friction, float restitution, boolean hasMass) {
		Space systemSpace = Blackboard.inst().getSpace("system");
		World world = systemSpace.get(Variable.physicsWorld).get(World.class);
		Element rampDef = null;
		if (triangle) {
			Vector2f v2 = new Vector2f();
			v2.y = Math.max(start.y, end.y);
			if (v2.y == start.y) {
				v2.x = end.x;
			} else {
				v2.x = start.x;
			}
			rampDef = triFact.triDef(start, v2, end, 0.0f, linearDamping,
					angularDamping, density, friction, restitution, hasMass);
		} else {
			double dist = Math.sqrt(Math.pow(start.x - end.x, 2)
					+ Math.pow(start.y - end.y, 2));
			float x = Math.abs(start.x - end.x);
			float y = Math.abs(start.y - end.y);
			double angle = Math.atan(y / x);
			
			
			
			double Xvertex = start.x + dist / 2;
			float Yvertex = start.y + .5f;

			rampDef = blockFact.blockDef((float) Xvertex, Yvertex,
					(float)- angle, (float)dist, 1.0f, linearDamping, angularDamping, density,
					friction, restitution, hasMass);

		}
		Event e = new CreatePhysicsObject(world, rampDef);
		EventManager.inst().dispatchImmediate(e);
	}
}
