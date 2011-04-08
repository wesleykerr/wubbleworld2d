package edu.arizona.simulator.ww2d;

import org.apache.log4j.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.scenario.VisibleScenario;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.states.GameplayState;
import edu.arizona.simulator.ww2d.states.MainMenuState;
import edu.arizona.simulator.ww2d.states.SplashState;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class Main extends StateBasedGame {
    private static Logger logger = Logger.getLogger( Main.class );
    
    private String _levelFile = "data/levels/Room-Balls.xml";
    private String _agentsFile = "data/levels/Agents-Test.xml";
    private Scenario _scenario = null;// new VisibleScenario("agent1", "ball1");

	private FengWrapper _fengWrapper;
	
	public Main() {
		super("Movie Generator");

	}
	
	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		_fengWrapper = new FengWrapper(container);

		addState(new SplashState(_fengWrapper, States.MainMenuState.ordinal()));
		addState(new MainMenuState(_fengWrapper));
		addState(new GameplayState(_fengWrapper, _levelFile, _agentsFile, _scenario));

		enterState(States.SplashState.ordinal());
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
            AppGameContainer app = new AppGameContainer(new Main());
		    app.setDisplayMode(800,600,false); 
		    app.setMaximumLogicUpdateInterval(20);
		    app.setMinimumLogicUpdateInterval(16);
		    app.setTargetFrameRate(80);
		    app.setAlwaysRender(true);
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }		
	}
}
