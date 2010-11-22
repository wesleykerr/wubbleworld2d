package edu.arizona.simulator.ww2d.scenario;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.SonarReading;

public class VisibleScenario implements Scenario {
    private static Logger logger = Logger.getLogger( VisibleScenario.class );

	private String _watched;
	private String _watcher;
	
	private boolean _rotate;
	
	public VisibleScenario(String watcher, String watched) { 
		_watcher = watcher;
		_watched = watched;
	}
	
	public VisibleScenario(String watcher, String watched, boolean rotate) { 
		this(watcher, watched);
		
		_rotate = rotate;
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
			float x = pos.x + (float) ((SonarReading.DISTANCE - 5) * Math.cos(radians));
			float y = pos.y + (float) ((SonarReading.DISTANCE - 5) * Math.sin(radians));

			Vec2 newPos = new Vec2(x,y);
			logger.debug("\tWatcher: " + newPos);
			if (PhysicsSubsystem.within(newPos)) { 
				happy = true;
				
				Vec2 direction = pos.sub(newPos);
				direction.normalize();
				
				float rad = (float) Math.atan2(direction.y, direction.x);
				
				watcher.getBody().setXForm(newPos, rad);
			} 
		}
	}
}
