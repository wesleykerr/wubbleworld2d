package edu.arizona.simulator.ww2d.external;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.AuditoryEntry;
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
import edu.arizona.verbs.shared.Environment;
import edu.arizona.verbs.shared.OOMDPObjectState;
import edu.arizona.verbs.shared.OOMDPState;
import edu.arizona.verbs.shared.Relation;

public class WW2DEnvironment implements Environment {

	private VerbGameContainer _container;
	private GameSystem _gameSystem;
	
	private NumberFormat _format;

	public WW2DEnvironment(boolean visualize) { 
		_format = NumberFormat.getInstance();
		_format.setMinimumFractionDigits(2);
		_format.setMaximumFractionDigits(2);

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
	}
	
	public void cleanup() { 
		_gameSystem.finish();
		_container.destroy();
	}
	
	// MDPObjectState -- This corresponds to the objects and their attributes
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

//	private OOMDPState trueState = null;
	private List<State> worldState = null;
	private boolean needsReset = false;
	
	@Override
	public OOMDPState initializeEnvironment(List<OOMDPObjectState> state) {
		_gameSystem = new GameSystem(800, 800, true);		
		_gameSystem.addSubsystem(GameSystem.Systems.PhysicsSubystem, new PhysicsSubsystem());
		_gameSystem.loadLevel("data/levels/Room-External.xml", null, null);

		for (OOMDPObjectState obj : state) { 
			ClassType type = ClassType.valueOf(obj.getClassName());
			Element e = type.convert(obj);

//			System.out.println("ELEMENT:\n" + e.asXML());
			
			Event event = new Event(EventType.CREATE_PHYSICS_OBJECT);
			event.addParameter("element", e);
			EventManager.inst().dispatchImmediate(event);
		}

		// Do an initial update to get the world turning....
		_gameSystem.update(0);
		
		if (_container != null)
			_container.render(_gameSystem);
		
		List<Relation> specialRelations = computeSpecialRelations();
		
		worldState = getGroundState();
		
		return makeMdpState(worldState, null, specialRelations);
	}

	@Override
	public OOMDPState performAction(String action) {
		reset();
		
		List<State> lastWorldState = worldState;
		
		List<Relation> specialRelations = go(action, true);
		
		worldState = getGroundState();
		
		return makeMdpState(worldState, lastWorldState, specialRelations); 
	}

	/**
	 * simulateAction first saves off the current state and thn performs
	 * the action, records the next state and resets the state to
	 * the original.
	 */
	public OOMDPState simulateAction(OOMDPState state, String action) {
		// Set the current state to be state
		setState(state);
		_gameSystem.update(0);

		List<State> prev = getGroundState();
		
		List<Relation> specialRelations = go(action, false); 

		OOMDPState next = makeMdpState(getGroundState(), prev, specialRelations); 
		
		setState(worldState);
		_gameSystem.update(0);
		
		return next;
	}
	
	/**
	 * Experimental: This version of simulate simulates from the current ground state. 
	 * It is a sort of hybrid between simulate and perform.
	 */
	public OOMDPState simulateAction(String action) {
		List<Relation> specialRelations = go(action, false);
		
		OOMDPState next = makeMdpState(getGroundState(), worldState, specialRelations);
		
		needsReset = true;
		
		return next;
	}
	
