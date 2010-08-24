package edu.arizona.simulator.ww2d.states;

import java.io.File;

import org.apache.log4j.Logger;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.Record;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.logging.StateDatabase;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class RecordingState extends BHGameState {
    private static Logger logger = Logger.getLogger( RecordingState.class );
	
    private int _count = 0;
    
	public RecordingState(FengWrapper feng) { 
		super(feng);
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
		if (_count > Record.REPEAT) { 
			container.exit();
			return;
		}
		
		String directory = "run-" + System.currentTimeMillis();
		File f = new File("states/" + directory + "/");
		f.mkdir();
		
		StateDatabase.PATH = "states/" + directory + "/";
		
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
