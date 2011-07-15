package edu.arizona.simulator.ww2d.utils;

public class CounterPerSecond {
	
	private long _start;
	private float _value;
	private float _counter;
	
	public CounterPerSecond() {
		_start = System.currentTimeMillis();
	}
	
	public void increment() { 
		++_counter;
	}
	
	public float getValue() { 
		long currentTime = System.currentTimeMillis();
		if (currentTime - _start > 1000) { 
			_value = (_counter / (float) (currentTime - _start)) * 1000f;
			_counter = 0;
			_start = currentTime;
		}
		return _value;
	}
}
