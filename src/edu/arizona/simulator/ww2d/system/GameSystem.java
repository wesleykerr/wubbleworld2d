package edu.arizona.simulator.ww2d.system;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.factory.ObjectFactory;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.SlickGlobals;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class GameSystem {
    private static Logger logger = Logger.getLogger( GameSystem.class );

	public enum Systems { 
		PhysicsSubystem, SpawnSubsystem, FoodSubsystem, LoggingSubsystem,
	};
	
	private Map<Systems,Subsystem> _systems;


	public GameSystem(int width, int height, boolean global) { 
		logger.debug("New GameSystem");
		_systems = new TreeMap<Systems,Subsystem>();
		
		final Space systemSpace = new Space();
		final ObjectSpace objectSpace = new ObjectSpace();
		
		systemSpace.put(Variable.random, new ValueEntry(new Random()));

		systemSpace.put(Variable.screenWidth, new ValueEntry(width));
		systemSpace.put(Variable.screenHeight, new ValueEntry(height));
		
		systemSpace.put(Variable.centerX, new ValueEntry(width/2));
		systemSpace.put(Variable.centerY, new ValueEntry(height/2));
		
		systemSpace.put(Variable.maxAcceleration, new ValueEntry(1.0f));
		systemSpace.put(Variable.maxAngularAcceleration, new ValueEntry(0.4f));
		systemSpace.put(Variable.maxSpeed, new ValueEntry(5.0f));
		systemSpace.put(Variable.maxRotation, new ValueEntry(2.0f));
		
		systemSpace.put(Variable.logicalTime, new ValueEntry(1L));

		if (global) 
			systemSpace.put(Variable.controlledObject, new ValueEntry(new Integer(-1)));
		else 
			systemSpace.put(Variable.controlledObject, new ValueEntry(new Integer(0)));
		
		Blackboard.inst().addSpace("system", systemSpace);
		Blackboard.inst().addSpace("object", objectSpace);
		
		// initialize the ObjectFactory in order to ensure that it is 
		// listening for important events.
		ObjectFactory.inst();
	}

	/**
	 * Add a subsystem to our map.
	 * @param id
	 * @param s
	 */
	public void addSubsystem(Systems id, Subsystem s) {
		_systems.put(id, s);
	}
	
	public void loadLevel(String levelName, String agentsFile, Scenario scenario) { 
		logger.debug("Loading level: " + levelName);
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		
		SAXReader reader = new SAXReader(false);
		try {
			URL url = this.getClass().getClassLoader().getResource(levelName);
			Document doc = reader.read(url);
			Element root = doc.getRootElement();
		
			float physicsScale = Float.parseFloat(root.element("physicsScale").attributeValue("value"));
			systemSpace.put(Variable.physicsScale, new ValueEntry(physicsScale));

			PhysicsSubsystem physics = (PhysicsSubsystem) _systems.get(Systems.PhysicsSubystem);
			physics.fromXML(root.element("physics"));

			Element objects = root.element("objects");
			if (objects.attribute("walls") != null && Boolean.parseBoolean(objects.attributeValue("walls"))) 
				makeWalls(physics);

			
			List physicsObjs = objects.elements("physicsObject");
			for (int i = 0; i < physicsObjs.size(); ++i) { 
				Element obj = (Element) physicsObjs.get(i);
				
				Event event = new Event(EventType.CREATE_PHYSICS_OBJECT);
				event.addParameter("element", obj);
				EventManager.inst().dispatchImmediate(event);
			}
			List gameObjs = objects.elements("gameObject");
			for (int i = 0; i < gameObjs.size(); ++i) { 
				Element obj = (Element) gameObjs.get(i);

				Event event = new Event(EventType.CREATE_GAME_OBJECT);
				event.addParameter("element", obj);
				EventManager.inst().dispatchImmediate(event);
			}
			
			Element food = root.element("foodSubsystem");
			if (food == null || Boolean.parseBoolean(food.attributeValue("value"))) {
				addSubsystem(Systems.FoodSubsystem, new FoodSubsystem());
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		if (agentsFile != null) {
			try {
				URL url = this.getClass().getClassLoader().getResource(agentsFile);
				Document doc = reader.read(url);
				Element root = doc.getRootElement();

				List physicsObjs = root.elements("physicsObject");
				for (int i = 0; i < physicsObjs.size(); ++i) { 
					Element obj = (Element) physicsObjs.get(i);

					Event event = new Event(EventType.CREATE_PHYSICS_OBJECT);
					event.addParameter("element", obj);
					EventManager.inst().dispatchImmediate(event);
				}

			} catch (DocumentException e) {
				e.printStackTrace();
			}		
		}

		// TODO: here we need to decide what we want to do when we are done loading.
		// Maybe we place a boolean value into the space that a knowledge source watches
		// for.
		if (scenario != null)
			scenario.setup();
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
		Event event = new Event(EventType.CREATE_WALL);
		event.addParameter("name", "wall1");
		event.addParameter("position", position);
		event.addParameter("dimensions", dimensions);
		EventManager.inst().dispatchImmediate(event);
	}
	
	public void update(int elapsed) {
		// Let those who are interested know that we are starting a brand new
		// update.
		Event updateStart = new Event(EventType.UPDATE_START);
		EventManager.inst().dispatchImmediate(updateStart);
		
		for (Subsystem s : _systems.values()) { 
			s.update(elapsed);
		}
		
		// Dispatch all of the messages from the physics system and other subsystems.
		EventManager.inst().update(elapsed);

		Blackboard.inst().update(elapsed);

		// Dispatch all of the messages generated by the blackboard system.
		EventManager.inst().update(elapsed);
		
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		for (GameObject obj : objectSpace.getAll()) { 
			obj.update(elapsed);
		}
		
		Event updateEnd = new Event(EventType.UPDATE_END);
		EventManager.inst().dispatchImmediate(updateEnd);
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		long currentTime = systemSpace.get(Variable.logicalTime).get(Long.class);
		systemSpace.get(Variable.logicalTime).setValue(currentTime+1);
	}
	
	public void render(Graphics g) { 
		if (g == null)
			return;
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		int index = systemSpace.get(Variable.controlledObject).get(Integer.class);
		PhysicsObject pobj = null; 
		if (index >= 0) {
			pobj = objectSpace.getCognitiveAgents().get(index);
			float centerX = systemSpace.get(Variable.centerX).get(Integer.class);
			float centerY = systemSpace.get(Variable.centerY).get(Integer.class);

			g.pushTransform();
			g.resetTransform();
			g.translate(-pobj.getPosition().x+centerX, -pobj.getPosition().y+centerY);
		}
		

		for (GameObject obj : objectSpace.getRenderObjects()) { 
			obj.render(g);
		}
		
		if (index >= 0) {
			g.popTransform();

			// now I would like to render some information about the currently controlled
			// agent....
			AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, pobj.getName());
			BoundedEntry energy = space.getBounded(Variable.energy);

			BoundedEntry valence = space.getBounded(Variable.valence);
			BoundedEntry arousal = space.getBounded(Variable.arousal);

			String target = space.get(Variable.target).get(String.class);

			Color blackAlpha = new Color(Color.black);
			blackAlpha.a = 0.5f;
			g.setColor(blackAlpha);
			g.fillRect(490, 0, 220, 60);

			SlickGlobals.textFont.drawString(500, 0, "Name: " + pobj.getName());
			SlickGlobals.textFont.drawString(500, 10, "Valence: " + GameGlobals.nf.format(valence.getValue()) + 
					" Arousal: " + GameGlobals.nf.format(arousal.getValue()));
			SlickGlobals.textFont.drawString(500, 20, "Energy: " + GameGlobals.nf.format(energy.getValue()));
			SlickGlobals.textFont.drawString(500, 30, "Target: " + target);
		}
	}
	
	public void finish() { 
		Event finishEvent = new Event(EventType.FINISH);
		EventManager.inst().dispatchImmediate(finishEvent);
		
		EventManager.inst().finish();
		Blackboard.inst().finish();
	}
}
