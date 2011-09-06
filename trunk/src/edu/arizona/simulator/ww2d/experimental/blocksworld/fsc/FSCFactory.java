package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.ContactCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.IntervalCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.MoveAction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.TimeCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.ZeroVelocityCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.AccelerateFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.CollisionReboundFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.FractionalDecelerationFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.IntervalChangeFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.IntervalReboundFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.MaintainDataFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.SimpleReboundFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.systems.FSCSubsystem;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class FSCFactory {

	private static HashMap<String, Element> blueprints = new HashMap<String, Element>();

	public static void registerBlueprint(Element blueprint) {
		blueprints.put(blueprint.attributeValue("name"), blueprint);
	}

	public static void buildAndAttach(PhysicsObject obj, String fsc,
			FSCSubsystem subsystem) {
		Element blueprint = blueprints.get(fsc);
		if (blueprint == null) {
			return;
		}
		List<Element> states = blueprint.elements("state");
		if (states == null || states.isEmpty()) {
			return;
		}
		List<Element> transitions = blueprint.elements("transition");

		subsystem.put(obj, parseState(obj, states, transitions));
	}

	private static FSCState parseState(PhysicsObject object,
			List<Element> states, List<Element> transitions) {
		FSCState[] stateArray = new FSCState[states.size()];

		for (int j = 0; j < states.size(); j++) {
			stateArray[j] = new FSCState(object);
			addActions(stateArray[j], states.get(j));
			if (states.get(j).elements("state") != null
					&& !states.get(j).elements("state").isEmpty()) {
				stateArray[j].setSubstate(parseState(object, states.get(j)
						.elements("state"), states.get(j)
						.elements("transition")));
			}
		}

		// List<Element> transitions = fsc.elements("transition");
		if (transitions != null) {
			for (int j = 0; j < transitions.size(); j++) {
				Element transition = transitions.get(j);
				FSCTransition trans = new FSCTransition(object);
				stateArray[Integer.parseInt(transition.attributeValue("from"))]
						.addTransition(trans);
				trans.setNextState(stateArray[Integer.parseInt(transition
						.attributeValue("to"))]);

				addComponents(trans, transition);
			}
		}
		return stateArray[0];
	}

	private static void addComponents(FSCTransition trans, Element root) {
		List<Element> actions = root.elements("action");
		if (actions != null) {
			for (int i = 0; i < actions.size(); i++) {
				Element action = actions.get(i);
				trans.addAction(parseAction(trans.getOwner(), action));
			}
		}

		actions = root.elements("check");
		if (actions != null) {
			for (int i = 0; i < actions.size(); i++) {
				Element action = actions.get(i);
				trans.addCheck(parseCheck(trans.getOwner(), action));
			}
		}

		actions = root.elements("func");
		if (actions != null) {
			for (int i = 0; i < actions.size(); i++) {
				Element action = actions.get(i);
				trans.addFunction(parseFunction(trans.getOwner(), action));
			}
		}
	}

	private static Action parseAction(PhysicsObject owner, Element action) {
		String name = action.attributeValue("name");
		Action toReturn = null;
		if (name.equals("MoveAction")) {
			// float dx = Float.parseFloat(action.attributeValue("dx"));
			// float dy = Float.parseFloat(action.attributeValue("dy"));
			// float ax = Float.parseFloat(action.attributeValue("ax"));
			// float ay = Float.parseFloat(action.attributeValue("ay"));
			toReturn = new MoveAction(owner);
		}

		if (toReturn != null) {
			List<Element> funcs = action.elements("func");
			for (Element func : funcs) {
				Function f = parseFunction(owner, func);
				if (f != null) {
					toReturn.addFunction(f);
				}
			}
		}
		return toReturn;
	}

	private static Check parseCheck(PhysicsObject owner, Element check) {
		String name = check.attributeValue("name");
		if (name.equals("ContactCheck")) {
			return new ContactCheck(owner);
		} else if (name.equals("TimeCheck")) {
			int interval;
			if (check.attributeValue("interval") == null) {
				interval = -1;
			} else {
				interval = Integer.parseInt(check.attributeValue("interval"));
			}
			return new TimeCheck(owner, interval);
		} else if (name.equals("IntervalCheck")) {
			return new IntervalCheck(owner);
		} else if (name.equals("ZeroVelocityCheck")) {
			return new ZeroVelocityCheck(owner);
		}
		return null;
	}

	private static void addActions(FSCState state, Element root) {
		List<Element> actions = root.elements("action");
		if (actions != null) {
			for (int i = 0; i < actions.size(); i++) {
				Element action = actions.get(i);
				Action a = parseAction(state.getOwner(), action);
				state.addAction(a);
			}
		}
	}

	private static Function parseFunction(PhysicsObject owner, Element function) {
		String name = function.attributeValue("name");
		if (name.equals("AccelerateFunction")) {
			float ax = Float.parseFloat(function.attributeValue("ax"));
			float ay = Float.parseFloat(function.attributeValue("ay"));
			return new AccelerateFunction(owner, ax, ay);
		} else if (name.equals("IntervalChangeFunction")) {
			int dt = Integer.parseInt(function.attributeValue("dt"));
			return new IntervalChangeFunction(owner, dt);
		} else if (name.equals("IntervalReboundFunction")) {
			return new IntervalReboundFunction(owner);
		} else if (name.equals("MaintainDataFunction")) {
			List<Element> fields = function.elements("field");
			LinkedList<String> retainers = new LinkedList<String>();
			for (Element e : fields) {
				retainers.add(e.attributeValue("value"));
			}
			return new MaintainDataFunction(owner, retainers);
		} else if (name.equals("SimpleReboundFunction")) {
			return new SimpleReboundFunction(owner);
		} else if (name.equals("CollisionReboundFunction")) {
			return new CollisionReboundFunction(owner);
		} else if (name.equals("FractionalDecelerationFunction")) {
			float dxScale = Float
					.parseFloat(function.attributeValue("dxScale"));
			float dyScale = Float
					.parseFloat(function.attributeValue("dyScale"));
			return new FractionalDecelerationFunction(owner, dxScale, dyScale);
		}
		return null;
	}
}
