package edu.arizona.simulator.ww2d.experimental.blocksworld.loaders;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Action;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Check;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.FSCFactory;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.FSCState;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.FSCTransition;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Function;
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
import edu.arizona.simulator.ww2d.level.DefaultLoader;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public class BlocksworldLoader extends DefaultLoader {
	
	private String _fscFile;

	public BlocksworldLoader(String levelFile, String agentsFile, String fscFile,
			Scenario scenario) {
		super(levelFile, agentsFile, scenario);
		this._fscFile = fscFile;
	}

	public void load(GameSystem gameSystem) {
		//Space systemSpace = Blackboard.inst().getSpace("system");

		SAXReader reader = new SAXReader(false);
		
		super.load(gameSystem);
		ObjectSpace objSpace = (ObjectSpace) Blackboard.inst().getSpace(
				"object");
		FSCSubsystem fscSubsystem = (FSCSubsystem) gameSystem
				.getSubsystem(SubsystemType.FSCSubsystem);
		try {
			URL url = this.getClass().getClassLoader().getResource(_fscFile);
			Document doc = reader.read(url);
			Element root = doc.getRootElement();
			
			List<Element> blueprints = root.elements("fsc");
			for(Element blueprint : blueprints){
				FSCFactory.registerBlueprint(blueprint);
			}
			
			url = this.getClass().getClassLoader().getResource(_levelFile);
			doc = reader.read(url);
			root = doc.getRootElement();
			
			
			Element objects = root.element("objects");
			List physicsObjs = objects.elements("physicsObject");
			if (physicsObjs != null) {
				for (int i = 0; i < physicsObjs.size(); ++i) {
//					Element obj = (Element) physicsObjs.get(i);
//					Element fsc = obj.element("fsc");
//					if (fsc == null) {
//						continue;
//					}
//					List<Element> states = fsc.elements("state");
//					if (states == null || states.isEmpty()) {
//						continue;
//					}
//					List<Element> transitions = fsc.elements("transition");
//					PhysicsObject object = objSpace.getPhysicsObject(obj
//							.attributeValue("name"));
//
//					fscSubsystem.put(object,
//							parseState(object, states, transitions));
					Element obj = (Element) physicsObjs.get(i);
					Attribute fsc = obj.attribute("fsc");
					if(fsc == null)
						continue;
					PhysicsObject object = objSpace.getPhysicsObject(obj.attributeValue("name"));
					FSCFactory.buildAndAttach(object, fsc.getValue(),fscSubsystem);
					
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	
}
