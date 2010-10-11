package edu.arizona.simulator.ww2d.blackboard.control;

import edu.arizona.simulator.ww2d.blackboard.ks.GlobalKS;
import edu.arizona.simulator.ww2d.blackboard.ks.MainKnowledgeSource;
import edu.arizona.simulator.ww2d.blackboard.ks.ReplayKnowledgeSource;

public class ConsoleControl {

	private MainKnowledgeSource _ap;
	private ReplayKnowledgeSource _replay;
	private GlobalKS _global;
	
	public ConsoleControl() { 
		_ap = new MainKnowledgeSource();
		_replay = new ReplayKnowledgeSource();
		_global = new GlobalKS();
	}
	
	public void update(int elapsed) { 
		_ap.update();
		_replay.update();
		_global.update();
	}
}
