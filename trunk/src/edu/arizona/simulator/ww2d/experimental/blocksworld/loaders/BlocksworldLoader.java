package edu.arizona.simulator.ww2d.experimental.blocksworld.loaders;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Action;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Check;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.FSCState;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.FSCTransition;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.ContactCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.IntervalCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.MoveAction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.TimeCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.ZeroVelocityCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.AccelerateFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.IntervalChangeFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.IntervalReboundFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.MaintainDataFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.fieldFunctions.SimpleReboundFunction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.systems.FSCSubsystem;
import edu.arizona.simulator.ww2d.level.DefaultLoader;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public class BlocksworldLoader extends DefaultLoader {

	public BlocksworldLoader(String levelFile, String agentsFile,
			Scenario scenario) {
		super(levelFile, agentsFile, scenario);
	}

	public void load(GameSystem gameSystem) {
		Space systemSpace = Blackboard.inst().getSpace("system");

		SAXReader reader = new SAXReader(false);
		super.load(gameSystem);
		ObjectSpace objSpace = (ObjectSpace) Blackboard.inst().getSpace(
				"object");
		FSCSubsystem fscSubsystem = (FSCSubsystem) gameSystem
				.getSubsystem(SubsystemType.FSCSubsystem);
		try {
			URL url = this.getClass().getClassLoader().getResource(_levelFile);
			Document doc = reader.read(url);
			Element root = doc.getRootElement();

			Element objects = root.element("objects");
			List physicsObjs = objects.elements("physicsObject");
			if (physicsObjs != null) {
				for (int i = 0; i < physicsObjs.size(); ++i) {
					Element obj = (Element) physicsObjs.get(i);
					Element fsc = obj.element("fsc");
					if (fsc == null) {
						continue;
					}
					List<Element> states = fsc.elements("state");
					if (states == null || states.isEmpty()) {
						continue;
					}
					List<Element> transitions = fsc.elements("transition");
					PhysicsObject object = objSpace.getPhysicsObject(obj
							.attributeValue("name"));

					fscSubsystem.put(object,
							parseState(object, states, transitions));
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private FSCState parseState(PhysicsObject object, List<Element> states,
			List<Element> transitions) {
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

	private void addComponents(FSCTransition trans, Element root) {
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

	private Action parseAction(PhysicsObject owner, Element action) {
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

	private Check parseCheck(PhysicsObject owner, Element check) {
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

	private void addActions(FSCState state, Element root) {
		List<Element> actions = root.elements("action");
		if (actions != null) {
			for (int i = 0; i < actions.size(); i++) {
				Element action = actions.get(i);
				Action a = parseAction(state.getOwner(), action);
				state.addAction(a);
			}
		}
	}

	private Function parseFunction(PhysicsObject owner, Element function) {
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
		}
		return null;
	}
}
