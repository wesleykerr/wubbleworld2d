package edu.arizona.simulator.ww2d.replay.awt;

import java.awt.Graphics2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.replay.awt.component.AWTComponent;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;

public class AWTGameObject {
    private static Logger logger = Logger.getLogger( AWTGameObject.class );

	/**
	 * A comparator that sorts the components by the individual render priorities
	 * Lower priorities will be first.
	 */
	public static Comparator<AWTGameObject> render = new Comparator<AWTGameObject>() { 
		public int compare(AWTGameObject c1, AWTGameObject c2) { 
			return new Integer(c1._renderPriority).compareTo(c2._renderPriority);
		}
	};
	
	private String _name;
	private ObjectType _type;
	
	/** Location of this entity */
	protected Vec2 _position;
	
	/** heading in radians */
	protected float _heading;
	
	private int _renderPriority;
	
	private List<AWTComponent> _renderQueue;
	
	private Map<String,Object> _userData;
	
	public AWTGameObject(String name, ObjectType type, int renderPriority) { 
		_name = name;
		_type = type;
		
		_position = new Vec2(0,0);
		_heading = 0;
		
		_renderQueue = new LinkedList<AWTComponent>();
		
		_renderPriority = renderPriority;
		_userData = new HashMap<String,Object>();
	}
	
	@Override
	public String toString() {
		return _name;
	}

	/**
	 * Return the name of this GameObject
	 * @return
	 */
	public String getName() { 
		return _name;
	}
	
	/**
	 * Return the type of game object that this is.
	 * @return
	 */
	public ObjectType getType() { 
		return _type;
	}
	
	public void setPriority(int renderPriority) { 
		_renderPriority = renderPriority;
	}

	/**
	 * Add the component to the different queues
	 * @param c
	 */
	public void addComponent(AWTComponent c) { 
		_renderQueue.add(c);
		
		Collections.sort(_renderQueue, AWTComponent.render);
	}
	
	/**
	 * Called every frame in order to render
	 * this Object and its components.
	 * @param g
	 */
	public void render(Graphics2D g) { 
		for (AWTComponent c : _renderQueue) { 
			c.render(g);
		}
	}
	
	/**
	 * Return the position of this GameObject
	 * @return
	 */
	public Vec2 getPosition() { 
		return _position;
	}
	
	/**
	 * Sets the position of this GameObject
	 * @param v
	 */
	public void setPosition(Vec2 v) { 
		_position.set(v);
	}
	
	/**
	 * Return the current heading of this GameObject
	 * @return
	 */
	public float getHeading() { 
		return _heading;
	}
	
	/**
	 * Set the heading of this GameObject
	 * @param heading
	 */
	public void setHeading(float heading) { 
		_heading = heading;
	}

	/**
	 * Set some user data specific to this GameObject
	 * @param name
	 * @param data
	 */
	public void setUserData(String name, Object data) { 
		_userData.put(name, data);
	}
	
	public <T> T getUserData(String name, Class<T> c) { 
		return c.cast(_userData.get(name));
	}
	
	public Object getUserData(String name) { 
		return _userData.get(name);
	}
}