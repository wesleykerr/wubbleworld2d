package edu.arizona.simulator.ww2d;

import org.apache.log4j.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.states.ReplayState;
import edu.arizona.simulator.ww2d.states.SplashState;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class Replay extends StateBasedGame {
    private static Logger logger = Logger.getLogger( Replay.class );

	private FengWrapper _fengWrapper;
	
	public Replay() {
		super("Replay");

	}
	
	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		_fengWrapper = new FengWrapper(container);

		addState(new SplashState(_fengWrapper, States.ReplayState.ordinal()));
		addState(new ReplayState(_fengWrapper));
		enterState(States.ReplayState.ordinal());
//		enterState(States.SplashState.ordinal());
	}
	
	@Override
	public boolean closeRequested() {
		logger.debug("Close requested");
		for (States s : States.values()) { 
			GameState state = getState(s.ordinal());
			if (state != null) { 
				BHGameState tmp = (BHGameState) state;
				tmp.finish();
			}
		}
		return super.closeRequested();
	}

	public static void main(String[] args) { 
		try { 
			Class<GameGlobals> globalsClass = GameGlobals.class;
			ClassLoader.getSystemClassLoader().loadClass(globalsClass.getName());
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
        try {
            AppGameContainer app = new AppGameContainer(new Replay());
		    app.setDisplayMode(800,600,false); 
		    app.setMaximumLogicUpdateInterval(20);
		    app.setMinimumLogicUpdateInterval(16);
		    app.setTargetFrameRate(80);
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }		
	}
}
