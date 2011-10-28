package edu.arizona.simulator.ww2d.experimental.blocksworld.learning;

import java.util.HashMap;
import java.util.LinkedList;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.Field;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.object.PhysicsObject;

public class Recorder {

	private class Data {
		Vec2 pos;
		int eps;
		PhysicsObject owner;

		public Data(Vec2 pos, int eps, PhysicsObject owner) {
			this.pos = pos;
			this.eps = eps;
			this.owner = owner;
		}
	}
	
	private class Update {
		HashMap<String,Field> fields;
		PhysicsObject owner;
		int eps;
		int cycle;
		
		public Update(HashMap<String,Field> fields, PhysicsObject owner, int eps,int cycle){
			this.fields = fields;
			this.owner = owner;
			this.eps = eps;
			this.cycle = cycle;
		}
		
		public void print(){
			System.out.println("name -- " + owner.getName() + " time elapsed -- " + eps + " iteration -- " + cycle);
			for(String name : fields.keySet()){
				System.out.println(" Field -- " + name + " " + fields.get(name));
			}
		}
	}

	public boolean deferred = false;
	LinkedList<Data> positions;
	LinkedList<Update> updates;
	private boolean quietMode = false;

	public Recorder(boolean wait, boolean quiet) {
		deferred = wait;
		quietMode = quiet;
		positions = new LinkedList<Data>();
	}

	public void update(int elapse) {
		ObjectSpace os = (ObjectSpace) Blackboard.inst().getSpace("object");
		ObjectFieldSpace rec = (ObjectFieldSpace) Blackboard.inst().getSpace(
				"recorder");
		for (PhysicsObject obj : os.getPhysicsObjects()) {
			recordPosition(elapse,obj);
		}

	}
	
	private void recordPosition(int elapse,PhysicsObject obj){
		positions.add(new Data(obj.getPPosition(),elapse,obj));
	}

	// Warning, this will be slow since you have to process a LOT of data
	public void calculateData() {
		ObjectSpace os = (ObjectSpace) Blackboard.inst().getSpace("object");
		ObjectFieldSpace rec = (ObjectFieldSpace) Blackboard.inst().getSpace(
				"recorder");

		int num = 0;
		int iteration = 1;
		updates = new LinkedList<Update>();
		for (Data d : positions) {
			calculateEverything(d, rec,iteration);
			updates.add(new Update(rec.getMap(d.owner),d.owner,d.eps,iteration));
			rec.update(d.owner);
			
			++num;
			if(num == os.getPhysicsObjects().size()){
				rec.updateAll();
				rec.purgeAll();
				num = 0;
				++iteration;
			}
		}
	
	}
	
	public void printall(){
		System.out.println("Dumping positional data:");
		for(Data d : positions){
			System.out.println("Object: " + d.owner.getName() + " x - " + d.pos.x + " y - " + d.pos.y + " elapsed " + d.eps);
		}
	}

	
	private void calculateEverything(Data d, ObjectFieldSpace rec,int iteration) {
		recordPosition(d,rec);
		if(iteration > 1){
			// First order: velocity, moving, variation in position etc
			calculateVelocity(d,rec);
			isMoving(d,rec);
			towards(d,rec);
		}
		if(iteration > 2){
			// Second order: acceleration, variation in velocity etc
			calculateAcceleration(d,rec);
			isStopStart(d,rec);
			isAccelerating(d,rec);
		}
	}

	private void towards(Data data, ObjectFieldSpace rec) {
		ObjectSpace os = (ObjectSpace) Blackboard.inst().getSpace("object");
		HashMap<String,Field> newFields = rec.getEphemeral().get(data.owner);
		
		for(PhysicsObject obj : os.getPhysicsObjects()){
			if(!obj.equals(data.owner)){
				HashMap<String,Field> otherFields = rec.getEphemeral().get(obj);
				if(otherFields == null){
					otherFields = rec.getMap(obj);
				}
				
				float dx = (Float)newFields.get("dx").data;
				float dy = (Float)newFields.get("dy").data;
				float x = (Float)newFields.get("x").data;
				float y = (Float)newFields.get("y").data;
				
				float otherX = (Float)otherFields.get("x").data;
				float otherY = (Float)otherFields.get("y").data;
				
				// Need to find a way to clean this up
				if(Math.abs(dx) > 0.0000001 || Math.abs(dy) > 0.0000001){
					if(dx >= 0 && dy >= 0){
						if(otherX > x && otherY > y){
							rec.add(data.owner, new Field("towards(" +  data.owner.getName() + "," + obj.getName() + ")", true));
						} else {
							rec.add(data.owner, new Field("towards " + obj.getName(), false));
						}
					} else if (dx >=0 && dy < 0) {
						if(otherX > x && otherY < y){
							rec.add(data.owner, new Field("towards " + obj.getName(), true));
						} else {
							rec.add(data.owner, new Field("towards " + obj.getName(), false));
						}
					} else if (dx < 0 && dy >= 0){
						if (otherX < x && otherY > y){
							rec.add(data.owner, new Field("towards " + obj.getName(), true));
						} else {
							rec.add(data.owner, new Field("towards " + obj.getName(), false));
						}	
						
					} else {
						if(otherX < x && otherY < y){
							rec.add(data.owner, new Field("towards " + obj.getName(), true));
						} else {
							rec.add(data.owner, new Field("towards " + obj.getName(), false));
						}	
					}
				} else { 
					rec.add(data.owner, new Field("towards" + obj.getName(), false));
				}
			}
		}
	}