	public void reset() {
		if (needsReset) {
			setState(worldState);
			_gameSystem.update(0);
			needsReset = false;
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
	private List<Relation> go(String action, boolean render) { 
		parseAction(action);
		
		List<Relation> specialRelations = new ArrayList<Relation>();
		
		for (int i = 0; i < 10; ++i) { 
			_gameSystem.update(15);
			if (render && _container != null)
				_container.render(_gameSystem);
			resolveRelations(specialRelations, computeSpecialRelations());
		}
		if (render && _container != null)
			_container.render(_gameSystem);
		
		EventType[] types = new EventType[] { 
				EventType.FORWARD_EVENT, EventType.LEFT_EVENT, 
				EventType.RIGHT_EVENT, EventType.BACKWARD_EVENT 
		};

		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		for (PhysicsObject obj : objectSpace.getCognitiveAgents()) { 
			for (EventType evt : types) { 
				Event event = new Event(evt);
				event.addRecipient(obj);
				event.addParameter("state", false);
				EventManager.inst().dispatchImmediate(event);
			}
		}
		
		return specialRelations;
	}
	
	// This assumes the special relations are always computed in the same order, which they are now
	private void resolveRelations(List<Relation> accumulator, List<Relation> current) {
		if (accumulator.isEmpty()) {
			accumulator.addAll(current);
		} else {
			for (int i = 0; i < accumulator.size(); i++) {
				accumulator.get(i).value = (accumulator.get(i).value || current.get(i).value);
			}
		}
	}
	
	/**
	 * Parse the action that will send messages so that
	 * stuff will move when the external environment asks.
	 */
	private void parseAction(String action) {
		EventType[] types = new EventType[] { 
				EventType.FORWARD_EVENT, EventType.LEFT_EVENT, 
				EventType.RIGHT_EVENT, EventType.BACKWARD_EVENT 
		};

		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

		String[] objectActions = action.split("[;]");
		for (String subAction : objectActions) { 
			String[] tokens = subAction.split("[ ]");
			String name = tokens[0];
			String code = tokens[1];
			
			PhysicsObject obj = objectSpace.getPhysicsObject(name);
			for (int i = 0; i < code.length(); ++i) { 
				Event event = new Event(types[i]);
				event.addRecipient(obj);
				event.addParameter("state", code.charAt(i) == '1');
				EventManager.inst().dispatch(event);
			}
		}
	}
	
	/**
	 * Set the world to match the given state
	 * @param state
	 */
	// TODO: Maybe remove this and always go through State first, for clarity
	private void setState(OOMDPState state) {
		ArrayList<State> states = new ArrayList<State>();
		
		for (OOMDPObjectState objState : state.getObjectStates()) {
			states.add(new State(objState));
		}
		
		setState(states);
		
//		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
//
//		for (OOMDPObjectState objState : state.getObjectStates()) { 
//			PhysicsObject obj = objectSpace.getPhysicsObject(objState.getName());
//			Body body = obj.getBody();
//			
//			State s = new State(objState);
//			
//			body.setLinearVelocity(new Vec2(s.vx, s.vy));
//			body.setAngularVelocity(s.vtheta);
//			body.setXForm(new Vec2(s.x, s.y), s.heading);
//			body.m_force.setZero();
//			body.m_torque = 0;
//		}
	}

	private void setState(List<State> states) {
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

		for (State s : states) { 
			PhysicsObject obj = objectSpace.getPhysicsObject(s.name);
			Body body = obj.getBody();
			body.setLinearVelocity(new Vec2(s.vx, s.vy));
			body.setAngularVelocity(s.vtheta);
			body.setXForm(new Vec2(s.x, s.y), s.heading);
			body.m_force.setZero();
			body.m_torque = 0;
		}
	}
	
	/**
	 * Iterate over the objects in the world generating the new state and determine
	 * all of the Relations that exist.
	 * @return
	 */
//	private OOMDPState getState(List<OOMDPObjectState> objectState, List<Relation> specialRelations) {
//		List<OOMDPObjectState> objectStates = getObjectStates(objectState);
//		List<Relation> relations = computeRelations2(objectStates);
//		relations.addAll(specialRelations);
//		return new OOMDPState(objectStates, relations);
//	}

	private OOMDPState makeMdpState(List<State> groundStates, List<State> previousStates, List<Relation> specialRelations) {
		List<OOMDPObjectState> objectStates = makeObjectStates(groundStates);
		
		List<Relation> relations = computeGroundRelations(groundStates, previousStates);
		relations.addAll(specialRelations);
		
		return new OOMDPState(objectStates, relations);
	}
	
	// NEW
	private List<State> getGroundState() {
		ArrayList<State> result = new ArrayList<State>();
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		for (PhysicsObject obj : objectSpace.getPhysicsObjects()) { 
			if (obj.getType() == ObjectType.wall)
				continue;
			
			State s = new State(obj);
			result.add(s);
		}
		
		return result;
	}
	
	private List<OOMDPObjectState> makeObjectStates(List<State> groundStates) {
		ArrayList<OOMDPObjectState> mdpStates = new ArrayList<OOMDPObjectState>();
		
		for (State s : groundStates) {
			mdpStates.add(s.discretize());
		}
		
		return mdpStates;
	}
	
//	private HashMap<String, OOMDPObjectState> makeStateMap(List<OOMDPObjectState> states) {
//		HashMap<String, OOMDPObjectState> stateMap = new HashMap<String, OOMDPObjectState>();
//
//		if (states == null) {
//			return stateMap;
//		}
//		
//		for (OOMDPObjectState os : states) {
//			stateMap.put(os.getName(), os);
//		}
//		
//		return stateMap;
//	}
	
//	private List<OOMDPObjectState> getObjectStates(List<OOMDPObjectState> previous) {
//		HashMap<String,OOMDPObjectState> stateMap = makeStateMap(previous);
//		
//		// First generate all of the attribute values.
//		List<OOMDPObjectState> objectStates = new ArrayList<OOMDPObjectState>();
//		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
//		for (PhysicsObject obj : objectSpace.getPhysicsObjects()) { 
//			if (obj.getType() == ObjectType.wall)
//				continue;
//			
//			State s = new State(obj);
//			OOMDPObjectState state = s.discretize();
//
//			// Do we want this anymore?
//			if (stateMap.containsKey(obj.getName())) {
//				OOMDPObjectState prevState = stateMap.get(obj.getName());
//				state.setAttribute("last_x", prevState.getValue("x"));
//				state.setAttribute("last_y", prevState.getValue("y"));
//				state.setAttribute("last_heading", prevState.getValue("heading"));
//			} else {
//				state.setAttribute("last_x", state.getValue("x"));
//				state.setAttribute("last_y", state.getValue("y"));
//				state.setAttribute("last_heading", state.getValue("heading"));
//			}
//			
//			objectStates.add(state);
//		}
//		
//		return objectStates;
//	}
	
	private List<Relation> computeGroundRelations(List<State> currentStates, List<State> previousStates) {
		List<Relation> list = new ArrayList<Relation>();
		
//		for (State state : currentStates) {
//			// First compute the predicates for the object
//			list.addAll(computePredicates(state));
//			
//			// Then compute all the binary relations
//			for (State other : currentStates) {
//				if (!state.name.equals(other.name)) { // No need to compare with self
//					list.addAll(computeBinaryRelations(state, other));
//				}
//			}
//		}

		HashMap<String, State> currentMap = makeStateMap(currentStates);
		
		if (previousStates == null) { // Only compute instantaneous relations
			for (String name : currentMap.keySet()) {
				// There are no inst. predicates right now
				
				// Compute the binary relations
				for (State other : currentStates) {
					if (!name.equals(other.name)) { // No need to compare with self
						list.addAll(computeInstBinaryRelations(currentMap.get(name), currentMap.get(other.name))); 
					}
				}
			}

		} else { // Compute both inst. and differential predicates
			
			HashMap<String, State> prevMap = makeStateMap(previousStates);
			
			for (String name : currentMap.keySet()) {
				// Compute the predicates
				list.addAll(computeDiffPredicates(currentMap.get(name), prevMap.get(name)));
				
				// Compute the binary relations
				for (State other : currentStates) {
					if (!name.equals(other.name)) { // No need to compare with self
						list.addAll(computeInstBinaryRelations(currentMap.get(name), currentMap.get(other.name)));
						list.addAll(computeDiffBinaryRelations(currentMap.get(name), prevMap.get(name), 
														   currentMap.get(other.name), prevMap.get(other.name))); 
					}
				}
			}
		}
		
		return list;
	}
	
	private HashMap<String, State> makeStateMap(List<State> states) {
		LinkedHashMap<String, State> map = new LinkedHashMap<String, State>();
		
		for (State s : states) {
			map.put(s.name, s);
		}
		
		return map;
	}
	
	private List<Relation> computeDiffPredicates(State current, State previous) {
		List<Relation> list = new ArrayList<Relation>();
		
		String[] names = new String[] { current.name };
		list.add(new Relation("Moved", names, (!(current.x == previous.x) ||
											   !(current.y == previous.y))));
		
		// TODO: Can we put these back?
//		float deltaX = current.x - previous.x;
//		float deltaY = current.y - previous.y;
//		
//		double magnitude = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
//		double direction = Math.atan2(deltaY, deltaX);
//		
//		double deltaAngle = State.deltaAngle(direction, current.heading);
//		
//		double deadZone = 0.05;
//		
//		if (Math.abs(magnitude) < deadZone) {
//			list.add(new Relation("MovedForward", names, false));
//			list.add(new Relation("MovedBackward", names, false));
//		} else {
//			list.add(new Relation("MovedForward", names, (-(Math.PI/2) < deltaAngle && deltaAngle < (Math.PI/2))));
//			list.add(new Relation("MovedBackward", names, (deltaAngle < -(Math.PI/2) || (Math.PI/2) < deltaAngle)));
//		}
		
		list.add(new Relation("Rotated", names, (current.heading != previous.heading)));
		list.add(new Relation("RotatedLeft", names, (current.vtheta < 0.0)));
		list.add(new Relation("RotatedRight", names, (current.vtheta > 0.0)));
		
		return list;
	}
	
	private List<Relation> computeInstBinaryRelations(State self, State other) {
		List<Relation> list = new ArrayList<Relation>();
		
		String[] names = new String[] { self.name, other.name };
		String[] symNames = new String[] { other.name, self.name };
		
		float dist = self.computeDistanceTo(other);
		
		list.add(new Relation("Near", names, (dist < 2.0)));
		list.add(new Relation("Near", symNames, (dist < 2.0)));
		
		if (self.className.equals("agent")) {
			String rels[] = new String[] { "RightOf", "LeftOf", "InFrontOf", "Behind" };
			int true_index = -1;

			float relAngle = self.computeRelativeAngle(other);

			
			true_index = 0;
			if (relAngle < 0) {
				true_index = 1;
				relAngle = Math.abs(relAngle);
			}

			if (relAngle < (Math.PI / 4) - 0.1) {
				true_index = 2;
			} else if (relAngle > (3 * Math.PI / 4) + 0.1) {
				true_index = 3;
			}

//			System.out.println("REL ANGLE: " + relAngle + " " + rels[true_index]);

			for (int i = 0; i < 4; i++) {
				list.add(new Relation(rels[i], names, (true_index == i)));
			}
		}
		
		return list;
	}
	
	private List<Relation> computeDiffBinaryRelations(State currentSelf, State formerSelf, State currentOther, State formerOther) {
		List<Relation> list = new ArrayList<Relation>();
		
		String[] names = new String[] { currentSelf.name, currentOther.name };
		String[] symNames = new String[] { currentOther.name, currentSelf.name };
 		
		float dist = currentSelf.computeDistanceTo(currentOther);
		float lastDist = formerSelf.computeDistanceTo(formerOther);
		
		boolean dd = (dist < lastDist);
		boolean di = (dist > lastDist);
		boolean dc = (dist == lastDist);
		
		list.add(new Relation("DistanceDecreased", names, dd));
		list.add(new Relation("DistanceDecreased", symNames, dd));
		
		list.add(new Relation("DistanceIncreased", names, di));
		list.add(new Relation("DistanceIncreased", symNames, di));
		
		list.add(new Relation("DistanceConstant", names, dc));
		list.add(new Relation("DistanceConstant", symNames, dc));
		
		return list;
	}
	
	/**
	 * Iterate over all of the objects and build the set of 
	 * relations that are important.  We want all true and
	 * false relations.
	 * @return
	 */
	private List<Relation> computeSpecialRelations() { 
		List<Relation> list = new ArrayList<Relation>();
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		List<PhysicsObject> objects = new ArrayList<PhysicsObject>(objectSpace.getPhysicsObjects());

		// First add all of the propositions that involve a single object.
		for (int i = 0; i < objects.size(); ++i) { 
			PhysicsObject obj = objects.get(i);
			if (obj.getType() == ObjectType.wall)
				continue;
			
//			String[] objArray = new String[] { obj.getName() };
//			
//			boolean moving = false;
//			if (obj.getBody().getLinearVelocity().lengthSquared() > 0)  
//				moving = true;
//			list.add(new Relation("Moving", objArray, moving));
			
//			boolean turning = false;
//			boolean turningLeft = false;
//			boolean turningRight = false;
//			float angVel = obj.getBody().getAngularVelocity();
//			if (Float.compare(angVel, 0) != 0)
//				turning = true;
//			if (angVel < 0)
//				turningLeft = true;
//			if (angVel > 0)
//				turningRight = true;
//			
//			list.add(new Relation("Rotating", objArray, turning));
//			list.add(new Relation("RotatingLeft", objArray, turningLeft));
//			list.add(new Relation("RotatingRight", objArray, turningRight));
			
			// Compute all of the pairwise relations.
			for (int j = i+1; j < objects.size(); ++j) { 
				PhysicsObject obj2 = objects.get(j);
				if (obj.getType() == ObjectType.wall)
					continue;				
				
				String[] relArray = new String[] { obj.getName(), obj2.getName() };
				String[] symArray = new String[] { obj2.getName(), obj.getName() };

//				DistanceEntry current = objectSpace.findOrAddDistance(obj, obj2);
//				DistanceEntry previous = objectSpace.getDistance(obj, obj2, 1);

//				boolean dd = false;
//				boolean ds = false;
//				boolean di = false;
//				if (previous != null) { 
//					int value = Float.compare(current.getDistance(), previous.getDistance());
//					System.out.println("Dist between " + relArray[0] + " and " + relArray[1] 
//                                      + " is " + current.getDistance() + " (was " + previous.getDistance() + ")");
//					if (value < 0) dd = true;
//					if (value > 0) di = true;
//					if (value == 0) ds = true;
//				}
//				
//				list.add(new Relation("DistanceDecreasing", relArray, dd));
//				list.add(new Relation("DistanceDecreasing", symArray, dd));
//				
//				list.add(new Relation("DistanceIncreasing", relArray, di));
//				list.add(new Relation("DistanceIncreasing", symArray, di));
//				
//				list.add(new Relation("DistanceStable", relArray, ds));
//				list.add(new Relation("DistanceStable", symArray, ds));
				
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
	        	.addAttribute("renderPriority", "3")
	        	.addAttribute("type", "dynamic")
	        	.addAttribute("hasMass", "true");

	        body(element, obj);
	        OOMDPObjectShape.valueOf(obj.getValue("shape-type")).convert(element, obj);
	   
	        Element components = element.addElement("components");
	        
	        Element sv = components.addElement("component")
        	.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.ShapeVisual")
        	.addAttribute("fromPhysics", "true");
	        sv.addElement("renderPriority").addAttribute("value", "99");
	        sv.addElement("color")
        	.addAttribute("r", "0.0")
        	.addAttribute("g", "0.0")
        	.addAttribute("b", "1.0")
        	.addAttribute("a", "1.0");
	        
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
	        
        	Element components = element.addElement("components");
	        
	        Element sv = components.addElement("component")
        	.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.ShapeVisual")
        	.addAttribute("fromPhysics", "true");
	        sv.addElement("renderPriority").addAttribute("value", "99");
	        sv.addElement("color")
        	.addAttribute("r", "0.0")
        	.addAttribute("g", "1.0")
        	.addAttribute("b", "1.0")
        	.addAttribute("a", "1.0");
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
		
//        element.addElement("bodyDef")
//    		.addAttribute("x", obj.getValue("x"))
//    		.addAttribute("y", obj.getValue("y"))
//    		.addAttribute("angle", obj.getValue("heading"));
		
		// TODO: Is this right?
		State state = new State(obj);
		element.addElement("bodyDef")
			.addAttribute("x", String.valueOf(state.x))
			.addAttribute("y", String.valueOf(state.y))
			.addAttribute("angle", String.valueOf(state.heading));
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
