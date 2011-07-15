package edu.arizona.simulator.ww2d.states;

import java.io.IOException;

import org.fenggui.Button;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.layout.StaticLayout;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.util.Point;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class MainMenuState extends BHGameState {

	private int _centerX;
	private int _centerY;
	
	private StateBasedGame _game;
	private Image _backgroundImage;
	
	public MainMenuState(FengWrapper feng) { 
		super(feng);
	}
	
	@Override
	public int getID() {
		return States.MainMenuState.ordinal();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		_game = game;
		_backgroundImage = new Image("data/images/main-screen.png");
		
		_centerX = container.getWidth() / 2;
		_centerY = container.getHeight() / 2;
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.enter(container, game);

		// This will add all of the widgets for your GUI to the
		// Display for rendering and capturing input..
		layout(_feng.getDisplay(), container, game);
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
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		// other rendering would go here (for instance if we wanted to put a 
		// pretty background we would render it here.
		g.setBackground(Color.white);

//		_backgroundImage.draw(0,0);
		_feng.render(container, game, g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int elapsed)
			throws SlickException {
		// If we had something to update, it would go here.
	}
	
	@Override 
	public void finish() { 
		
	}

	private void layout(Display display, final GameContainer container, final StateBasedGame game) {
		try {
			FengGUI.setTheme(new XMLTheme("data/themes/QtCurve/QtCurve.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IXMLStreamableException e) {
			e.printStackTrace();
		}
		display.setLayoutManager(new StaticLayout());

		Button btn = FengGUI.createWidget(Button.class);
		btn.setText("start");
	    btn.setPosition(new Point(350, _centerY));
		btn.setSize(100, 25);
		btn.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(Object obj, ButtonPressedEvent arg0) {
				_game.enterState(States.GameplayState.ordinal());
			} 
		});	
		display.addWidget(btn);
		
		btn = FengGUI.createWidget(Button.class);
		btn.setText("exit");
	    btn.setPosition(new Point(350, _centerY-25));
		btn.setSize(100, 25);
		btn.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(Object obj, ButtonPressedEvent arg0) {
				container.exit();
			} 
		});	
		display.addWidget(btn);
	}
}
