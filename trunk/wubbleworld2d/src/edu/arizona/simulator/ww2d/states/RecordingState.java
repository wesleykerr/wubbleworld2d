package edu.arizona.simulator.ww2d.states;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.Record;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.logging.StateDatabase;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class RecordingState extends BHGameState {
    private static Logger logger = Logger.getLogger( RecordingState.class );
	
    private List<Params> _paramsList;
    private int _index = 0;
    
    private int _count = 0;
    
	public RecordingState(FengWrapper feng) { 
		super(feng);
		
		_paramsList = new ArrayList<Params>();
	}
	
	public void addParams(String name, String levelFile, String agentFile, Scenario scenario) {
		_paramsList.add(new Params(name, levelFile, agentFile, scenario));
	}
	
	@Override
	public int getID() {
		return States.RecordingState.ordinal();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {

	}

	@Override 
	public void enter(GameContainer container, StateBasedGame game) throws SlickException { 
		super.enter(container, game);
		
		++_count;
		
		// Test to see if we have repeated the parameters the designated 
		// number of times.
		if (_count > Record.REPEAT) { 
			// Move to the next set of parameters...
			_count = 1;
			++_index;
			if (_index >= _paramsList.size()) {
				container.exit();
				return;
			}
		}
		
		Params params = _paramsList.get(_index);
		String directory = params.name + "-" + _count;
		File f = new File("states/" + directory + "/");
		f.mkdir();
		
		StateDatabase.PATH = "states/" + directory + "/";
		RecordingGameplayState state = (RecordingGameplayState) game.getState(States.RecordingGameplayState.ordinal());
		state.setParams(params.levelFile, params.agentFile, params.scenario);
		
		game.enterState(States.RecordingGameplayState.ordinal());
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.leave(container, game);

		// Don't forget to remove all of the widgets before moving
		// to the next state.  If you forget then the widgets (although not rendered)
		// could actually receive button presses.
		_feng.getDisplay().removeAllWidgets();
	}

	public void finish() { 
		
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {

	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int millis) throws SlickException {

	}		
}

class Params { 
	public String name;
	
	public String levelFile;
	public String agentFile;
	public Scenario scenario;
	
	public Params(String name, String levelFile, String agentFile, Scenario scenario) { 
		this.name = name;
		this.levelFile = levelFile;
		this.agentFile = agentFile;
		this.scenario = scenario;
	}
}
