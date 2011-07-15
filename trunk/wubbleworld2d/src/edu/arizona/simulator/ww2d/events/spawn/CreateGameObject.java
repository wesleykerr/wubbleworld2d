package edu.arizona.simulator.ww2d.events.spawn;

import org.dom4j.Element;

import edu.arizona.simulator.ww2d.events.Event;

public class CreateGameObject extends Event {

	private Element _element;
	
	/**
	 * CreateGameObject event consisting of an XML element
	 * @param element
	 */
	public CreateGameObject(Element element) { 
		super();
		
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
}
