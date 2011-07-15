package edu.arizona.simulator.ww2d.object.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.player.BehaviorEvent;
import edu.arizona.simulator.ww2d.events.player.SetWaypoints;
import edu.arizona.simulator.ww2d.events.player.SetWaypointsAndTimes;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Align;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Arrive;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Behavior;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Seek;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class PathComponent extends Component {
    private static Logger logger = Logger.getLogger( PathComponent.class );

	public static float CLOSENESS = 0.2f;
	
	private PhysicsObject _obj;

	private List<Vec2>    _waypoints;
	private List<Integer> _times;

	private int _current;
	private Vec2 _currentWP;
	private int  _currentTime;
	
	private long _startWaiting;;
	
	private boolean _active;
	
	public PathComponent(GameObject obj) { 
		super(obj);
		
		_obj = (PhysicsObject) obj;
		_active = false;

		// Add in the energy updates that come in from eating and what not
		EventManager.inst().register(SetWaypoints.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				setPath(((SetWaypoints) e).getWaypoints());
			} 
		});
		
		EventManager.inst().register(SetWaypointsAndTimes.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) { 
				SetWaypointsAndTimes event = (SetWaypointsAndTimes) e;
				setPath(event.getWaypoints(), event.getTimes());
			}
		});
	}
	
	/**
	 * Set the path that we must follow.
	 * @param waypoints
	 * @param times
	 */
	public void setPath(List<Vec2> waypoints, List<Integer> times) { 
		_waypoints = waypoints;
		_times = times;
		
		_current = 0;
		_currentWP = null;
		_startWaiting = -1;
		_active = true;
	}
	
	/**
	 * Set the path that we must follow.
	 * @param waypoints
	 */
	public void setPath(List<Vec2> waypoints) { 
		_waypoints = waypoints;
		_times = new ArrayList<Integer>();
		for (int i = 0; i < waypoints.size(); ++i)  
			_times.add(0);

		_current = 0;
		_currentWP = null;
		_startWaiting = -1;
		_active = true;
	}
	
	@Override
	public void update(int elapsed) {
		if (!_active) {
			return;
		}

		// Have we reached the end of our path?
		if (shouldWeStop()) {
			_active = false;
			return;
		}
		
		// have we dispatched an order?
		if (_currentWP == null) { 
			dispatch();
		} else { 
			// Check to see if we are already waiting and if the time has elapsed
			// so that we can move on.
			if (_startWaiting != -1 && System.currentTimeMillis() > _startWaiting + _currentTime) { 
				_currentWP = null;
				_startWaiting = -1;
				return;
			} else if (_startWaiting != -1) { 
				return;
			}
			
			float distance = _currentWP.sub(_obj.getPPosition()).length();
			if (distance < CLOSENESS) {
				// we are close enough to determine what we are going to 
				// do.  If _currentTime > 0 then we startWaiting and if
				// not then we move on to the next waypoint.
				if (_currentTime > 0) 
					_startWaiting = System.currentTimeMillis();
				else  
					_currentWP = null;
			}
		}
	}
	
	private void dispatch() { 
		_currentWP = _waypoints.get(_current);
		_currentTime = _times.get(_current);
		_startWaiting = -1;
		
		_current += 1;
		
		// Dispatch the events....
		Class<? extends Behavior> bClass = null;
		if (_current >= _waypoints.size()-1 || _currentTime > 0)
			bClass = Arrive.class;
		else
			bClass = Seek.class;
		
		BehaviorEvent e1 = new BehaviorEvent(bClass, true, _parent);
		e1.setTarget(_currentWP);
		EventManager.inst().dispatch(e1);
		
		BehaviorEvent e2 = new BehaviorEvent(Align.class, true, _parent);
		e2.setTarget(_currentWP);
		EventManager.inst().dispatch(e2);
	}
	
    private boolean shouldWeStop() {
    	// Technically, we should turn off all of the behaviors
    	// once we have finished walking the path, but for now
    	// I'm just going to set it to false and move on.
        if (_current < _waypoints.size())
                return false;
        return true;

//        // check to see what our distance is to the waypoint at the end.
//        Vec2 v = _waypoints.get(_waypoints.size()-1);
//        float distance = _obj.getPPosition().sub(v).length();
//        if (distance < 0.02f) {
//                return true;
//        }
//        return false;
    }
	

	@Override
	public void render(Graphics g) { 
		if (!_active)  
			return;
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);

		g.setColor(Color.red);
		for (int i = 0; i < _waypoints.size(); ++i) { 
			Vec2 waypoint = _waypoints.get(i);
			g.fillOval((waypoint.x-0.25f)*scale, (waypoint.y-0.25f)*scale, .5f*scale, .5f*scale);
		}
	}
	
	@Override
	public void fromXML(Element e) {
		// TODO Auto-generated method stub
		if (e.attribute("loop") != null) { 
			// todo add the ability to loop (or patrol)...
		}

		if (e.element("waypoints") != null) { 
			throw new RuntimeException("Revisit and do properly");
//			List<Vec2> waypoints = new ArrayList<Vec2>();
//
//			List waypointElements = e.element("waypoints").elements("waypoint");
//			for (Object obj : waypointElements) { 
//				Element wp = (Element) obj;
//				float x = Float.parseFloat(wp.attributeValue("x"));
//				float y = Float.parseFloat(wp.attributeValue("y"));
//				waypoints.add(new Vec2(x,y));
//			}
//			
//			setPath(waypoints);
		}
	}

}
