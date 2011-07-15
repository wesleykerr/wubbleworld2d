package edu.arizona.simulator.ww2d.gui;

import org.fenggui.Display;
import org.fenggui.binding.render.Binding;
import org.fenggui.binding.render.lwjgl.EventHelper;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.event.mouse.MouseButton;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.event.mouse.MouseReleasedEvent;
import org.newdawn.slick.Game;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;
import org.newdawn.slick.opengl.SlickCallable;
/**
 *
 */
public class FengWrapper implements InputListener {

	private Display _fengDisplay;
	
	private GameContainer _container;
	private Input _input;

	public FengWrapper(GameContainer container) { 
		_container = container;
		_container.getInput().addPrimaryListener(this);
		_container.getInput().enableKeyRepeat();

		LWJGLBinding binding = new LWJGLBinding();
		_fengDisplay = new Display(binding);
		_fengDisplay.setDepthTestEnabled(true);

		Binding.getInstance().setUseClassLoader(true);
	}
	
	/**
	 * FengGUI has it's own handle to the OpenGL context.  Anytime
	 * we access the OpenGL context it must be done safely.  This is done
	 * by wrapping it in calls to SlickCallable.
	 * @param container
	 * @param game
	 * @param g
	 */
	public void render(GameContainer container, Game game, Graphics g) { 
		SlickCallable.enterSafeBlock();
		_fengDisplay.display();
		SlickCallable.leaveSafeBlock();
	}

	private MouseButton slickButtonToFeng(int button) { 
		MouseButton pressed;
		switch (button) {
		case (1):
			pressed = MouseButton.RIGHT;
		break;
		case (2):
			pressed = MouseButton.MIDDLE;
		break;
		default:
			pressed = MouseButton.LEFT;
			break;
		}
		return pressed;
	}

	public void controllerButtonPressed(int controller, int button) { }
	public void controllerButtonReleased(int controller, int button) { }
	public void controllerDownPressed(int controller) { }
	public void controllerDownReleased(int controller) { }
	public void controllerLeftPressed(int controller) { }
	public void controllerLeftReleased(int controller) { }
	public void controllerRightPressed(int controller) { }
	public void controllerRightReleased(int controller) { }
	public void controllerUpPressed(int controller) { }
	public void controllerUpReleased(int controller) { }

	public void inputEnded() { }

	public boolean isAcceptingInput() {
		return true;
	}

	public void keyPressed(int key, char c) {
		_fengDisplay.fireKeyPressedEvent(EventHelper.mapKeyChar(), EventHelper.mapEventKey());
		_fengDisplay.fireKeyTypedEvent(EventHelper.mapKeyChar());
	}

	public void keyReleased(int key, char c) {
		_fengDisplay.fireKeyReleasedEvent(EventHelper.mapKeyChar(), EventHelper.mapEventKey());
	}
	
	public void mouseDragged(int oldx, int oldy, int newx, int newy) { 
		_fengDisplay.fireMouseDraggedEvent(newx, _container.getHeight()-newy, MouseButton.LEFT);
	}

	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		_fengDisplay.fireMouseMovedEvent(newx, _container.getHeight()-newy);
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
//		_fengDisplay.fireMouseClickEvent(x, _container.getHeight()-y, slickButtonToFeng(button), clickCount);
	}
	
	public void mousePressed(int button, int x, int y) {
		MousePressedEvent event = _fengDisplay.fireMousePressedEvent(x, _container.getHeight()-y, slickButtonToFeng(button));
		if (event.isUIHit()) { 
			_container.getInput().consumeEvent();
		}
	}

	public void mouseReleased(int button, int x, int y) {
		MouseReleasedEvent event = _fengDisplay.fireMouseReleasedEvent(x, _container.getHeight()-y, slickButtonToFeng(button));
		if (event.isUIHit()) { 
			_container.getInput().consumeEvent();
		}
	}

	public void mouseWheelMoved(int change) {
		int x = _container.getInput().getMouseX();
		int y = _container.getInput().getMouseY();
		
		_fengDisplay.fireMouseWheel(x, _container.getHeight()-y, change > 0, 1, change);
	}

	/**
	 * Return the FengGUI display so that we can layout 
	 * the items on the canvas.
	 * @return
	 */
	public Display getDisplay() { 
		return _fengDisplay;
	}
	
	public void setInput(Input input) {
		_input = input;
	}

	@Override
	public void inputStarted() {
		// TODO Auto-generated method stub
		
	}

} 