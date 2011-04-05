package edu.arizona.simulator.ww2d.states;

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
import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.movement.BackwardEvent;
import edu.arizona.simulator.ww2d.events.movement.ForwardEvent;
import edu.arizona.simulator.ww2d.events.movement.LeftEvent;
import edu.arizona.simulator.ww2d.events.movement.RightEvent;
import edu.arizona.simulator.ww2d.events.movement.StrafeLeftEvent;
import edu.arizona.simulator.ww2d.events.movement.StrafeRightEvent;
import edu.arizona.simulator.ww2d.events.system.ChangeControlEvent;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.gui.TWLInputAdapter;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.enums.States;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class GameplayState extends BHGameState {
	private static Logger logger = Logger.getLogger( GameplayState.class );
	
	private String _levelFile;
	private String _agentsFile;
	private Scenario _scenario;
	
	private GameSystem _gameSystem;
	private boolean _shiftDown;

    private LWJGLRenderer lwjglRenderer;
    private ThemeManager theme;
    private GUI gui;
    private Widget root;
    private TWLInputAdapter twlInputAdapter;
    
    private long _enterTime;
    private boolean _update;
    
    public GameplayState(FengWrapper feng, String levelFile, String agentsFile, Scenario scenario) {
		super(feng);
		
		_levelFile = levelFile;
		_agentsFile = agentsFile;
		_scenario = scenario;
	}

    @Override
	public int getID() {
		return States.GameplayState.ordinal();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		_gameSystem = new GameSystem(container.getWidth(), container.getHeight(), false);
		_gameSystem.addSubsystem(SubsystemType.PhysicsSubsystem, new PhysicsSubsystem());
		_gameSystem.loadLevel(_levelFile, _agentsFile, _scenario);
		
//        root = new Widget();
//        root.setTheme("");
//
//        // save Slick's GL state while loading the theme
//        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
//        try {
//                lwjglRenderer = new LWJGLRenderer();
//                theme = ThemeManager.createThemeManager(GameplayState.class.getClassLoader()
//                                .getResource("data/themes/gui/simple.xml"), lwjglRenderer);
//                gui = new GUI(root, lwjglRenderer);
//                gui.applyTheme(theme);
//        } catch (LWJGLException e) {
//                e.printStackTrace();
//        } catch(IOException e){
//                e.printStackTrace();
//        } finally {
//                // restore Slick's GL state
//                GL11.glPopAttrib();
//        }
//
//        // connect input
//        twlInputAdapter = new TWLInputAdapter(gui, container.getInput());
//        container.getInput().addPrimaryListener(twlInputAdapter);		
//
//		new MessageConsole(root);
	}

	@Override 
	public void enter(GameContainer container, StateBasedGame game) throws SlickException { 
		super.enter(container, game);
//		layout(_feng.getDisplay());
		
		_enterTime = System.currentTimeMillis();
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
		_gameSystem.finish();
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		g.setAntiAlias(true);
		g.setBackground(Color.green);
		
		_gameSystem.render(g);
		_feng.render(container, game, g);
		
		g.setColor(Color.white);
		if (_gameSystem.getCameraMode()) { 
			g.drawString("Input Mode: camera", 10, container.getHeight()-20);
		} else { 
			g.drawString("Input Mode: player", 10, container.getHeight()-20);
		}
		
//		twlInputAdapter.render();
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int millis) throws SlickException {
		if (_enterTime + 5000 < System.currentTimeMillis())
			_gameSystem.update(millis);
//		twlInputAdapter.update();
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
	public void keyPressed(int key, char c) {
		if (_gameSystem.getCameraMode()) {
			cameraKey(key);
		} else { 
			handleKey(key, true);
		}
		
		float delta = 0.01f;
		switch (key) {
		case Input.KEY_F1:
			EventManager.inst().dispatch(new ChangeControlEvent());
			break;
		case Input.KEY_LSHIFT:
		case Input.KEY_RSHIFT:
			_shiftDown = true;
			break;
		case Input.KEY_EQUALS:
			if (_shiftDown)
				delta = 0.05f;
			_gameSystem.scale(delta);
			break;
		case Input.KEY_MINUS:
			if (_shiftDown)
				delta = 0.05f;
			_gameSystem.scale(-delta);
			break;
		}
	}
	
	@Override
	public void keyReleased(int key, char c) { 
		if (!_gameSystem.getCameraMode()) {
			handleKey(key, false);
		} 
		
		switch (key) { 
		case Input.KEY_F2:
			_gameSystem.flipCameraMode();
			break;
		case Input.KEY_LSHIFT:
		case Input.KEY_RSHIFT:
			_shiftDown = false;
			break;
		}
	}
	
	private void handleKey(int key, boolean state) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		ValueEntry entry = systemSpace.get(Variable.controlledObject);
		PhysicsObject obj = objectSpace.getCognitiveAgents().get(entry.get(Integer.class));
		
		switch (key) { 
		case Input.KEY_W:
			EventManager.inst().dispatch(new ForwardEvent(state, obj));
			break;
		case Input.KEY_S:
			EventManager.inst().dispatch(new BackwardEvent(state, obj));
			break;
		case Input.KEY_A:
			EventManager.inst().dispatch(new LeftEvent(state, obj));
			break;
		case Input.KEY_D:
			EventManager.inst().dispatch(new RightEvent(state, obj));
			break;
		case Input.KEY_Q:
			EventManager.inst().dispatch(new StrafeLeftEvent(state, obj));
			break;
		case Input.KEY_E:
			EventManager.inst().dispatch(new StrafeRightEvent(state, obj));
			break;
		}
	}
}
