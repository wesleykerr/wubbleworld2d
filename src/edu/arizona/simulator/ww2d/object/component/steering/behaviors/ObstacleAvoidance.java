package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.apache.log4j.Logger;
import org.jbox2d.collision.Segment;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.RaycastResult;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class ObstacleAvoidance extends Behavior {
    private static Logger logger = Logger.getLogger( ObstacleAvoidance.class );

	private SteeringOutput _empty;
	
	private float _lookAhead;
	private float _avoidDistance;
	
	private Seek _seek;
	private Align _align;
	
	// the maximum number of collisions we care about are 10
	private Shape[] _shapes = new Shape[10];
	
	public ObstacleAvoidance(PhysicsObject obj) { 
		super(obj);
		_seek = new Seek(obj, new Vec2());
		_align = new Align(obj, 0);
		
		_lookAhead = 10;
		_avoidDistance = 15;
		
		_empty = new SteeringOutput(new Vec2(), 0);
	}
	
	public SteeringOutput getSteering(float moveModifier, float turnModifier) {
		Vec2 lookAhead = MathUtils.toVec2(_entity.getHeading());
		lookAhead.normalize();
		lookAhead.mulLocal(_lookAhead);

		Segment s = new Segment();
		s.p1.set(_entity.getPPosition());
		s.p2.set(_entity.getPPosition().add(lookAhead));
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		World world = systemSpace.get(Variable.physicsWorld).get(World.class);

		SteeringOutput so = new SteeringOutput(new Vec2(), 0);
		int found = world.raycast(s, _shapes, 10, false, null);
		for (int i = 0; i < found; ++i) { 
			if (_shapes[i].getBody().getMass() == 0) { 
				RaycastResult rs = new RaycastResult();
				PhysicsObject obj = (PhysicsObject) _shapes[i].getUserData();
				
				// Populate the Raycast result so that we can get the normal.
				_shapes[i].testSegment(_shapes[i].getBody().getMemberXForm(), rs, s, 1.0f);
				Vec2 cp = s.p1.mul(1-rs.lambda).add(s.p2.mul(rs.lambda));
				Vec2 target = cp.add(rs.normal.mul(_avoidDistance));

//				logger.debug(_entity.getName() + " COLLISION SOON: " + obj.getName() + " " + cp  + " " + target);

				_seek.setTarget(target);
				_align.setTarget(target);

				so.blend(_seek.getSteering(moveModifier, turnModifier), 1);
				so.blend(_align.getSteering(moveModifier, turnModifier), 1);
			}
		}
//		if (found > 0) { 
//			logger.debug(_entity.getName() + " pposition: " + _entity.getPPosition());
//			logger.debug(_entity.getName() + " " + so.toString());
//		}		
		return so;
	}
	
	@Override
	public void onEvent(Event e) {
		_isOn = (Boolean) e.getValue("status");
	}
}
