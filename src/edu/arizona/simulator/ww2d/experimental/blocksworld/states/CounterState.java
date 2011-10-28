package edu.arizona.simulator.ww2d.experimental.blocksworld.states;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.level.DefaultLoader;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.enums.States;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public class CounterState extends BHGameState{
	private int count;
	private int curr = 0;

	public CounterState(FengWrapper feng, int count) {
		super(feng);
		this.count = count;
	}

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}
	
	public int getID() {
		return States.GameplayState.ordinal();
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
		++curr;
		if(curr < count){
			game.enterState(States.MainMenuState.ordinal());
		} else {
			container.exit();
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
