package edu.arizona.simulator.ww2d.level;

import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.spawn.CreateGameObject;
import edu.arizona.simulator.ww2d.events.spawn.CreatePhysicsObject;
import edu.arizona.simulator.ww2d.events.spawn.CreateWall;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.system.FoodSubsystem;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

/**
 * The DefaultLoader will load in a level file consisting
 * of XML.  Objects are created through EventManager messages
 * to the SpawnSubsystem.
 * @author wkerr
 *
 */
public class DefaultLoader implements LevelLoader {
    private static Logger logger = Logger.getLogger( DefaultLoader.class );

	protected String _levelFile;
	protected String _agentsFile;

	protected Scenario _scenario;
	
	public DefaultLoader(String levelFile) { 
		this(levelFile, null, null);
	}
	
	public DefaultLoader(String levelFile, String agentsFile) { 
		this(levelFile, agentsFile, null);
	}
	
	public DefaultLoader(String levelFile, String agentsFile, Scenario scenario) { 
		_levelFile = levelFile;
		_agentsFile = agentsFile;
		
		_scenario = scenario;
	}
	
	/**
	 * Load in the level after correctly initializing
	 * all of the components.  
	 * @param gameSystem -- The initialized GameSystem
	 */
	public void load(GameSystem gameSystem) { 
		logger.debug("Loading level: " + _levelFile);
		
		Space systemSpace = Blackboard.inst().getSpace("system");

		SAXReader reader = new SAXReader(false);
		try {
			URL url = this.getClass().getClassLoader().getResource(_levelFile);
			Document doc = reader.read(url);
			Element root = doc.getRootElement();
		
			float physicsScale = Float.parseFloat(root.element("physicsScale").attributeValue("value"));
			systemSpace.put(Variable.physicsScale, new ValueEntry(physicsScale));

			PhysicsSubsystem physics = (PhysicsSubsystem) gameSystem.getSubsystem(SubsystemType.PhysicsSubsystem);
			physics.fromXML(root.element("physics"));

			Element objects = root.element("objects");
			if (objects.attribute("walls") != null && Boolean.parseBoolean(objects.attributeValue("walls"))) 
				makeWalls(physics);

			World world = systemSpace.get(Variable.physicsWorld).get(World.class);
			List physicsObjs = objects.elements("physicsObject");
			for (int i = 0; i < physicsObjs.size(); ++i) { 
				Element obj = (Element) physicsObjs.get(i);
				EventManager.inst().dispatchImmediate(new CreatePhysicsObject(world, obj));
			}
			List gameObjs = objects.elements("gameObject");
			for (int i = 0; i < gameObjs.size(); ++i) { 
				Element obj = (Element) gameObjs.get(i);
				EventManager.inst().dispatchImmediate(new CreateGameObject(obj));
			}
			
			Element food = root.element("foodSubsystem");
			if (food == null || Boolean.parseBoolean(food.attributeValue("value"))) {
				gameSystem.addSubsystem(SubsystemType.FoodSubsystem, new FoodSubsystem());
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		if (_agentsFile != null) {
			try {
				URL url = this.getClass().getClassLoader().getResource(_agentsFile);
				Document doc = reader.read(url);
				Element root = doc.getRootElement();

				World world = systemSpace.get(Variable.physicsWorld).get(World.class);
				List physicsObjs = root.elements("physicsObject");
				for (int i = 0; i < physicsObjs.size(); ++i) { 
					Element obj = (Element) physicsObjs.get(i);
					EventManager.inst().dispatchImmediate(new CreatePhysicsObject(world, obj));
				}

			} catch (DocumentException e) {
				e.printStackTrace();
			}		
		}

		// TODO: here we need to decide what we want to do when we are done loading.
		// Maybe we place a boolean value into the space that a knowledge source watches
		// for.
		if (_scenario != null)
			_scenario.setup();		
	}
	
	/**
	 * Construct walls that are on the boundaries of the physics world.
	 */
	private void makeWalls(PhysicsSubsystem physics) { 
		// walls are always 0.5 units wide and as long as the world - 2 (+1 buffer on each side)
		AABB aabb = physics.getWorldAABB();
		float width = (aabb.upperBound.x - aabb.lowerBound.x);
		float height = (aabb.upperBound.y - aabb.lowerBound.y);
		
		float w = 0.125f;
		float w2 = 2.0f*w;
		
		
		sendWallEvent("wall1", new Vec2(w, height/2), new Vec2(w2,  height));
		sendWallEvent("wall3", new Vec2(width-w, height/2), new Vec2(w2, height));

		sendWallEvent("wall2", new Vec2(width/2, w), new Vec2(width, w2));
		sendWallEvent("wall4", new Vec2(width/2, height-w), new Vec2(width, w2));
		
	}
	
	/**
	 * Send the actual Wall event out to whoever is listening.
	 * @param name
	 * @param position
	 * @param dimensions
	 */
	private void sendWallEvent(String name, Vec2 position, Vec2 dimensions) { 
		EventManager.inst().dispatchImmediate(new CreateWall("wall1", position, dimensions));
	}
}
