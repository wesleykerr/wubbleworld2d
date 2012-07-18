package edu.arizona.simulator.ww2d.experimental.blocksworld;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.experimental.blocksworld.loaders.XMLMutator;
import edu.arizona.simulator.ww2d.experimental.blocksworld.states.BlocksworldState;
import edu.arizona.simulator.ww2d.experimental.blocksworld.states.CounterState;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.states.AWTReplayState;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.States;

// put this in VM args: -Xmx2048m -Djava.library.path="natives/linux/"

public class Main extends StateBasedGame {

	private static Logger logger = Logger.getLogger( Main.class );
	
	private String _bw_path = "edu/arizona/simulator/ww2d/experimental/blocksworld/";
	private String _levelFile = _bw_path + "data/levels/Room-Blocksworld-objs-angle-test-simple.xml";
	private String _agentsFile = _bw_path + "data/levels/Agents-Blocksworld.xml";
	private String _fscFile = _bw_path + "data/levels/States.xml";
	
	// False means .dump files will be generated in _bw_path/data/levels after each experiment
	// true still has console output, but no dump files
	public static final boolean QUIET_MODE = true;
	
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

		LinkedList<Params> params = new LinkedList<Params>();
		//params.add(new Params(_levelFile,true,9000));
		//params.add(new Params(_levelFile,false,20000));
		//params.add(new Params(_bw_path + "data/levels/go.xml", true, 9000));
		//params.add(new Params(_bw_path + "data/levels/go.xml", false, 9000));
		//params.add(new Params(_bw_path + "data/levels/slow.xml", false, 15000));
		//params.add(new Params(_bw_path + "data/levels/turn.xml", false, 20000));
		//params.add(new Params(_bw_path + "data/levels/fall.xml", false, 15000));
		//params.add(new Params(_bw_path + "data/levels/scenario.xml", false, 25000));
		params.add(new Params(_bw_path + "data/levels/go2.xml", true, 8000));
		
		addState(new CounterState(_fengWrapper,params));
		addState(new BlocksworldState(_fengWrapper, _levelFile, _agentsFile, _fscFile, _scenario));
		addState(new AWTReplayState(_fengWrapper));
		
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
