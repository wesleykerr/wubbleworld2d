package edu.arizona.simulator.ww2d.experimental.blocksworld.loaders;

import java.net.URL;
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
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.ContactCheck;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.MoveAction;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.actions.TimeCheck;
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
					FSCState[] stateArray = new FSCState[states.size()];
					PhysicsObject object = objSpace.getPhysicsObject(obj
							.attributeValue("name"));
					for (int j = 0; j < states.size(); j++) {
						stateArray[j] = new FSCState(object);
						addActions(stateArray[j], states.get(j));
					}

					List<Element> transitions = fsc.elements("transition");
					if (transitions != null) {
						for (int j = 0; j < transitions.size(); j++) {
							Element transition = transitions.get(j);
							FSCTransition trans = new FSCTransition(object);
							stateArray[Integer.parseInt(transition
									.attributeValue("from"))]
									.addTransition(trans);
							trans.setNextState(stateArray[Integer
									.parseInt(transition.attributeValue("to"))]);

							addActions(trans, transition);
						}
					}
					fscSubsystem.put(object, stateArray[0]);
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private void addActions(FSCTransition trans, Element root) {
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
	}

	private Action parseAction(PhysicsObject owner, Element action) {
		String name = action.attributeValue("name");
		Action toReturn = null;
		if (name.equals("MoveAction")) {
			float dx = Float.parseFloat(action.attributeValue("dx"));
			float dy = Float.parseFloat(action.attributeValue("dy"));
			float ax = Float.parseFloat(action.attributeValue("ax"));
			float ay = Float.parseFloat(action.attributeValue("ay"));
			toReturn = new MoveAction(owner, dx, dy, ax, ay);
		}
		
		if(toReturn != null){
			List<Element> checks = action.elements("check");
			for(Element check : checks){
				Check c = parseCheck(owner,check);
				if(c != null){
					toReturn.addCheck(c);
				}
			}
		}
		return toReturn;
	}

	private Check parseCheck(PhysicsObject owner, Element check) {
		String name = check.attributeValue("name");
		if (name.equals("ContactCheck")) {
			return new ContactCheck(owner);
		} else if(name.equals("TimeCheck")){
			int interval = Integer.parseInt(check.attributeValue("interval"));
			return new TimeCheck(owner,interval);
		}
		return null;
	}

	private void addActions(FSCState state, Element root) {
		List<Element> actions = root.elements("action");
		if (actions != null) {
			for (int i = 0; i < actions.size(); i++) {
				Element action = actions.get(i);
				state.addAction(parseAction(state.getOwner(), action));
			}
		}
	}
}
