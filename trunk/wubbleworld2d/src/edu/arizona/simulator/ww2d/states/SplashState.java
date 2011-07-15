package edu.arizona.simulator.ww2d.states;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.utils.enums.States;

public class SplashState extends BHGameState {

	private Image _splashImage;
	private int   _timer;
	
	private int   _transitionTo;
	
	public SplashState(FengWrapper feng, int transitionTo) { 
		super(feng);
		_transitionTo = transitionTo;
	}
	
	@Override
	public int getID() {
		return States.SplashState.ordinal();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		Image initial = new Image("data/images/splash.png");		
		
		float imgWidth = (float) initial.getWidth();
		float screenWidth = (float) container.getWidth();
		
		float ratio = screenWidth / imgWidth;
		if (ratio >= 1) { 
			_splashImage = initial;
		} else { 
			_splashImage = initial.getScaledCopy(ratio);
		}
		_timer = 2000;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics graphics) throws SlickException {
		float centerX = container.getWidth() / 2.0f;
		float centerY = container.getHeight() / 2.0f;

		graphics.setBackground(Color.white);
		_splashImage.drawCentered(centerX, centerY);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int millis) throws SlickException {
		_timer -= millis;
		
		if (_timer < 0) { 
			game.enterState(_transitionTo, new FadeOutTransition(), new FadeInTransition());
		}
	}
	
	@Override
	public void finish() { 
		
	}
}
