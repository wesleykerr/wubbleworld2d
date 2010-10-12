package edu.arizona.simulator.ww2d.scenario;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.enums.EventType;

/**
 * In PathScenario3 we have two agents who come together
 * have a short conversation and leave in the direction in
 * which they came.
 * @author wkerr
 *
 */
public class PathScenario3 implements Scenario {

	@Override
	public void setup() {
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		List<PhysicsObject> objects = objectSpace.getCognitiveAgents();

		if (objects.size() != 2)
			throw new RuntimeException("Need two agents in order to run PathScenario3!");
		
		// first we determine the line between their two points.
		PhysicsObject obj1 = objects.get(0);
		PhysicsObject obj2 = objects.get(1);
		
		Vec2 direction = obj2.getPPosition().sub(obj1.getPPosition());
		Vec2 normal = new Vec2(direction.x, direction.y);
		normal.normalize();

		float radians = (float) Math.atan2(normal.y, normal.x);
		float PI = (float) Math.PI;
		float PI2 = PI * 0.5f;

		float p = radians + PI2;
		Vec2 vLeft = new Vec2(1.4f * (float) Math.cos(p), 1.4f*(float) Math.sin(p));
		Vec2 vRight = vLeft.mul(-1);
		
		// switch who gets the left and who goes to the right.... (sometimes they both go the
		// same direction)
		if (MathUtils.random.nextDouble() < 0.5) { 
			Vec2 tmp = vLeft;
			vLeft = vRight;
			vRight = tmp;
		}
		
		if (MathUtils.random.nextDouble() < 0.01) { 
			vLeft = vRight;
		}
		

		Vec2 half = direction.mul(0.5f).sub(normal.mul(0.85f));
		
		Vec2 o1Close = obj1.getPPosition().add(half);
		Vec2 o2Close = obj2.getPPosition().sub(half);
		
		List<Vec2> waypoints1 = new ArrayList<Vec2>();
		waypoints1.add(o1Close);
		waypoints1.add(o1Close.add(vLeft));
		waypoints1.add(obj1.getPPosition().add(vLeft));
		
		List<Integer> times1 = new ArrayList<Integer>();
		times1.add(3000);
		times1.add(0);
		times1.add(0);

		Event waypoint1 = new Event(EventType.SET_WAYPOINTS_AND_TIMES);
		waypoint1.addRecipient(obj1);
		waypoint1.addParameter("waypoints", waypoints1);
		waypoint1.addParameter("times", times1);
		EventManager.inst().dispatch(waypoint1);
		
		List<Vec2> waypoints2 = new ArrayList<Vec2>();
		waypoints2.add(o2Close);
		waypoints2.add(o2Close.add(vRight));
		waypoints2.add(obj2.getPPosition().add(vRight));

		List<Integer> times2 = new ArrayList<Integer>();
		times2.add(3000);
		times2.add(0);
		times2.add(0);

		Event waypoint2 = new Event(EventType.SET_WAYPOINTS_AND_TIMES);
		waypoint2.addRecipient(obj2);
		waypoint2.addParameter("waypoints", waypoints2);
		waypoint2.addParameter("times", times2);
		EventManager.inst().dispatch(waypoint2);
		
		// Set positions and orientations
		obj2.getBody().setXForm(obj2.getPPosition(), radians + PI);
		obj1.getBody().setXForm(obj1.getPPosition(), radians);	}

}
