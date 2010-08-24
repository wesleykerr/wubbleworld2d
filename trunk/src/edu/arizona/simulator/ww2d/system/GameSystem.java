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
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.factory.ObjectFactory;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class GameSystem {
    private static Logger logger = Logger.getLogger( GameSystem.class );

	public enum Systems { 
		PhysicsSubystem, SpawnSubsystem, FoodSubsystem, LoggingSubsystem,
	};
	
	private Map<Systems,Subsystem> _systems;


	public GameSystem(int width, int height) { 
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
		systemSpace.put(Variable.controlledObject, new ValueEntry(new Integer(0)));
		
		Blackboard.inst().addSpace("system", systemSpace);
		Blackboard.inst().addSpace("object", objectSpace);
		
		// initialize the ObjectFactory in order to ensure that it is 
		// listening for important events.
		ObjectFactory.inst();

		EventManager.inst().registerForAll(EventType.KEY_PRESSED_EVENT, new EventListener() {
			@Override
			public void onEvent(Event e) {
				int key = (Integer) e.getValue("key");
				if (key == Input.KEY_F1) { 
					ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

					int size = objectSpace.getCognitiveAgents().size();
					ValueEntry entry = systemSpace.get(Variable.controlledObject);
					int current = entry.get(Integer.class);

					PhysicsObject previousObj = objectSpace.getCognitiveAgents().get(current);
					
					current = (current + 1) % size;
					entry.setValue(current);
					
					PhysicsObject newObj = objectSpace.getCognitiveAgents().get(current);

					Event controlledEvent = new Event(EventType.CHANGE_CAMERA_FOLLOWING);
					controlledEvent.addParameter("previous-object", previousObj);
					controlledEvent.addParameter("new-object", newObj);
					EventManager.inst().dispatch(controlledEvent);
				}
			} 
		});		
	}

	/**
	 * Add a subsystem to our map.
	 * @param id
	 * @param s
	 */
	public void addSubsystem(Systems id, Subsystem s) {
		_systems.put(id, s);
	}
	
	public void loadLevel(String levelName, String agentsFile) { 
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

		// TODO: here we need to decide what we want to do when we are done loading.
		// Maybe we place a boolean value into the space that a knowledge source watches
		// for.
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
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		int centerX = systemSpace.get(Variable.centerX).get(Integer.class);
		int centerY = systemSpace.get(Variable.centerY).get(Integer.class);

		int index = systemSpace.get(Variable.controlledObject).get(Integer.class);
		PhysicsObject pobj = objectSpace.getCognitiveAgents().get(index);
		g.pushTransform();
		g.resetTransform();
		g.translate(-pobj.getPosition().x+centerX, -pobj.getPosition().y+centerY);

		for (GameObject obj : objectSpace.getRenderObjects()) { 
			obj.render(g);
		}
		
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

		GameGlobals.textFont.drawString(500, 0, "Name: " + pobj.getName());
		GameGlobals.textFont.drawString(500, 10, "Valence: " + GameGlobals.nf.format(valence.getValue()) + 
				     " Arousal: " + GameGlobals.nf.format(arousal.getValue()));
		GameGlobals.textFont.drawString(500, 20, "Energy: " + GameGlobals.nf.format(energy.getValue()));
		GameGlobals.textFont.drawString(500, 30, "Target: " + target);
	}
	
	public void finish() { 
		Event finishEvent = new Event(EventType.FINISH);
		EventManager.inst().dispatchImmediate(finishEvent);
		
		EventManager.inst().finish();
		Blackboard.inst().finish();
		ObjectFactory.inst().finish();
	}
}
