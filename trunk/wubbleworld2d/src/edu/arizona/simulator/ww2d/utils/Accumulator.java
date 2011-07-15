package edu.arizona.simulator.ww2d.utils;

import java.util.LinkedList;

/**
 * The accumulator will allow you to create a running
 * window of the statistics that may be important for you.
 * @author wkerr
 *
 */
public class Accumulator {

	private int _maxSize;
	
	private LinkedList<Float> _values;
	private float _total;
	
	public Accumulator(int size) { 
		_maxSize = size;
		_values = new LinkedList<Float>();
		_total = 0;
	}
	
	/**
	 * Return the number of values stored as a 
	 * ratio of the total size we could have.
	 * @return
	 */
	public float getSize() { 
		return (float) _values.size() / (float) _maxSize;
	}
	
	public float getAverage() { 
		if (_values.isEmpty())
			return 0;

		return _total / (float) _values.size();
	}
	
	public void record(float value) { 
		_values.addFirst(value);
		_total += value;
		
		if (_values.size() > _maxSize) { 
			float tmp = _values.removeLast();
			_total -= tmp;
		}
	}
	
	public void reset() { 
		_values.clear();
		_total = 0;
	}
}
