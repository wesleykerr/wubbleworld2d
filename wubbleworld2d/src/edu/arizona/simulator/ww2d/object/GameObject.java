package edu.arizona.simulator.ww2d.object;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.object.component.Component;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;

public class GameObject {
    private static Logger logger = Logger.getLogger( GameObject.class );

	/**
	 * A comparator that sorts the components by the individual render priorities
	 * Lower priorities will be first.
	 */
	public static Comparator<GameObject> render = new Comparator<GameObject>() { 
		public int compare(GameObject c1, GameObject c2) { 
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
	
	private List<Component> _updateQueue;
	private List<Component> _renderQueue;
	
	private Map<String,Object> _userData;
	
	public GameObject(String name, ObjectType type, int renderPriority) { 
		_name = name;
		_type = type;
		
		_position = new Vec2(0,0);
		_heading = 0;
		
		_updateQueue = new LinkedList<Component>();
		_renderQueue = new LinkedList<Component>();
		
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

	/**
	 * Add the component to the different queues
	 * @param c
	 */
	public void addComponent(Component c) { 
		_updateQueue.add(c);
		_renderQueue.add(c);
		
		Collections.sort(_updateQueue, Component.update);
		Collections.sort(_renderQueue, Component.render);
	}
	
	/**
	 * Called every update cycle.  Allows us to
	 * update all of the components attached to this
	 * entity.
	 * @param elapsed
	 */
	public void update(int elapsed) { 
		for (Component c : _updateQueue) { 
			c.update(elapsed);
		}
	}
	
	/**
	 * Called every frame in order to render
	 * this Object and its components.
	 * @param g
	 */
	public void render(Graphics g) { 
		g.pushTransform();
		for (Component c : _renderQueue) { 
			c.render(g);
		}
		g.popTransform();
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
//		if (_userData.containsKey(name))
//			logger.error("User Data Key Collision: " + _name + " key: " + name + "");
		_userData.put(name, data);
	}
	
	public <T> T getUserData(String name, Class<T> c) { 
		return c.cast(_userData.get(name));
	}
	
	public Object getUserData(String name) { 
		return _userData.get(name);
	}
}
