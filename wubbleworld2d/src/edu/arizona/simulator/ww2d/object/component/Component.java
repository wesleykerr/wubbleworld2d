package edu.arizona.simulator.ww2d.object.component;

import java.util.Comparator;

import org.dom4j.Element;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.object.GameObject;

public abstract class Component {

	protected GameObject _parent;
	
	protected int _updatePriority;
	protected int _renderPriority;

	/**
	 * A comparator that sorts the components by the individual update priorities.
	 * Lower priorities will be first.
	 */
	public static Comparator<Component> update = new Comparator<Component>() { 
		public int compare(Component c1, Component c2) { 
			return new Integer(c1._updatePriority).compareTo(c2._updatePriority);
		}
	};
	
	/**
	 * A comparator that sorts the components by the individual render priorities
	 * Lower priorities will be first.
	 */
	public static Comparator<Component> render = new Comparator<Component>() { 
		public int compare(Component c1, Component c2) { 
			return new Integer(c1._renderPriority).compareTo(c2._renderPriority);
		}
	};
	
	public Component(GameObject owner) { 
		_parent = owner;
		
		_updatePriority = 10;
		_renderPriority = 10;
	}
	
	public void setRenderPriority(int priority) { 
		_renderPriority = priority;
	}
	
	/**
	 * Called every frame so that the component
	 * can update things it needs to update.
	 * @param elapsed
	 */
	public abstract void update(int elapsed);
	
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
	public void render(Graphics g) { 
		
	}
}
