package edu.arizona.simulator.ww2d.experimental.blocksworld.states;

import java.io.File;
import java.util.LinkedList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.experimental.blocksworld.Params;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.logging.StateDatabase;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class CounterState extends BHGameState{
	private LinkedList<Params> params;
	private int curr = 0;

	public CounterState(FengWrapper feng, LinkedList<Params> params) {
		super(feng);
		this.params = params;
	}

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}
	
	public int getID() {
		return States.RecordingState.ordinal();
	}

	@Override
	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}
	
	public void enter(GameContainer container, StateBasedGame game) throws SlickException { 
		super.enter(container, game);
		if(curr < params.size()){
			StateDatabase.PATH = "edu/arizona/simulator/ww2d/experimental/blocksworld/movies/" + "scenario-" + curr + "/";
			((BlocksworldState) game.getState(States.MainMenuState.ordinal())).setLevel(params.get(curr).getLevel());
			((BlocksworldState) game.getState(States.MainMenuState.ordinal())).setPhysics(params.get(curr).getPhysics());
			((BlocksworldState) game.getState(States.MainMenuState.ordinal())).setDuration(params.get(curr).getDuration());
			String directory = "blocksworld-" + curr;
			File f = new File("edu/arizona/simulator/ww2d/experimental/blocksworld/movies/" + directory + "/");
			++curr;
			game.enterState(States.MainMenuState.ordinal());
		} else {
			//game.enterState(States.ReplayState.ordinal());
			//container.exit();
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
