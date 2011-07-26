package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.HashMap;
import java.util.LinkedList;

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
	
	public LinkedList<Field> getAll(FSCState state){
		HashMap<String, Field> fields = info.get(state);
		if(fields == null){
			return null;
		}
		
		LinkedList<Field> toReturn = new LinkedList<Field>();
		toReturn.addAll(fields.values());
		return toReturn;
	}
	
	public LinkedList<String> getAllNames(FSCState state){
		HashMap<String, Field> fields = info.get(state);
		if(fields == null){
			return null;
		}
		
		LinkedList<String> toReturn = new LinkedList<String>();
		toReturn.addAll(fields.keySet());
		return toReturn;
	}
	
	public HashMap<String,Field> getMap(FSCState state){
		return info.get(state);
	}
	
	public void copy(FSCState state1, FSCState state2, String name){
		add(state2,retrieve(state1,name));
	}
}
