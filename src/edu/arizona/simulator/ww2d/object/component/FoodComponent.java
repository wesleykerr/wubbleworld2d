package edu.arizona.simulator.ww2d.object.component;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.SlickGlobals;
import edu.arizona.simulator.ww2d.utils.enums.EventType;

public class FoodComponent extends Component {
    private static Logger logger = Logger.getLogger( FoodComponent.class );

	public FoodComponent(GameObject obj) { 
		super(obj);
	}
	
	public void init(float store) { 
//		logger.debug("Setting the store value : " + store);
		_parent.setUserData("store", store);
		EventManager.inst().register(EventType.REQUEST_EAT, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				float store = _parent.getUserData("store", Float.class);
//				logger.debug("received food request: " + store);
				if (store > 0) { 
					PhysicsObject target = (PhysicsObject) e.getValue("requestor");
					dispatchEvents(target);
					
					// decrease the amount of food in the store					
					_parent.setUserData("store", store-1);
				}
			} 
		});
	}

	/**
	 * Dispatch power and health events to the agent
	 * who has requested to eat.
	 * @param target
	 */
	private void dispatchEvents(PhysicsObject target) { 
		Event e = new Event(EventType.ENERGY_EVENT);
		e.addRecipient(target);
		e.addParameter("amount", 2f);
		EventManager.inst().dispatch(e);
	}
	
	@Override
	public void update(int elapsed) {
		// go ahead an have some decay on our plant life
		float store = _parent.getUserData("store", Float.class);
		store -= (float) elapsed / 1000f;
		
		// save the new value.
		_parent.setUserData("store", store);
		if (store <= 0) { 
			Event e = new Event(EventType.REMOVE_OBJECT_EVENT);
			e.addParameter("object", _parent);
			EventManager.inst().dispatch(e);
		}
	}
	
	@Override 
	public void render(Graphics g) { 
		int store = (int) _parent.getUserData("store", Float.class).floatValue();
		SlickGlobals.textFont.drawString(_parent.getPosition().x-5, _parent.getPosition().y-5, store+"", Color.black);
	}
	
	@Override
	public void fromXML(Element e) {
		init(Float.parseFloat(e.attributeValue("store")));
	}
}
