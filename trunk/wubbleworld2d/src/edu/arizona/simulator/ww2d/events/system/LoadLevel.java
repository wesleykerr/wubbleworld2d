package edu.arizona.simulator.ww2d.events.system;

import org.jbox2d.dynamics.World;

import edu.arizona.simulator.ww2d.events.Event;

public class LoadLevel extends Event {

	private String _levelName;
	private World _world;
	private boolean _isStart;
	
	public LoadLevel(String levelName, World world, boolean isStart) { 
		_levelName = levelName;
		_world = world;
		_isStart = isStart;
	}
	
	public String getLevelName() { 
		return _levelName;
	}
	
	public World getWorld() { 
		return _world;
	}
	
	public boolean isStart() { 
		return _isStart;
	}
}
