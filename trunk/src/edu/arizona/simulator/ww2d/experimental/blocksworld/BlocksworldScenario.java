package edu.arizona.simulator.ww2d.experimental.blocksworld;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.experimental.blocksworld.objects.BlockFactory;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.utils.GameGlobals;

public class BlocksworldScenario implements Scenario {

	BlockFactory _blockFactory;
	
	public BlocksworldScenario() {
		_blockFactory = new BlockFactory();
		
		GameGlobals.cameraPos = new Vec2(25,25);
		GameGlobals.cameraScale = 1.0f;
	}
	
	@Override
	public void setup() {
		// x,y,width,density,friction,restitution
		_blockFactory.create(2.0f, 2.0f, 1.0f, 0.05f, 0.2f, 0.5f);
		_blockFactory.create(2.0f, 4.0f, 1.0f, 0.05f, 0.2f, 0.5f);
	}

}
