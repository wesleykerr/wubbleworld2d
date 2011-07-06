package edu.arizona.simulator.ww2d.experimental.blocksworld; 

import org.jbox2d.common.Vec2;
import org.newdawn.slick.geom.Vector2f;

import edu.arizona.simulator.ww2d.experimental.blocksworld.objects.BlockFactory;
import edu.arizona.simulator.ww2d.experimental.blocksworld.objects.RampFactory;
import edu.arizona.simulator.ww2d.experimental.blocksworld.objects.TriangleFactory;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.utils.GameGlobals;

public class BlocksworldScenario implements Scenario {

	BlockFactory _blockFactory;
	TriangleFactory _triangleFactory;
	RampFactory _rampFactory;
	
	public BlocksworldScenario() {
		_blockFactory = new BlockFactory();
		_triangleFactory = new TriangleFactory();
		_rampFactory = new RampFactory();
		
		float physicsScale = 12.0f;
		// cameraPos is in rendering scale, so need to adjust coordinates
		// from physics to rendering
		GameGlobals.cameraPos = new Vec2(25*physicsScale,25*physicsScale);
		GameGlobals.cameraScale = 1.0f;
	}
	
	@Override
	public void setup() {
		// x,y,angle,width,linearDamping, angularDamping,density,friction,restitution,hasMass
		_blockFactory.createBox(10.0f, 10.0f, 0.0f, 3.0f, 1.0f, 1.0f, 0.05f, 0.2f, 0.5f,true);
		_blockFactory.createBox(10.0f, 20.0f, 2.0f, 3.0f, 1.0f, 1.0f, 0.05f, 0.2f, 0.5f,true);
		// Vertex 1, 2, 3, angle, linearDamping, angular Damping, density, friction, restitution, hasMass
		// NOTE vertices must be in the counter-clockwise order
		//_triangleFactory.create(new Vector2f(10.0f,30.0f),new Vector2f(10.0f,25.0f),new Vector2f(15.0f,25.0f),0.0f,1.0f,1.0f,.05f,0.2f,0.5f,true);
		//_triangleFactory.create(new Vector2f(9.5f,9.5f),new Vector2f(9.5f,11.5f),new Vector2f(11.5f,11.5f),0.0f,1.0f,1.0f,.05f,0.2f,0.5f,false);
		//_rampFactory.create(new Vector2f(10f,30f), new Vector2f(5,25), true, 1.0f, 1.0f, .05f, .2f, .5f, false);
		//_rampFactory.create(new Vector2f(10f,30f), new Vector2f(5,25), false, 1.0f, 1.0f, .05f, .2f, .5f, false);
	}

}