	private void isAccelerating(Data data, ObjectFieldSpace rec) {
		HashMap<String,Field> newFields = rec.getEphemeral().get(data.owner);
		if(Math.abs((Float) newFields.get("ax").data) > .00000001f && Math.abs((Float) newFields.get("ay").data) > .00000001f){
			rec.addTemp(data.owner, new Field("accelerating",true));
		} else {
			rec.addTemp(data.owner, new Field("accelerating",true));
		}
	}

	private void isStopStart(Data data, ObjectFieldSpace rec) {
		HashMap<String,Field> fields = rec.getMap(data.owner);
		HashMap<String,Field> newFields = rec.getEphemeral().get(data.owner);
		boolean stopped = (Boolean)newFields.get("moving").data && !(Boolean) fields.get("moving").data;
		boolean started = !(Boolean)newFields.get("moving").data && (Boolean) fields.get("moving").data;
		
		rec.addTemp(data.owner, new Field("stopped",started));
		rec.addTemp(data.owner, new Field("stopped",stopped));
	}

	private void calculateAcceleration(Data data, ObjectFieldSpace rec) {
		HashMap<String,Field> fields = rec.getMap(data.owner);
		HashMap<String,Field> newFields = rec.getEphemeral().get(data.owner);
		float ax = (Float)newFields.get("dx").data - (Float) fields.get("dx").data;
		float ay = (Float)newFields.get("dy").data - (Float) fields.get("dy").data;
		
		ax *= data.eps;
		ay *= data.eps;
		
		rec.addTemp(data.owner, new Field("ax",ax));
		rec.addTemp(data.owner, new Field("ay",ay));
	}

	private void isMoving(Data data, ObjectFieldSpace rec) {
		HashMap<String,Field> newFields = rec.getEphemeral().get(data.owner);
		if(Math.abs((Float) newFields.get("dx").data) > .00000001f && Math.abs((Float) newFields.get("dy").data) > .00000001f){
			rec.addTemp(data.owner, new Field("moving",true));
		} else {
			rec.addTemp(data.owner, new Field("moving",true));
		}
	}

	private void recordPosition(Data data, ObjectFieldSpace rec) {
		rec.addTemp(data.owner, new Field("x",data.pos.x));
		rec.addTemp(data.owner, new Field("y",data.pos.y));
	}
	
	private void calculateVelocity(Data data, ObjectFieldSpace rec){
		HashMap<String,Field> fields = rec.getMap(data.owner);
		HashMap<String,Field> newFields = rec.getEphemeral().get(data.owner);
		float dx = (Float)newFields.get("x").data - (Float) fields.get("x").data;
		float dy = (Float)newFields.get("y").data - (Float) fields.get("y").data;
		
		dx *= data.eps;
		dy *= data.eps;
		
		rec.addTemp(data.owner, new Field("dx",dx));
		rec.addTemp(data.owner, new Field("dy",dy));
	}

	protected void updateVelocity(int eps, PhysicsObject obj,
			ObjectFieldSpace rec) {
		HashMap<String, Field> fields = rec.getMap(obj);
		float newX = obj.getPPosition().x;
		float newY = obj.getPPosition().y;

		rec.addTemp(obj, new Field("x", newX));
		rec.addTemp(obj, new Field("y", newY));

		if (fields == null) {
			return;
		}

		Field xpos = fields.get("x");
		Field ypos = fields.get("y");

		if (xpos == null || ypos == null) {
			return;
		}

		float x = (Float) xpos.getData();
		float y = (Float) ypos.getData();
		float dx = eps * (newX - x);
		float dy = eps * (newY - y);
		rec.addTemp(obj, new Field("dx", dx));
		rec.addTemp(obj, new Field("dy", dy));
		boolean moving = dx > 0.0000000001 || dy > 0.0000000001;

		rec.addTemp(obj, new Field("moving", moving));
		boolean stopped = false;
		boolean started = false;

		if (fields.get("moving") != null) {
			stopped = !moving && (Boolean) fields.get("moving").getData();
			started = moving && !((Boolean) fields.get("moving").getData());

		}

		rec.addTemp(obj, new Field("stopped", stopped));
		rec.addTemp(obj, new Field("started", started));
		if (!quietMode) {
			System.out.println(obj.getName() + " x - " + newX + " y - " + newY
					+ " dx - " + dx + " dy - " + dy + " moving - " + moving
					+ " stopped - " + stopped + " started - " + started);
		}
	}

	public void print() {

	}
}
