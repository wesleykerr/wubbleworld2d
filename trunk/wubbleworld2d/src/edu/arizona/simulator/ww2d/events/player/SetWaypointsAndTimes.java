package edu.arizona.simulator.ww2d.events.player;

import java.util.List;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class SetWaypointsAndTimes extends Event {
	private List<Vec2> _waypoints;
	private List<Integer> _times;
	
	public SetWaypointsAndTimes(List<Vec2> waypoints, List<Integer> times) { 
		_waypoints = waypoints;
		_times = times;
	}
	
	public SetWaypointsAndTimes(List<Vec2> waypoints, List<Integer> times, GameObject... objects) { 
		super(objects);
		
		_waypoints = waypoints;
		_times = times;
	}
	
	public List<Vec2> getWaypoints() { 
		return _waypoints;
	}

	public List<Integer> getTimes() { 
		return _times;
	}
}
