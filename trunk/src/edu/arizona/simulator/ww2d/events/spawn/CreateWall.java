package edu.arizona.simulator.ww2d.events.spawn;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.events.Event;

public class CreateWall extends Event {
	private String _name;
	private Vec2 _position;
	private Vec2 _dimensions;

	public CreateWall(String name, Vec2 pos, Vec2 dim) { 
		_name = name;
		_position = pos;
		_dimensions = dim;
	}
	
	public String getName() {
		return _name;
	}
	
	public Vec2 getPosition() { 
		return _position;
	}
	
	public Vec2 getDimensions() { 
		return _dimensions;
	}
}
