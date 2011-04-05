package edu.arizona.simulator.ww2d.events.spawn;

import org.dom4j.Element;
import org.jbox2d.dynamics.World;

import edu.arizona.simulator.ww2d.events.Event;

public class CreatePhysicsObject extends Event {

	private World _world;
	private Element _element;
	
	/**
	 * CreateGameObject event consisting of an XML element
	 * @param element
	 */
	public CreatePhysicsObject(World world, Element element) { 
		super();
		
		_world = world;
		_element = element;
	}
	
	/**
	 * Return the XML element that is specifies
	 * how to create the GameObject
	 * @return
	 */
	public Element getElement() { 
		return _element;
	}
	
	/**
	 * Get the physics world that we will be adding this
	 * physics object to.
	 * @return
	 */
	public World getWorld() { 
		return _world;
	}
}
