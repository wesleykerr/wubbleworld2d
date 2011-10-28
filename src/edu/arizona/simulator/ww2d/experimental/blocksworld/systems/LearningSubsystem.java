package edu.arizona.simulator.ww2d.experimental.blocksworld.systems;

import java.util.LinkedList;

import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.experimental.blocksworld.fsc.ObjectFieldSpace;
import edu.arizona.simulator.ww2d.experimental.blocksworld.learning.Recorder;
import edu.arizona.simulator.ww2d.system.Subsystem;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public class LearningSubsystem implements Subsystem {
	
	LinkedList<Record> prev;
	Record rec;
	int interval = 1000;
	int curr = 0;
	
	private class Record{
		Recorder recorder;
		String info;
		public Record(String info, Recorder rec){
			this.recorder = rec;
			this.info = info;
		}
		
		public void output(String filename){
			//TODO stub
		}
		
	}
	
	public LearningSubsystem(String info){
		prev = new LinkedList<Record>();
		rec = new Record(info,new Recorder(true,false));
		Blackboard.inst().addSpace("recorder", new ObjectFieldSpace());
	}
	
	@Override
	public SubsystemType getId() {
		return SubsystemType.LearningSubsystem;
	}

	@Override
	public void update(int eps) {
		curr += eps;
		if( curr >= interval){
			rec.recorder.update(eps);
			curr -= interval;
		}
	}

	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO: currently throwing a null pointer exception that I'm
		// not sure what the cause is.
//		rec.recorder.calculateData();
//		rec.output("tmp");
	}

}
