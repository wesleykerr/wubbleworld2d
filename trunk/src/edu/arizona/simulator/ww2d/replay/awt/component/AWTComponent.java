package edu.arizona.simulator.ww2d.replay.awt.component;

import java.awt.Graphics2D;
import java.util.Comparator;

import org.dom4j.Element;

import edu.arizona.simulator.ww2d.replay.awt.AWTGameObject;

public abstract class AWTComponent {

	protected AWTGameObject _parent;
	
	protected int _renderPriority;

	/**
	 * A comparator that sorts the components by the individual render priorities
	 * Lower priorities will be first.
	 */
	public static Comparator<AWTComponent> render = new Comparator<AWTComponent>() { 
		public int compare(AWTComponent c1, AWTComponent c2) { 
			return new Integer(c1._renderPriority).compareTo(c2._renderPriority);
		}
	};
	
	public AWTComponent(AWTGameObject owner) { 
		_parent = owner;
		
		_renderPriority = 10;
	}
	
	public void setRenderPriority(int priority) { 
		_renderPriority = priority;
	}
	
	/**
	 * Fill the data in the component with the information
	 * found in the given Element
	 * @param e
	 */
	public abstract void fromXML(Element e);
	
	/**
	 * Override this method if the component is
	 * responsible for drawing anything....
	 */
	public void render(Graphics2D g) { 
		
	}
}
