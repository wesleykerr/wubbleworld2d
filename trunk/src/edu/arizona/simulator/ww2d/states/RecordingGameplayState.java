package edu.arizona.simulator.ww2d.states;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.composite.Window;
import org.fenggui.layout.FlowLayout;
import org.fenggui.layout.RowLayout;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.util.Spacing;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.Record;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.scenario.Scenario;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.system.PhysicsSubsystem;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.States;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class RecordingGameplayState extends BHGameState {
	private static Logger logger = Logger.getLogger( RecordingGameplayState.class );
	
	private GameSystem _gameSystem;

	private String   _levelFile;
	private String   _agentsFile;
	private Scenario _scenario;
	
	private long _enteredTime;

    public RecordingGameplayState(FengWrapper feng) {
		super(feng);
	}
    
    public void setParams(String levelFile, String agentsFile, Scenario scenario) { 
		_levelFile = levelFile;
		_agentsFile = agentsFile;
		_scenario = scenario;
    }

    @Override
	public int getID() {
		return States.RecordingGameplayState.ordinal();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
	}

	@Override 
	public void enter(GameContainer container, StateBasedGame game) throws SlickException { 
		super.enter(container, game);
		_gameSystem = new GameSystem(container.getWidth(), container.getHeight(), false);
		_gameSystem.addSubsystem(GameSystem.Systems.PhysicsSubystem, new PhysicsSubsystem());
		_gameSystem.loadLevel(_levelFile, _agentsFile, _scenario);
		
		_enteredTime = System.currentTimeMillis();
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.leave(container, game);

		// Don't forget to remove all of the widgets before moving
		// to the next state.  If you forget then the widgets (although not rendered)
		// could actually receive button presses.
		_feng.getDisplay().removeAllWidgets();
		_gameSystem.finish();
	}

	public void finish() { 
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		g.setAntiAlias(true);
		g.setBackground(Color.black);
		
		_gameSystem.render(g);
		_feng.render(container, game, g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int millis) throws SlickException {
		if (_enteredTime + 2000 >= System.currentTimeMillis())
			return;

		long time = System.currentTimeMillis();
		
		// 300000 - 5 minutes
		if (_enteredTime + Record.MILLIS < time) {
			game.enterState(States.ReplayState.ordinal());
			return;
		}
		
		_gameSystem.update(millis);
	}

	@Override
	public void keyPressed(int key, char c) {
		Event keyPressed = new Event(EventType.KEY_PRESSED_EVENT);
		keyPressed.addParameter("key", key);
		EventManager.inst().dispatch(keyPressed);
	}
	
	@Override
	public void keyReleased(int key, char c) { 
		Event keyReleased = new Event(EventType.KEY_RELEASED_EVENT);
		keyReleased.addParameter("key", key);
		EventManager.inst().dispatch(keyReleased);
	}
	
	/**
	 * Layout the GUI for this game state.  We definitely need some sliders to 
	 * modify values, etc.
	 */
	private void layout(Display display) {
		try {
			FengGUI.setTheme(new XMLTheme("data/themes/QtCurve/QtCurve.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IXMLStreamableException e) {
			e.printStackTrace();
		}
		
		Window f = FengGUI.createWindow(false, false);
	    f.setTitle("Parameters");
	    f.getContentContainer().setLayoutManager(new RowLayout(false));
	    f.getContentContainer().getAppearance().setPadding(new Spacing(10,10));

	    f.getContentContainer().addWidget(addParameterSlider(Variable.maxAngularAcceleration, 0, 500));
	    
		Container c = FengGUI.createWidget(Container.class);
		c.setLayoutManager(new FlowLayout(FlowLayout.CENTER));
		
		f.getContentContainer().addWidget(c);
		f.setSize(400, 200);
//	    f.pack();
	    display.addWidget(f);
	}
	

	/**
	 * Creates a FengGUI slider and connects it to a global variable with the same name
	 * @param name
	 * 		the name of the global variable.
	 * @param min
	 * 		the minimum value of the global variable.
	 * @param max
	 * 		the maximum value of the global variable.
	 * @return
	 */
	private Container addParameterSlider(final Variable name, final float min, final float max) { 
//		float startValue = 0;
//		Object obj = BB.inst().getVariableObject(name);
//		if (obj != null)
//			startValue = (Float) obj;
//		
//		Container c = FengGUI.createWidget(Container.class);
//		c.setLayoutManager(new FlowLayout(FlowLayout.CENTER));
//	    
//		Label nameLabel = FengGUI.createWidget(Label.class);
//		nameLabel.getAppearance().setAlignment(Alignment.MIDDLE);
//		nameLabel.setText(name.toString());
//		nameLabel.updateMinSize();
//		c.addWidget(nameLabel);
//		
//		double v = (double) (startValue - min) / (double) (max - min);
//		// Eclipse says it is deprecated, but it still works and you need to call
//		// it this way for some reason.
//		Slider slider = FengGUI.createSlider(true);
//		slider.setValue(v);
//		slider.setMinSize(75, 25);
//	    c.addWidget(slider);
//	    
//	    final Label valueLabel = FengGUI.createWidget(Label.class);
//	    valueLabel.setText(startValue + "");
//		valueLabel.updateMinSize();
//	    c.addWidget(valueLabel);
//
//	    slider.addSliderMovedListener(new ISliderMovedListener() {
//			public void sliderMoved(SliderMovedEvent evt) {
//				double range = max - min;
//				double d = evt.getPosition();
//				float v = (float) (min + (d*range));
//				
//				BB.inst().setVariableObject(name, v);
//				valueLabel.setText(v + "");
//			} 
//	    });
//
//	    c.updateMinSize();
//	    c.pack();
//		return c;
		return null;
	}		
}
