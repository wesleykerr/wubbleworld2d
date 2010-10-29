package edu.arizona.simulator.ww2d.states;

import org.apache.log4j.Logger;
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
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.gui.TWLInputAdapter;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.States;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class GameplayState extends BHGameState {
	private static Logger logger = Logger.getLogger( GameplayState.class );
	
	private String _levelFile;
	private String _agentsFile;
	private Scenario _scenario;
	
	private GameSystem _gameSystem;

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
		_gameSystem.addSubsystem(GameSystem.Systems.PhysicsSubystem, new PhysicsSubsystem());
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
//		twlInputAdapter.render();
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int millis) throws SlickException {
		if (_enterTime + 5000 < System.currentTimeMillis())
			_gameSystem.update(millis);
//		twlInputAdapter.update();
	}

	@Override
	public void keyPressed(int key, char c) {
		if (key == Input.KEY_F1) {
			Event event = new Event(EventType.CHANGE_CONTROL_EVENT);
			EventManager.inst().dispatch(event);
		}
		
		handleKey(key, true);
	}
	
	@Override
	public void keyReleased(int key, char c) { 
		handleKey(key, false);
	}
	
	private void handleKey(int key, boolean state) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		ValueEntry entry = systemSpace.get(Variable.controlledObject);
		PhysicsObject obj = objectSpace.getCognitiveAgents().get(entry.get(Integer.class));
		
		Event event = null;
		switch (key) { 
		case Input.KEY_W:
			event = new Event(EventType.FORWARD_EVENT);
			event.addRecipient(obj);
			event.addParameter("state", state);
			EventManager.inst().dispatch(event);
			break;
		case Input.KEY_S:
			event = new Event(EventType.BACKWARD_EVENT);
			event.addRecipient(obj);
			event.addParameter("state", state);
			EventManager.inst().dispatch(event);
			break;
		case Input.KEY_A:
			event = new Event(EventType.LEFT_EVENT);
			event.addRecipient(obj);
			event.addParameter("state", state);
			EventManager.inst().dispatch(event);
			break;
		case Input.KEY_D:
			event = new Event(EventType.RIGHT_EVENT);
			event.addRecipient(obj);
			event.addParameter("state", state);
			EventManager.inst().dispatch(event);
			break;
		case Input.KEY_Q:
			event = new Event(EventType.STRAFE_LEFT_EVENT);
			event.addRecipient(obj);
			event.addParameter("state", state);
			EventManager.inst().dispatch(event);
			break;
		case Input.KEY_E:
			event = new Event(EventType.STRAFE_RIGHT_EVENT);
			event.addRecipient(obj);
			event.addParameter("state", state);
			EventManager.inst().dispatch(event);
			break;
		}
	}
}
