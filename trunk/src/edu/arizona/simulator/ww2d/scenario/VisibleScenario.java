package edu.arizona.simulator.ww2d.scenario;

import java.util.Random;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.PerceptionComponent;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.MathUtils;

public class VisibleScenario implements Scenario {
    private static Logger logger = Logger.getLogger( VisibleScenario.class );

	private String _watched;
	private String _watcher;
	
	private boolean _rotate;
	
	private float _range;
	
	public VisibleScenario(String watcher, String watched) { 
		this(watcher, watched, false, PerceptionComponent.SIGHT_RANGE);
	}
	
	public VisibleScenario(String watcher, String watched, float range) { 
		this(watcher, watched, false, range);
	}
	
	public VisibleScenario(String watcher, String watched, boolean rotate) { 
		this(watcher, watched, rotate, PerceptionComponent.SIGHT_RANGE);
	}
	
	public VisibleScenario(String watcher, String watched, boolean rotate, float range) { 
		_watcher = watcher;
		_watched = watched;
		
		_rotate = rotate;
		_range = range;
	}
	
	public void setup() { 
		if (_rotate) { 
			boolean shouldSwitch = MathUtils.random.nextBoolean();
			if (shouldSwitch) { 
				String tmp = _watcher;
				_watcher = _watched;
				_watched = tmp;
			}
		}
		
		// we expect 2 agents and each agent should have a random location....
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		// agent1 is the chaser and agent2 is the chasee
		PhysicsObject watcher = objectSpace.getPhysicsObject(_watcher);
		PhysicsObject watched = objectSpace.getPhysicsObject(_watched);
		if (watched == null || watcher == null)
			throw new RuntimeException("Expect two agents one named agent1 and the other named agent2");

		logger.debug("Position: " + watched.getPPosition());
		
		boolean happy = false;
		while (!happy) { 
			// find a position within the visibility range of the chasee
			int angle = MathUtils.random.nextInt(360);
			float radians = (float) Math.toRadians(angle);
			
			Vec2 pos = watched.getPPosition();
			float x = pos.x + (float) ((_range - 0.25f) * Math.cos(radians));
			float y = pos.y + (float) ((_range - 0.25f) * Math.sin(radians));

			Vec2 newPos = new Vec2(x,y);
			logger.debug("\tWatcher: " + newPos);
			if (PhysicsSubsystem.within(newPos)) { 
				happy = true;
				watcher.getBody().setXForm(newPos, 0);
			}
		}
		
		// Make the watcher look at the watched...
		Vec2 direction = null;
		float rad = 0f;
		
		direction = watched.getPPosition().sub(watcher.getPPosition());
		direction.normalize();
		
		rad = (float) Math.atan2(direction.y, direction.x);
		watcher.getBody().setXForm(watcher.getPPosition(), rad);

		direction = watcher.getPPosition().sub(watched.getPPosition());
		direction.normalize();

		rad = (float) Math.atan2(direction.y, direction.x);
		
		// Now we can add in some random noise.
		float noise = MathUtils.random.nextFloat();
		float range = MathUtils.PI4+MathUtils.PI4;
		
		noise = noise*range + (-MathUtils.PI4);
		
		watched.getBody().setXForm(watched.getPPosition(), rad+noise);
	}
}
