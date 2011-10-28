package edu.arizona.simulator.ww2d.experimental.blocksworld.states;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import edu.arizona.simulator.ww2d.experimental.blocksworld.loaders.BlocksworldLoader;
import edu.arizona.simulator.ww2d.experimental.blocksworld.systems.FSCSubsystem;
import edu.arizona.simulator.ww2d.experimental.blocksworld.systems.LearningSubsystem;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.gui.TWLInputAdapter;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.enums.States;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;

public class BlocksworldState extends BHGameState {
	private static Logger logger = Logger.getLogger(BlocksworldState.class);

	private String _levelFile;
	private String _agentsFile;
	private String _fscFile;
	private Scenario _scenario;

	private GameSystem _gameSystem;
	private boolean _shiftDown;

	private LWJGLRenderer lwjglRenderer;
	private ThemeManager theme;
	private GUI gui;
	private Widget root;
	private TWLInputAdapter twlInputAdapter;

	private long _enterTime;
	private long currTime = 0;
	private boolean _update;
	private boolean physics;

	public BlocksworldState(FengWrapper feng, String levelFile,
			String agentsFile, String fscFile, Scenario scenario) {
		super(feng);

		_levelFile = levelFile;
		_agentsFile = agentsFile;
		_scenario = scenario;
		_fscFile = fscFile;
		physics = true;
	}

	@Override
	public int getID() {
		return States.MainMenuState.ordinal();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
	}


	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.enter(container, game);
		_enterTime = System.currentTimeMillis();
		
		System.out.println("BlocksworldState enter");
		_gameSystem = new GameSystem(container.getWidth(), container.getHeight(), false);
		_gameSystem.addSubsystem(SubsystemType.PhysicsSubsystem, new PhysicsSubsystem());
		_gameSystem.addSubsystem(SubsystemType.FSCSubsystem, new FSCSubsystem());
		_gameSystem.addSubsystem(SubsystemType.LearningSubsystem, new LearningSubsystem("Physics 1"));
		_gameSystem.disable(SubsystemType.FSCSubsystem);
		FSCSubsystem.system = _gameSystem;
		BlocksworldLoader loader = new BlocksworldLoader(_levelFile,
				_agentsFile, _fscFile, _scenario);
		loader.load(_gameSystem);
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.leave(container, game);

		// Don't forget to remove all of the widgets before moving
		// to the next state. If you forget then the widgets (although not
		// rendered)
		// could actually receive button presses.
		System.out.println("BlocksworldState leave");
		_gameSystem.getSubsystem(SubsystemType.LearningSubsystem).finish();
		_gameSystem.finish();
		_feng.getDisplay().removeAllWidgets();
	}

	public void finish() {
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.setAntiAlias(true);
		g.setBackground(Color.green);

		_gameSystem.render(g);
		_feng.render(container, game, g);

		g.setColor(Color.white);
		if (_gameSystem.getCameraMode()) {
			g.drawString("Input Mode: camera", 10, container.getHeight() - 20);
		} else {
			g.drawString("Input Mode: player", 10, container.getHeight() - 20);
		}

		if (physics) {
			g.drawString("Physics on, Learning on", 10,
					container.getHeight() - 40);
		} else {
			g.drawString("FSC on, Learning on", 10, container.getHeight() - 40);
		}

		// twlInputAdapter.render();
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int millis)
			throws SlickException {
		if (_enterTime + 500 < System.currentTimeMillis())
			_gameSystem.update(millis);
		
		// NOTE: this is not an else if, so update happens, and then we leave 
		if(_enterTime + 6000 < System.currentTimeMillis()){
			game.enterState(States.GameplayState.ordinal());
			return;
		}
		// twlInputAdapter.update();
	}

	private void cameraKey(int key) {
		float delta = 5;
		if (_shiftDown) {
			delta = 10;
		}

		switch (key) {
		case Input.KEY_W:
			_gameSystem.translate(new Vec2(0, -delta));
			break;
		case Input.KEY_S:
			_gameSystem.translate(new Vec2(0, delta));
			break;
		case Input.KEY_A:
			_gameSystem.translate(new Vec2(-delta, 0));
			break;
		case Input.KEY_D:
			_gameSystem.translate(new Vec2(delta, 0));
			break;
		default:

		}
	}

	@Override
	public void keyReleased(int key, char c) {
		super.keyReleased(key, c);

		switch (key) {
		case Input.KEY_F3:
			if (physics) {
				physics = false;
				_gameSystem.disable(SubsystemType.PhysicsSubsystem);
				_gameSystem.enable(SubsystemType.FSCSubsystem);
			} else {
				physics = true;
				_gameSystem.disable(SubsystemType.FSCSubsystem);
				_gameSystem.enable(SubsystemType.PhysicsSubsystem);
			}
			break;
		}
	}

}
