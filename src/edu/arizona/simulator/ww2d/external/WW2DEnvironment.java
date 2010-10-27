package edu.arizona.simulator.ww2d.external;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.newdawn.slick.Input;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.AuditoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.DistanceEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.MemoryEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.verbs.environment.Environment;
import edu.arizona.verbs.mdp.OOMDPObjectState;
import edu.arizona.verbs.mdp.OOMDPState;
import edu.arizona.verbs.mdp.Relation;

public class WW2DEnvironment implements Environment {

	private VerbGameContainer _container;
	private GameSystem _gameSystem;
	
	private List<OOMDPObjectState> _state;
	
	private NumberFormat _format;

	public WW2DEnvironment(boolean visualize) { 
		_format = NumberFormat.getInstance();
		_format.setMinimumFractionDigits(3);
		_format.setMaximumFractionDigits(3);

		if (visualize) {
			try { 
				_container = new VerbGameContainer(800, 800);
				_container.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Initializes the GameSystem so that we can run
		// different initializations.
		GameGlobals.record = false;
		
		_gameSystem = new GameSystem(800, 800, true);		
		_gameSystem.addSubsystem(GameSystem.Systems.PhysicsSubystem, new PhysicsSubsystem());
	}
	
	public void cleanup() { 
		_gameSystem.finish();
		_container.destroy();
	}
	
	// MDPObjectState -- This corresponds to the objects and thier attributes
	//    -- Class Name: cognitiveAgent, dynamic, obstacle, wall, food
	//    -- Attributes: x, y, shape, width, height
	//       -- shapes : circle, rectangle  (squares are subsets)
	/**
	 * Return a list of the available actions.
	 * We have four possible actions for now for each of the cognitive agents.
	 *   Forward Left Right Back
	 * To make things easier to parse, we just have a string
	 * of four binary values for if the control is true or false.
	 *   0000 -- all off
	 *   1000 -- only forward
	 *   ...
	 *   1111 -- all on
	 *   
	 * All off is a no-op and all on is a wasted action.
	 * Action message format
	 * 		objectName 0000; objectName 1111; ...
	 * Semicolons and spaces separate everything for easy parsing.
	 */
	public List<String> getActions() {
		List<String> actions = new ArrayList<String>();
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		List<PhysicsObject> agents = objectSpace.getCognitiveAgents();

		List<List<String>> actionsByAgent = new ArrayList<List<String>>(agents.size());
		for (PhysicsObject obj : objectSpace.getCognitiveAgents()) { 
			List<String> tmp = new ArrayList<String>();
			for (int i = 0; i < 16; ++i) { 
				StringBuffer buf = new StringBuffer(Integer.toBinaryString(i));
				while (buf.length() < 4) 
					buf.insert(0, '0');
				int w = buf.length();
				
				// Cannot go forward and backward at the same time.
				if (buf.charAt(0) == '1' && buf.charAt(3) == '1')
					continue;
				
				if (buf.charAt(1) == '1' && buf.charAt(2) == '1')
					continue; 
				
				tmp.add(obj.getName() + " " + buf.substring(w-4).toString());
			}
			actionsByAgent.add(tmp);
		}
		
		int[] counters = new int[agents.size()];
		boolean done = false;
		while (!done) { 			
			// construct the actual action...
			StringBuffer action = new StringBuffer();
			for (int i = 0; i < counters.length; ++i) { 
				action.append(actionsByAgent.get(i).get(counters[i]) + ";");
			}
			action.deleteCharAt(action.length()-1);
			actions.add(action.toString());
			
			// increment the last counter and carry values over.
			int last = agents.size()-1;
			boolean keepGoing = true;
			while (keepGoing && last >= 0) { 
				counters[last] += 1;
				if (counters[last] < 9) {
					keepGoing = false;
				} else { 
					counters[last] = 0;
					--last;
				}
			}
			
			if (last < 0)
				done = true;
		}
		
		return actions;
	}

	@Override
	public OOMDPState initializeEnvironment(List<OOMDPObjectState> state) {
		_state = new ArrayList<OOMDPObjectState>(state);
		
		_gameSystem.finish();
		_gameSystem = new GameSystem(800, 800, true);		
		_gameSystem.addSubsystem(GameSystem.Systems.PhysicsSubystem, new PhysicsSubsystem());
		_gameSystem.loadLevel("data/levels/Room-External.xml", null, null);

		for (OOMDPObjectState obj : state) { 
			ClassType type = ClassType.valueOf(obj.getClassName());
			Element e = type.convert(obj);

			Event event = new Event(EventType.CREATE_PHYSICS_OBJECT);
			event.addParameter("element", e);
			EventManager.inst().dispatchImmediate(event);
		}

		// Do an initial update to get the world turning....
		_gameSystem.update(0);
		
		if (_container != null)
			_container.render(_gameSystem);
		
		// TODO: build up the OOMDPState by adding the relations to
		// the current state of the world.
		updateState();
		List<Relation> relations = computeAllRelations();
		return new OOMDPState(_state, relations);
	}

	@Override
	public OOMDPState performAction(String action) {
		go(action);

		updateState();
		List<Relation> relations = computeAllRelations();
		return new OOMDPState(_state, relations);
	}

	@Override
	public OOMDPState simulateAction(OOMDPState state, String action) {
		go(action);

		updateState();
		List<Relation> relations = computeAllRelations();
		return new OOMDPState(_state, relations);
	}

	/**
	 * Iterate over all of the objects and build the set of 
	 * relations that are important.  We want all true and
	 * false relations.
	 * @return
	 */
	private List<Relation> computeAllRelations() { 
		List<Relation> list = new ArrayList<Relation>();
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		List<PhysicsObject> objects = new ArrayList<PhysicsObject>(objectSpace.getPhysicsObjects());

		// First add all of the propositions that involve a single object.
		for (int i = 0; i < objects.size(); ++i) { 
			PhysicsObject obj = objects.get(i);
			if (obj.getType() == ObjectType.wall)
				continue;
			
			String[] objArray = new String[] { obj.getName() };
			
			boolean moving = false;
			if (obj.getBody().getLinearVelocity().lengthSquared() > 0)  
				moving = true;
			list.add(new Relation("Moving", objArray, moving));
			
			boolean turning = false;
			boolean turningLeft = false;
			boolean turningRight = false;
			float angVel = obj.getBody().getAngularVelocity();
			if (Float.compare(angVel, 0) != 0)
				turning = true;
			if (angVel < 0)
				turningLeft = true;
			if (angVel > 0)
				turningRight = true;
			
			list.add(new Relation("Rotating", objArray, turning));
			list.add(new Relation("RotatingLeft", objArray, turningLeft));
			list.add(new Relation("RotatingRight", objArray, turningRight));
			
			// Compute all of the pairwise relations.
			for (int j = i+1; j < objects.size(); ++j) { 
				PhysicsObject obj2 = objects.get(j);
				if (obj.getType() == ObjectType.wall)
					continue;				
				
				String[] relArray = new String[] { obj.getName(), obj2.getName() };
				String[] symArray = new String[] { obj2.getName(), obj.getName() };

				DistanceEntry current = objectSpace.findOrAddDistance(obj, obj2);
				DistanceEntry previous = objectSpace.getDistance(obj, obj2, 1);

				boolean dd = false;
				boolean ds = false;
				boolean di = false;
				if (previous != null) { 
					int value = Float.compare(current.getDistance(), previous.getDistance());
					if (value < 0) dd = true;
					if (value > 0) di = true;
					if (value == 0) ds = true;
				}
				
				list.add(new Relation("DistanceDecreasing", relArray, dd));
				list.add(new Relation("DistanceDecreasing", symArray, dd));
				
				list.add(new Relation("DistanceIncreasing", relArray, di));
				list.add(new Relation("DistanceIncreasing", symArray, di));
				
				list.add(new Relation("DistanceStable", relArray, ds));
				list.add(new Relation("DistanceStable", symArray, ds));

				list.add(new Relation("Collision", relArray, objectSpace.isCollision(obj, obj2)));
				list.add(new Relation("Collision", symArray, objectSpace.isCollision(obj, obj2)));
			}
		}

		// Now we gather the perceptive system updates for each of the cognitive
		// agents.
		for (PhysicsObject obj : objectSpace.getCognitiveAgents()) { 
			AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, obj.getName());
			
			Map<String,AuditoryEntry> auditoryMap = agentSpace.getAuditoryMemories().getFirst();
			Map<String,MemoryEntry> visualMap = agentSpace.getVisualMemories().getFirst();
			
			for (PhysicsObject other : objects) { 
				String[] relArray = new String[] { obj.getName(), other.getName() };

				if (obj.getName().equals(other.getName()))
					continue;
				
				boolean heard = auditoryMap.containsKey(other.getName());
				list.add(new Relation("Heard", relArray, heard));
				
				boolean seen = visualMap.containsKey(other.getName());
				list.add(new Relation("Seen", relArray, seen));
			}
		}
		
//		System.out.println("Compute all relations: " + list);
//		for (Relation r : list) { 
//			System.out.println(r.toString() + " -- " + r.value);
//		}
		return list;
	}
	
	/**
	 * Iterate over all the stored original state and put
	 * in the new values for x,y and the other real-valued
	 * information that has changed.
	 */
	private void updateState() { 
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		for (OOMDPObjectState obj : _state) { 
			PhysicsObject pObject = objectSpace.getPhysicsObject(obj.getName());
			
			obj.setAttribute("x", _format.format(pObject.getPPosition().x));
			obj.setAttribute("y", _format.format(pObject.getPPosition().y));
			
			obj.setAttribute("heading", _format.format(pObject.getHeading()));
			
			obj.setAttribute("vx", _format.format(pObject.getBody().getLinearVelocity().x));
			obj.setAttribute("vy", _format.format(pObject.getBody().getLinearVelocity().y));
		}
	}
	
	/**
	 * Walk the simulation forward for some period of time.
	 * We have four possible actions for now.
	 *   Forward Left Right Back
	 * To make things easier to parse, we just have a string
	 * of four binary values for if the control is true or false.
	 *   0000 -- all off
	 *   1000 -- only forward
	 *   ...
	 *   1111 -- all on
	 */
	private void go(String action) {
		int[] keys = new int[] { Input.KEY_W, Input.KEY_A, Input.KEY_D, Input.KEY_S };

		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

		String[] objectActions = action.split("[;]");
		for (String subAction : objectActions) { 
			String[] tokens = subAction.split("[ ]");
			String name = tokens[0];
			String code = tokens[1];
			
			PhysicsObject obj = objectSpace.getPhysicsObject(name);
			for (int i = 0; i < code.length(); ++i) { 
				Event event = null;
				if (code.charAt(i) == '1')
					event = new Event(EventType.KEY_PRESSED_EVENT);
				else
					event = new Event(EventType.KEY_RELEASED_EVENT);
				
				event.addParameter("key", keys[i]);
				event.addRecipient(obj);
				EventManager.inst().dispatch(event);
				
			}
		}
		
		for (int i = 0; i < 10; ++i) { 
			_gameSystem.update(100);
			if (_container != null)
				_container.render(_gameSystem);
			computeAllRelations();
		}
		if (_container != null)
			_container.render(_gameSystem);
	}
	
	/**
	 * For the time being ignored.
	 * TODO: remove if not necessary.
	 * @param objectStates
	 * @return
	 */
	public OOMDPState computeRelations(List<OOMDPObjectState> objectStates) {
		return null;
	}
}

enum ClassType { 
	agent {
		@Override
		public Element convert(OOMDPObjectState obj) {
			Document document = DocumentHelper.createDocument();
	        Element element = document.addElement( "physicsObject" )
	        	.addAttribute("name", obj.getName())
	        	.addAttribute("renderPriority", "2")
	        	.addAttribute("type", "cognitiveAgent")
	        	.addAttribute("hasMass", "true")
	        	.addAttribute("initAgent", "initAgent");

	        body(element, obj);
	        OOMDPObjectShape.valueOf(obj.getValue("shape-type")).convert(element, obj);
	        
	        Element components = element.addElement("components");
	        
	        Element sv = components.addElement("component")
	        	.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.ShapeVisual")
	        	.addAttribute("fromPhysics", "true");
	        sv.addElement("renderPriority").addAttribute("value", "99");
	        sv.addElement("color")
	        	.addAttribute("r", "1.0")
	        	.addAttribute("g", "0.0")
	        	.addAttribute("b", "0.0")
	        	.addAttribute("a", "1.0");

	        Element sp = components.addElement("component")
        		.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.SpriteVisual");
	        sp.addElement("renderPriority").addAttribute("value", "100");
	        sp.addElement("image")
        		.addAttribute("name", "data/images/half-circle.png")
        		.addAttribute("scale", "0.0165");
	        
	        components.addElement("component")
	        	.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.steering.BehaviorControl");
	        components.addElement("component")
	        	.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.PerceptionComponent");
	        components.addElement("component")
        		.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.TopDownControl")
        		.addAttribute("individual", "true");
//	        components.addElement("component")
//	        	.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.InternalComponent");

	        return element;
		}
		
	},
	dynamic {
		@Override
		public Element convert(OOMDPObjectState obj) {
			Document document = DocumentHelper.createDocument();
	        Element element = document.addElement( "physicsObject" )
	        	.addAttribute("name", obj.getName())
	        	.addAttribute("renderPriority", "100")
	        	.addAttribute("type", "dynamic")
	        	.addAttribute("hasMass", "true");

	        body(element, obj);
	        OOMDPObjectShape.valueOf(obj.getValue("shape-type")).convert(element, obj);
	        
	        element.addElement("components");
	        return element;
		}
	},
	obstacle {
		@Override
		public Element convert(OOMDPObjectState obj) {
			Document document = DocumentHelper.createDocument();
	        Element element = document.addElement( "physicsObject" )
	        	.addAttribute("name", obj.getName())
	        	.addAttribute("renderPriority", "100")
	        	.addAttribute("type", "obstacle");

	        body(element, obj);
	        OOMDPObjectShape.valueOf(obj.getValue("shape-type")).convert(element, obj);
	        
	        element.addElement("components");
	        return element;
		}
		
	};
	
