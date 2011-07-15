package edu.arizona.simulator.ww2d.scenario;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.events.player.SetWaypoints;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;

public class PathScenario4 implements Scenario {

	@Override
	public void setup() {
		// we expect 2 agents and each agent should have a random location....
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		List<PhysicsObject> objects = objectSpace.getCognitiveAgents();

		if (objects.size() != 2)
			throw new RuntimeException("Need two agents in order to run PathScenario4!");
		
		// first we determine the line between their two points.
		PhysicsObject obj1 = objects.get(0);
		PhysicsObject obj2 = objects.get(1);
				
		Vec2 direction = obj1.getPPosition().sub(obj2.getPPosition());
		Vec2 nDirection = new Vec2(direction);
		nDirection.normalize();
		float radians = (float) Math.atan2(nDirection.y, nDirection.x);
		float PI = (float) Math.PI;
		float PI2 = PI * 0.5f;

		float perp = radians + PI2;
		Vec2 v = new Vec2((float) Math.cos(perp), (float) Math.sin(perp));
		Vec2 offset1 = new Vec2(obj1.getPPosition().add(v));

		List<Vec2> waypoints1 = new ArrayList<Vec2>();
		waypoints1.add(obj2.getPPosition());

		List<Vec2> waypoints2 = new ArrayList<Vec2>();
		waypoints2.add(offset1);
		
		// Set positions and orientations
		obj1.getBody().setXForm(obj1.getPPosition(), radians + (float) Math.PI);
		obj2.getBody().setXForm(obj2.getPPosition(), radians);

		EventManager.inst().dispatch(new SetWaypoints(waypoints1, obj1));
		EventManager.inst().dispatch(new SetWaypoints(waypoints2, obj2));
	}
}
