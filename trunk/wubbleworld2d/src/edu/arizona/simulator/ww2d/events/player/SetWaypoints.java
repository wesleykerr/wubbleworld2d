package edu.arizona.simulator.ww2d.events.player;

import java.util.List;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;

public class SetWaypoints extends Event {
	private List<Vec2> _waypoints;
	
	public SetWaypoints(List<Vec2> waypoints) { 
		_waypoints = waypoints;
	}
	
	public SetWaypoints(List<Vec2> waypoints, GameObject... objects) { 
		super(objects);
		
		_waypoints = waypoints;
	}
	
	public List<Vec2> getWaypoints() { 
		return _waypoints;
	}
}
