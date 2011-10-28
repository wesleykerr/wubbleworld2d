package edu.arizona.simulator.ww2d.experimental.blocksworld.fsc;

import java.util.HashMap;
import java.util.LinkedList;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class ObjectFieldSpace extends Space {
	private HashMap<PhysicsObject,HashMap<String,Field>> info;
	private HashMap<PhysicsObject,HashMap<String,Field>> ephemeral;
	private HashMap<PhysicsObject,Boolean> updated;
	
	public ObjectFieldSpace(){
		super();
		info = new HashMap<PhysicsObject,HashMap<String,Field>>();
		updated = new HashMap<PhysicsObject,Boolean>();
		ephemeral = new HashMap<PhysicsObject,HashMap<String,Field>>();
		
//		for(PhysicsObject obj : ((ObjectSpace) Blackboard.inst().getSpace("object")).getPhysicsObjects()){
//			updated.put(obj, new P)
//		}
	}
	
	public void preUpdate(PhysicsObject obj){
		updated.put(obj, false);
	}
	
	public void addTemp(PhysicsObject obj, Field field){
		HashMap<String,Field> fields = ephemeral.get(obj);
		if(fields == null){
			fields = new HashMap<String,Field>();
		}
		fields.put(field.getName(),field);
		ephemeral.put(obj, fields);
	}
	
	public void add(PhysicsObject obj, Field field){
		HashMap<String,Field> fields = info.get(obj);
		if(fields == null){
			fields = new HashMap<String,Field>();
		}
		fields.put(field.getName(),field);
		info.put(obj, fields);
	}
	
	public void purge(PhysicsObject obj){
		info.put(obj, new HashMap<String,Field>());
		ephemeral = info;
	}
	
	public void remove(PhysicsObject obj, String name){
		HashMap<String, Field> fields = info.get(obj);
		if(fields != null){
			fields.remove(name);
		}
	}
	
	public Field retrieve(PhysicsObject obj, String name){
		HashMap<String, Field> fields = info.get(obj);
		if(fields == null){
			return null;
		}
		
		return fields.get(name);
	}
	
	public LinkedList<Field> getAll(PhysicsObject obj){
		HashMap<String, Field> fields = info.get(obj);
		if(fields == null){
			return null;
		}
		
		LinkedList<Field> toReturn = new LinkedList<Field>();
		toReturn.addAll(fields.values());
		return toReturn;
	}
	
	public LinkedList<String> getAllNames(PhysicsObject obj){
		HashMap<String, Field> fields = info.get(obj);
		if(fields == null){
			return null;
		}
		
		LinkedList<String> toReturn = new LinkedList<String>();
		toReturn.addAll(fields.keySet());
		return toReturn;
	}
	
	public HashMap<String,Field> getMap(PhysicsObject obj){
		return info.get(obj);
	}
	
	public void copy(PhysicsObject obj1, PhysicsObject obj2, String name){
		add(obj2,retrieve(obj1,obj2.getName() + "-" + name));
	}
	
	public void update(PhysicsObject obj){
		HashMap<String,Field> temp = info.get(obj);
		info.put(obj, ephemeral.get(obj));
		ephemeral.put(obj, temp);
		updated.put(obj, true);
	}
	
	public void updateAll(){
		ObjectSpace os = (ObjectSpace) Blackboard.inst().getSpace("object");
		for(PhysicsObject obj : os.getPhysicsObjects()){
			info.put(obj, ephemeral.get(obj));
			preUpdate(obj);
		}
	}
	
	public boolean updated(PhysicsObject obj){
		return updated.get(obj);
	}
	
	public HashMap<PhysicsObject, HashMap<String, Field>> getEphemeral(){
		return ephemeral;
	}

	public void purgeAll() {
		ephemeral = new HashMap<PhysicsObject,HashMap<String,Field>>();
	}
}
