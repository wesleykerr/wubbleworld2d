package edu.arizona.simulator.ww2d.object.component.trigger;

import java.util.Random;

import org.dom4j.Element;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class HealthTrigger extends Trigger {

	private Image _sprite;
	
	private int _timeUntilRespawn;
	private boolean _active;
	
	public HealthTrigger(GameObject owner) { 
		super(owner);
		
		try {
			_sprite = new Image("data/images/health.png");
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_timeUntilRespawn = 0;
		_active = true;
	}
	
	@Override
	public void update(int elapsed) {
		_timeUntilRespawn = Math.max(0, _timeUntilRespawn-elapsed);
		_active = _timeUntilRespawn == 0;
	}
	
	@Override
	public void render(Graphics g) { 
		if (_active)
			_sprite.drawCentered(_parent.getPosition().x, _parent.getPosition().y);
	}
	
	@Override
	protected void collision(Event e) {
		if (!_active)
			return;
		
		ContactPoint cp = (ContactPoint) e.getValue("contact-point");
		GameObject target = (GameObject) cp.shape1.getUserData();
		if (target == null || target == _parent)
			target = (GameObject) cp.shape2.getUserData();
		
		// We do not process collisions between a trigger and a projectile
		if (target.getName().startsWith("projectile"))
			return;
		
//		Event healthEvent = new Event(EventType.HEALTH_EVENT);
//		healthEvent.addRecipient(target);
//		healthEvent.addParameter("amount", 100.0f);
//		EventManager.inst().dispatch(healthEvent);
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		Random r = systemSpace.get(Variable.random).get(Random.class);
		_timeUntilRespawn = 1000 + r.nextInt(5000);
	}

	@Override
	public void fromXML(Element e) {
		// TODO Auto-generated method stub
		
	}
}
