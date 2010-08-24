package edu.arizona.simulator.ww2d.object.component.steering.behaviors;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class Wander extends Behavior {
    private static Logger logger = Logger.getLogger( Wander.class );
	
    private float _wanderOffset;
    private float _wanderRadius;
    
    private float _wanderRate;
    private float _wanderOrientation;
    
    private Seek  _seek;
    private Align _align;
    
    private SteeringOutput _output;
    
	/**
	 * 
	 * @param entity
	 * @param target - in radians
	 */
	public Wander(PhysicsObject obj) { 
		super(obj);
		
		// Each entity should have a radius of about 0.5f;
		_wanderOffset = 7.5f;
		_wanderRadius = 3.5f;
		
		_wanderRate = 0.1f;
		_wanderOrientation = (float) Math.PI / 2;
		
		_seek = new Seek(obj, new Vec2());
		_align = new Align(obj, 0);
	}
	
	public SteeringOutput getSteering(float moveModifier, float turnModifier) {
		_wanderOrientation += MathUtils.randomBinomial() * _wanderRate;
		float targetOrientation = _wanderOrientation + _entity.getHeading();
		
		Vec2 direction = MathUtils.toVec2(_entity.getHeading());
		Vec2 target = _entity.getPPosition().add(direction.mul(_wanderOffset));
		
		// Calculate the target location
		target.addLocal(MathUtils.toVec2(targetOrientation).mul(_wanderRadius));
		
		_output = new SteeringOutput(new Vec2(), 0);

		_align.setTarget(target);
		_output.blend(_align.getSteering(moveModifier, turnModifier/4), 1);

		Space systemSpace = Blackboard.inst().getSpace("system");
		float max = systemSpace.get(Variable.maxAcceleration).get(Float.class);
		
		direction.mulLocal(max*moveModifier);
		
		_seek.setTarget(direction.add(_entity.getPPosition()));
		_output.blend(_seek.getSteering(moveModifier, turnModifier), 1);
		
//		if (_entity.getName().equals("agent2")) { 
//			logger.debug("agent2 - target: " + target);
//			logger.debug("\toutput: " + _output);
//		}
//		logger.debug("\t" + _entity.getName() + " position " + _entity.getPPosition());
//		logger.debug(_entity.getName() + " " + so);
		return _output;
	}
	
	@Override
	public void onEvent(Event e) {
		_isOn = (Boolean) e.getValue("status");
	}
	
	@Override
	public void render(Graphics g) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);
		
		// first draw the circle.
		Vec2 pos = _entity.getPPosition();
		Vec2 dir = MathUtils.toVec2(_entity.getHeading());
		
		// determine the position of the wander circle
		Vec2 wPos = pos.add(dir.mul(_wanderOffset));
		Vec2 tmp = wPos.sub(new Vec2(_wanderRadius,_wanderRadius)).mul(scale);

		g.setColor(Color.red);
		g.drawOval(tmp.x, tmp.y, 2*_wanderRadius*scale, 2*_wanderRadius*scale);
		
		float targetOrientation = _wanderOrientation + _entity.getHeading();
		tmp = wPos.add(MathUtils.toVec2(targetOrientation).mul(_wanderRadius));
		tmp.subLocal(new Vec2(0.5f,0.5f)).mulLocal(scale);
		g.setColor(Color.blue);
		g.drawOval(tmp.x, tmp.y, scale, scale);

		Vec2 p = _entity.getPosition();
		Vec2 v = _output.getVelocity().mul(scale).mul(10);
		Vec2 av = MathUtils.toVec2(_entity.getHeading() + _output.getAngular()).mul(scale).mul(10);
		
		g.setColor(Color.black);
		g.drawLine(p.x, p.y, p.x+v.x, p.y+v.y);
		
		g.setColor(Color.yellow);
		g.drawLine(p.x, p.y, p.x+av.x, p.y+av.y);
		
		_align.render(g);
	}
}
