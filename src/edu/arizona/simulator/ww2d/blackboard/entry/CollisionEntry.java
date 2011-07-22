package edu.arizona.simulator.ww2d.blackboard.entry;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class CollisionEntry extends Entry {
    private static Logger logger = Logger.getLogger( CollisionEntry.class );

	private String _id;
	private String _key;
	private String _name;
	
	private PhysicsObject _obj1;
	private PhysicsObject _obj2;

	private Vec2 _position;
	private Vec2 _normal;
	private Vec2 _velocity;
	
	private boolean _processed;

	private float _normalImpulse;
	private float _tangentImpulse;
	
	public CollisionEntry(ContactPoint cp, PhysicsObject obj1, PhysicsObject obj2) { 
		_obj1 = obj1;
		_obj2 = obj2;

		if (_obj1.getName().compareTo(_obj2.getName()) < 0) { 
			_name = _obj1.getName() + " " + _obj2.getName();
		} else { 
			_name = _obj2.getName() + " " + _obj1.getName();
		}

		_position = cp.position;
		_normal = cp.normal;
		_velocity = cp.velocity;

		_processed = false;
		
		_id = cp.id.features.toString();
		_key = key(cp, _obj1, _obj2);
	}
	
	public void update(ContactPoint cp) { 
		_position = cp.position;
		_normal = cp.normal;
		_velocity = cp.velocity;
		
		_processed = false;
	}
	
	public void update(ContactResult cr) { 
		_normalImpulse = cr.normalImpulse;
		_tangentImpulse = cr.tangentImpulse;
		
		_processed = false;
	}
	
	public String key() { 
		return _key;
	}
	
	public String getId() { 
		return _id;
	}
	
	public String getName() { 
		return _name;
	}
	
	public PhysicsObject getObject1() { 
		return _obj1;
	}
	
	public PhysicsObject getObject2() { 
		return _obj2;
	}
	
	public PhysicsObject getOther(PhysicsObject obj) { 
		if (_obj1 == obj)
			return _obj2;
		return _obj1;
	}
	
	public Vec2 getPosition() { 
		return _position;
	}
	
	public Vec2 getNormal() { 
		return _normal;
	}
	
	public Vec2 getVelocity() { 
		return _velocity;
	}
	
	public boolean processed() { 
		return _processed;
	}
	
	public void processed(boolean status) { 
		_processed = status;
	}
	
	public float getNormalImpulse() { 
		return _normalImpulse;
	}
	
	public float getTangentImpulse() { 
		return _tangentImpulse;
	}
	
	public void classify() {
		
	}
	
	public static String key(ContactPoint cp, PhysicsObject obj1, PhysicsObject obj2) { 
		String name = "";
		if (obj1.getName().compareTo(obj2.getName()) < 0) { 
			name = obj1.getName() + " " + obj2.getName();
		} else { 
			name = obj2.getName() + " " + obj1.getName();
		}

		return name + cp.id.features.toString();
	}
	
	public static String key(ContactResult cp, PhysicsObject obj1, PhysicsObject obj2) { 
		String name = "";
		if (obj1.getName().compareTo(obj2.getName()) < 0) { 
			name = obj1.getName() + " " + obj2.getName();
		} else { 
			name = obj2.getName() + " " + obj1.getName();
		}

		return name + cp.id.features.toString();
	}
	
}
