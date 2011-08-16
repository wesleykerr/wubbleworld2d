package edu.arizona.simulator.ww2d.experimental.blocksworld;

import org.apache.log4j.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.experimental.blocksworld.states.BlocksworldState;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.states.GameplayState;
import edu.arizona.simulator.ww2d.states.MainMenuState;
import edu.arizona.simulator.ww2d.states.SplashState;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.States;

// put this in VM args: -Xmx2048m -Djava.library.path="natives/linux/"

public class Main extends StateBasedGame {

	private static Logger logger = Logger.getLogger( Main.class );
	
	private String _bw_path = "edu/arizona/simulator/ww2d/experimental/blocksworld/";
	private String _levelFile = _bw_path + "data/levels/Room-Blocksworld-objs-angle-test.xml";
	private String _agentsFile = _bw_path + "data/levels/Agents-Blocksworld.xml";
	
	private Scenario _scenario; // new VisibleScenario("claw", "ball1");
	
	private FengWrapper _fengWrapper;
	
	public Main() {
		super("Blocks World");
	}
	
	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		
		_scenario = new BlocksworldScenario();
		
		_fengWrapper = new FengWrapper(container);
		
		// addState(new SplashState(_fengWrapper, States.MainMenuState.ordinal()));
		// addState(new MainMenuState(_fengWrapper));
		addState(new BlocksworldState(_fengWrapper, _levelFile, _agentsFile, _scenario));
		
		// enterState(States.SplashState.ordinal());
		enterState(States.GameplayState.ordinal());
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
			app.setDisplayMode(800, 600, false);
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
