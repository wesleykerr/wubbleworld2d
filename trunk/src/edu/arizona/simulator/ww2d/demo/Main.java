package edu.arizona.simulator.ww2d.demo;

import org.apache.log4j.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.gui.FengWrapper;

public class Main extends StateBasedGame {
    private static Logger logger = Logger.getLogger( Main.class );

	private FengWrapper _fengWrapper;
	
	public Main() {
		super("Kinematics Demo");

	}
	
	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		_fengWrapper = new FengWrapper(container);

		KinematicDemo state = new KinematicDemo(_fengWrapper);
		addState(state);

		enterState(state.getID());
	}
	
	public static void main(String[] args) { 
        try {
            AppGameContainer app = new AppGameContainer(new Main());
		    app.setDisplayMode(1000,600,false); 
		    app.setMaximumLogicUpdateInterval(20);
		    app.setMinimumLogicUpdateInterval(16);
		    app.setTargetFrameRate(80);
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }		
	}
}
