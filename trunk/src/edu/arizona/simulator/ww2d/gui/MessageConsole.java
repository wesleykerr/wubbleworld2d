package edu.arizona.simulator.ww2d.gui;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.HTMLTextAreaModel;
import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class MessageConsole {

	private static Logger logger = Logger.getLogger( MessageConsole.class );

	private Map<String,Console> _consoles;
	
	public MessageConsole(Widget root) { 
		logger.debug("Constructing MessageConsole");
		
		_consoles = new HashMap<String,Console>();
		
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		for (PhysicsObject obj : objectSpace.getCognitiveAgents()) { 
			_consoles.put(obj.getName(), makeTextArea(obj));
			root.add(_consoles.get(obj.getName()).scrollPane);
		}
		addListeners();
	}
	
	/**
	 * Add a listener that registers for Message events.
	 * When an event comes in for the active agent, then we
	 * will add it to the console and continue going.
	 */
	private void addListeners() { 
//		EventManager.inst().registerForAll(EventType.CONSOLE_MESSAGE, new EventListener() {
//			@Override
//			public void onEvent(Event e) {
//				GameObject obj = (GameObject) e.getValue("object");
//				String message = (String) e.getValue("message");
//
//				Console console = _consoles.get(obj.getName());
//				String oldText = console.model.getHtml();
//				console.model.setHtml(oldText + message);
//			} 
//		});
//		
//		EventManager.inst().registerForAll(EventType.CHANGE_CAMERA_FOLLOWING, new EventListener() { 
//			@Override
//			public void onEvent(Event e) {
//				PhysicsObject oldObj = (PhysicsObject) e.getValue("previous-object");
//				PhysicsObject newObj = (PhysicsObject) e.getValue("new-object");
//				
//				logger.debug("Were following: " + oldObj.getName() + " now following: " + newObj.getName());
//				_consoles.get(oldObj.getName()).textArea.setVisible(false);
//				_consoles.get(newObj.getName()).textArea.setVisible(true);
//			} 
//		});
	}
	
	private Console makeTextArea(PhysicsObject obj) {
		Console c = new Console();
		c.model.setHtml(obj.getName() + "<br/>");
		return c;
	}
	
}

class Console { 
	public ScrollPane scrollPane;
	public TextArea textArea;
	public HTMLTextAreaModel model;
	
	public Console() { 
		model = new HTMLTextAreaModel();
		textArea = new TextArea(model);

        scrollPane = new ScrollPane(textArea);
        scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);

		scrollPane.setPosition(0, 0);
		scrollPane.setSize(800, 100);
	}
}
