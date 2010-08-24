package edu.arizona.simulator.ww2d.states;

import org.newdawn.slick.state.BasicGameState;

import edu.arizona.simulator.ww2d.gui.FengWrapper;

public abstract class BHGameState extends BasicGameState {
	/** Each GameState has access to the FengWrapper since
	 *  there is only one needed per Game.
	 */
	protected FengWrapper _feng;
	
	public BHGameState(FengWrapper feng) { 
		_feng = feng;
	}
	
	public abstract void finish();
}