	public abstract Element convert(OOMDPObjectState obj);
	
	/**
	 * Extract the position and add it to a physics
	 * body object.  
	 * @param element
	 * @param obj
	 */
	private static void body(Element element, OOMDPObjectState obj) { 
        element.addElement("bodyDef")
    		.addAttribute("x", obj.getValue("x"))
    		.addAttribute("y", obj.getValue("y"))
    		.addAttribute("angle", obj.getValue("angle"));
	}
}

enum OOMDPObjectShape { 
	
	circle {
		@Override
		public void convert(Element element, OOMDPObjectState obj) {
	        Element shape = element.addElement("shapeDef")
	    		.addAttribute("type", "circle")
	    		.addAttribute("radius", obj.getValue("radius"));
	        
	        addPhysicsProperties(shape, obj);
		}
	}, 
	rectangle {
		@Override
		public void convert(Element element, OOMDPObjectState obj) {
			int width = Integer.parseInt(obj.getValue("width"));
			int height = Integer.parseInt(obj.getValue("height"));
	        Element shape = element.addElement("shapeDef")
    			.addAttribute("type", "polygon");
	        
	        shape.addElement("vertex")
	        	.addAttribute("x", -(width/2) + "")
	        	.addAttribute("y", -(height/2) + "");
	        shape.addElement("vertex")
        		.addAttribute("x", -(width/2) + "")
        		.addAttribute("y", (height/2) + "");
	        shape.addElement("vertex")
        		.addAttribute("x", (width/2) + "")
        		.addAttribute("y", (height/2) + "");
	        shape.addElement("vertex")
        		.addAttribute("x", (width/2) + "")
        		.addAttribute("y", -(height/2) + "");
	        
	        addPhysicsProperties(shape, obj);
		}
	}; 
	
	public abstract void convert(Element element, OOMDPObjectState obj);

	/**
	 * Some additional physics properties that are important regardless of
	 * shape.
	 * @param element
	 * @param obj
	 */
	private static void addPhysicsProperties(Element element, OOMDPObjectState obj) { 
		element.addAttribute("density", "0.2");
		element.addAttribute("friction", "0.5");
		element.addAttribute("restitution", "0.25");
	}
}
