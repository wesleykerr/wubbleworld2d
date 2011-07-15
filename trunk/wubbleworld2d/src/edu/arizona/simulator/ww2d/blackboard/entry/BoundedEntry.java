package edu.arizona.simulator.ww2d.blackboard.entry;

/**
 * @author wkerr
 *
 */
public class BoundedEntry extends Entry {

	private float _value;
	
	private float _min;
	private float _max;
	
	public BoundedEntry(float value, float min, float max) { 
		super();
		
		_value = value;
		_min = min;
		_max = max;
		
		constrain();
	}
	
	/**
	 * Change the value stored within by the given delta.
	 * @param delta
	 */
	public void change(float delta) {
		_value += delta;
		constrain();
		updated();
	}
	
	/**
	 * Set the value to the one given.
	 * @param value
	 */
	public void set(float value) { 
		this._value = value;
		constrain();
		updated();
	}
	
	private void constrain() { 
		_value = Math.max(_min, _value);
		_value = Math.min(_max, _value);
	}
	
	public float getMax() { 
		return _max;
	}
	
	public float getMin() { 
		return _min;
	}
	
	public float getValue() { 
		return _value;
	}
	
}
