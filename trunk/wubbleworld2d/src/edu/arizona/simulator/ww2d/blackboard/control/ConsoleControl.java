package edu.arizona.simulator.ww2d.blackboard.control;

import edu.arizona.simulator.ww2d.blackboard.ks.GlobalKS;
import edu.arizona.simulator.ww2d.blackboard.ks.MainKnowledgeSource;
import edu.arizona.simulator.ww2d.blackboard.ks.ReplayKnowledgeSource;
import edu.arizona.simulator.ww2d.utils.GameGlobals;

public class ConsoleControl {

	private MainKnowledgeSource _ap;
	private ReplayKnowledgeSource _replay;
	private GlobalKS _global;
	
	public ConsoleControl() { 
		_ap = new MainKnowledgeSource();
		
		if (GameGlobals.record) { 
			_replay = new ReplayKnowledgeSource();
			_global = new GlobalKS();
		}
	}
	
	public void update(int elapsed) { 
		_ap.update();

		if (_replay != null) 
			_replay.update();
		
		if (_global != null) 
			_global.update();
	}
}
