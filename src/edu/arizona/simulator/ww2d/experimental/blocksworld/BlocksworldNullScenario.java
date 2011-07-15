package edu.arizona.simulator.ww2d.experimental.blocksworld;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.experimental.blocksworld.objects.BlockFactory;
import edu.arizona.simulator.ww2d.experimental.blocksworld.objects.RampFactory;
import edu.arizona.simulator.ww2d.experimental.blocksworld.objects.TriangleFactory;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.utils.GameGlobals;

public class BlocksworldNullScenario implements Scenario{

	public BlocksworldNullScenario() {
		
		float physicsScale = 12.0f;
		// cameraPos is in rendering scale, so need to adjust coordinates
		// from physics to rendering
		GameGlobals.cameraPos = new Vec2(25*physicsScale,25*physicsScale);
		GameGlobals.cameraScale = 1.0f;
	}
	
	@Override
	public void setup() {
		// TODO Auto-generated method stub
		
	}

}
