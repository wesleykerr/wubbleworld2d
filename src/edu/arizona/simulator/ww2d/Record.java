package edu.arizona.simulator.ww2d;

import java.util.List;

import org.apache.log4j.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.states.AWTReplayState;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.states.RecordingGameplayState;
import edu.arizona.simulator.ww2d.states.RecordingState;
import edu.arizona.simulator.ww2d.states.SplashState;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class Record extends StateBasedGame {
    private static Logger logger = Logger.getLogger( Record.class );

	private FengWrapper _fengWrapper;
	
	public static final int MILLIS = 30000;
	public static final int REPEAT = 100;
	
	public Record() {
		super("Movie Recorder");

	}
	
	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		_fengWrapper = new FengWrapper(container);

		RecordingState recState = new RecordingState(_fengWrapper);
		
//		recState.addParams("pass", "data/levels/Room-Empty.xml", "data/levels/Agents-Path.xml", new PathScenario1());
//		recState.addParams("talk-a", "data/levels/Room-Empty.xml", "data/levels/Agents-Path.xml", new PathScenario2());
//		recState.addParams("talk-b", "data/levels/Room-Empty.xml", "data/levels/Agents-Path.xml", new PathScenario3());
//		recState.addParams("collide", "data/levels/Room-Empty.xml", "data/levels/Agents-Path.xml", new PathScenario4());

		recState.addParams("chase", "data/levels/Room-Empty.xml", "data/levels/Agents-Chase.xml", new Scenario() {
			@Override
			public void setup() {
				// we expect 2 agents and each agent should have a random location....
				ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
				
				// agent1 is the chaser and agent2 is the chasee
				PhysicsObject chaser = objectSpace.getPhysicsObject("agent1");
				PhysicsObject chasee = objectSpace.getPhysicsObject("agent2");
				if (chaser == null || chasee == null)
					throw new RuntimeException("Expect two agents one named agent1 and the other named agent2");
				
				boolean happy = false;
				while (!happy) { 
					// find a position within the visibility range of the chasee
					
				}
			} 
			
		});
		recState.addParams("flee", "data/levels/Room-Empty.xml", "data/levels/Agents-Flee.xml", null);
		recState.addParams("fight", "data/levels/Room-Empty.xml", "data/levels/Agents-Fight.xml", null);
		recState.addParams("kick-ball", "data/levels/Room-Balls.xml", "data/levels/Agents-Kick.xml", null);
		recState.addParams("kick-column", "data/levels/Room-Columns.xml", "data/levels/Agents-Kick.xml", null);
		recState.addParams("eat", "data/levels/Room-Food.xml", "data/levels/Agents-Eat.xml", null);
		

		addState(new SplashState(_fengWrapper, States.RecordingState.ordinal()));
		addState(recState);
		addState(new RecordingGameplayState(_fengWrapper));
//		addState(new RecordingGameplayState(_fengWrapper, "data/levels/Room-Empty.xml", "data/levels/Agents-Chase.xml"));
//		addState(new RecordingGameplayState(_fengWrapper, "data/levels/Room-Empty.xml", "data/levels/Agents-Flee.xml"));
//		addState(new RecordingGameplayState(_fengWrapper, "data/levels/Room-Empty.xml", "data/levels/Agents-Fight.xml"));
//		addState(new RecordingGameplayState(_fengWrapper, "data/levels/Room-Empty.xml", "data/levels/Agents-Wander.xml"));
//		addState(new RecordingGameplayState(_fengWrapper, "data/levels/Room-Balls.xml", "data/levels/Agents-Kick.xml", null));
//		addState(new RecordingGameplayState(_fengWrapper, "data/levels/Room-Columns.xml", "data/levels/Agents-Kick.xml"));
//		addState(new RecordingGameplayState(_fengWrapper, "data/levels/Room-Food.xml", "data/levels/Agents-Eat.xml"));
		addState(new AWTReplayState(_fengWrapper));

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
            AppGameContainer app = new AppGameContainer(new Record());
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
