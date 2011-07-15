package edu.arizona.simulator.ww2d.object.component.trigger;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.system.CollisionEvent;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.component.Component;
import edu.arizona.simulator.ww2d.system.EventManager;

public abstract class Trigger extends Component {

	public Trigger(GameObject owner) {
		super(owner);
		
		addListeners();
	}
	
	public void addListeners() { 
		EventManager.inst().register(CollisionEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				CollisionEvent event = (CollisionEvent) e;
				collision(event);
			} 
		});
	}
	
	protected abstract void collision(CollisionEvent e);
}
