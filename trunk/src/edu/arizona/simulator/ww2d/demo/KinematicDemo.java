package edu.arizona.simulator.ww2d.demo;

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
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.component.ShapeVisual;
import edu.arizona.simulator.ww2d.object.component.SpriteVisual;
import edu.arizona.simulator.ww2d.object.component.kinematics.KinematicsComponent;
import edu.arizona.simulator.ww2d.states.BHGameState;
import edu.arizona.simulator.ww2d.states.GameplayState;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.system.GameSystem;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class KinematicDemo extends BHGameState {
    private static Logger logger = Logger.getLogger( GameplayState.class );
	
	private GameSystem _gameSystem;

	private GameObject _character;

	private Vec2 _target;
	private KinematicsComponent.Behavior _behavior = KinematicsComponent.Behavior.seek;

	public KinematicDemo(FengWrapper feng) { 
		super(feng);
	}
	
	@Override
	public int getID() {
		return 1;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		_gameSystem = new GameSystem(container.getWidth(), container.getHeight(), false);
		
		_character = new GameObject("character", ObjectType.visual, 100);
		ShapeVisual v1 = new ShapeVisual(_character);
		v1.setRenderPriority(99);
		v1.set(new Circle(0,0,10f), Color.red);
		_character.addComponent(v1);

		SpriteVisual v2 = new SpriteVisual(_character);
		v2.setRenderPriority(100);
		v2.set("data/images/half-circle.png", 0.625f);
		_character.addComponent(v2);

		_character.addComponent(new KinematicsComponent(_character));
		_character.setPosition(new Vec2(200,200));
		
		ObjectSpace objSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		objSpace.add(_character);
		
		_target = new Vec2(0,0);
	}	

	@Override 
	public void enter(GameContainer container, StateBasedGame game) throws SlickException { 
		super.enter(container, game);
//		layout(_feng.getDisplay());
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
	
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		g.setAntiAlias(true);
		g.setBackground(Color.black);
		renderGrid(container, g, 20);

		g.setColor(Color.blue);
		g.fillRect(_target.x-2, _target.y-2, 4f, 4f);
		
		_gameSystem.render(g);
		_feng.render(container, game, g);
	}

	private void renderGrid(GameContainer container, Graphics g, int width) {
		g.setColor(Color.lightGray);
		for (int i = 0; i < container.getWidth(); i+=width) { 
			if (i % 100 == 0) 
				g.setLineWidth(2);
			else 
				g.setLineWidth(1);
			g.drawLine(i, 0, i, container.getHeight());
		}
		
		for (int i = 0; i < container.getHeight(); i+=width) { 
			if (i % 100 == 0) 
				g.setLineWidth(2);
			else 
				g.setLineWidth(1);
			g.drawLine(0, i, container.getWidth(), i);
		}
		
	}	
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int millis) throws SlickException {
		_gameSystem.update(millis);
		
		Vec2 pos = _character.getPosition();
		if (pos.x < 0)
			pos.x += container.getWidth();
		if (pos.x > container.getWidth())
			pos.x -= container.getWidth();

		if (pos.y < 0)
			pos.y += container.getHeight();
		if (pos.y > container.getHeight())
			pos.y -= container.getHeight();
	}
	
	@Override
	public void finish() { 
		
	}
	
	private void dispatchKinematic() { 
		Event event = new Event(EventType.KINEMATIC_EVENT);
		event.addRecipient(_character);
		event.addParameter("target", _target);
		event.addParameter("movement", _behavior);
		
		EventManager.inst().dispatch(event);
	}
	

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		if (button == Input.MOUSE_LEFT_BUTTON) { 
			_target = new Vec2(x, y);
			dispatchKinematic();
		}
		super.mouseClicked(button, x, y, clickCount);
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO Auto-generated method stub
		super.mousePressed(button, x, y);
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		// TODO Auto-generated method stub
		super.mouseReleased(button, x, y);
	}

	@Override
	public void keyPressed(int key, char c) {
		Event keyPressed = new Event(EventType.KEY_PRESSED_EVENT);
		keyPressed.addParameter("key", key);
		EventManager.inst().dispatch(keyPressed);
		
		switch (key) { 
		case Input.KEY_S: 
			_behavior = KinematicsComponent.Behavior.seek;
			dispatchKinematic();
			break;
		case Input.KEY_W:
			_behavior = KinematicsComponent.Behavior.wander;
			dispatchKinematic();
			break;
		case Input.KEY_A:
			_behavior = KinematicsComponent.Behavior.arrive;
			dispatchKinematic();
			break;
		case Input.KEY_F:
			_behavior = KinematicsComponent.Behavior.flee;
			dispatchKinematic();
			break;
		}
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
