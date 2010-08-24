package edu.arizona.simulator.ww2d.object.component.trigger;

import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.component.Component;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.enums.EventType;

public abstract class Trigger extends Component {

	public Trigger(GameObject owner) {
		super(owner);
		
		addListeners();
	}
	
	public void addListeners() { 
		EventManager.inst().register(EventType.COLLISION_EVENT, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				collision(e);
			} 
		});
	}
	
	protected abstract void collision(Event e);
}
