package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.HashMap;

import edu.arizona.simulator.ww2d.blackboard.spaces.Space;

public class StateFieldSpace extends Space {
	private HashMap<FSCState,HashMap<String,Field>> info;
	public StateFieldSpace(){
		super();
		info = new HashMap<FSCState,HashMap<String,Field>>();
	}
	
	public void add(FSCState state, Field field){
		HashMap<String,Field> fields = info.get(state);
		if(fields == null){
			fields = new HashMap<String,Field>();
		}
		fields.put(field.getName(),field);
		info.put(state, fields);
	}
	
	public void remove(FSCState state, String name){
		HashMap<String, Field> fields = info.get(state);
		if(fields != null){
			fields.remove(name);
		}
	}
	
	public Field retrieve(FSCState state, String name){
		HashMap<String, Field> fields = info.get(state);
		if(fields == null){
			return null;
		}
		
		return fields.get(name);
	}
	
	public void copy(FSCState state1, FSCState state2, String name){
		add(state2,retrieve(state1,name));
	}
}
