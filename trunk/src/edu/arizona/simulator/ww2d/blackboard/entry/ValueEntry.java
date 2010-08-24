package edu.arizona.simulator.ww2d.blackboard.entry;


public class ValueEntry extends Entry {

	private Object _value;
	
	public ValueEntry(Object value) { 
		_value = value;
	}
	
	public void setValue(Object value) { 
		_value = value;
		updated();
	}
	
	public Object getValue() { 
		return _value;
	}
	
	public <T> T get(Class<T> c) { 
		return c.cast(_value);
	}
}
