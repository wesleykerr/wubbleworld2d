package edu.arizona.simulator.ww2d.replay.awt.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.object.component.InternalComponent;
import edu.arizona.simulator.ww2d.replay.awt.AWTGameObject;
import edu.arizona.simulator.ww2d.system.ImageManager;
import edu.arizona.simulator.ww2d.utils.enums.EmotionEnum;

public class AWTInternalComponent extends AWTComponent {
    private static Logger logger = Logger.getLogger( InternalComponent.class );

    private BufferedImage _speechBubble;
    private BufferedImage _emotionImage;
    
    private String _currentEmotion;

    private int _numRenders;
    private int _maxRenders;
    
	public AWTInternalComponent(AWTGameObject obj) { 
		super(obj);
		
		// preload all of the images so that we don't suffer
		// any slow down from loading them one at a time.
		for (EmotionEnum emotion : EmotionEnum.values()) { 
			ImageManager.inst().getImage("data/images/emotion/" + emotion + ".png");
		}
		
		_speechBubble = ImageManager.inst().getImage("data/images/speech-bubble.png");
	    // Flip the image horizontally
	    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
	    tx.translate(-_speechBubble.getWidth(), 0);
	    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	    _speechBubble = op.filter(_speechBubble, null);
	    
	    _numRenders = 0;
	    _maxRenders = 200;
	}
	
	@Override 
	public void render(Graphics2D g) { 
		renderEnergy(g);
//		renderEmotion(g);
	}
	
	private void renderEnergy(Graphics2D g) { 
		Vec2 position = _parent.getPosition();
		float heading = _parent.getHeading();
		
		float energy = _parent.getUserData("energy", Float.class);
		float energyMax = _parent.getUserData("energyMax", Float.class);
		
		AffineTransform save = new AffineTransform(g.getTransform());

		AffineTransform transform = new AffineTransform(g.getTransform());
		transform.translate(position.x, position.y);
		g.setTransform(transform);

		
		Font f = new Font("Courier", Font.BOLD, 16);
		g.setFont(f);
		
		Color white = new Color(255,255,255,255);
		
		g.setColor(white);
		g.drawString("Energy " + ((int) energy), -50, -20); 

		g.setTransform(save);

		// first draw a black bar that represents 100% of the agent's health
		// then draw a red bar that represents what percent of the agent's health remains
		// each tick is worth a single pixel.
//		float halfEnergy = energyMax / 2;
//		float quarterEnergy = halfEnergy / 2;
//		Vec2 position = _parent.getPosition().sub(new Vec2(quarterEnergy,13));
//		
//		Rectangle2D energyBackground = new Rectangle2D.Float(position.x-1, position.y-1, halfEnergy, 8);
//		Rectangle2D energyForeground = new Rectangle2D.Float(position.x, position.y, energy*0.5f, 7);
//
//		g.setColor(Color.black);
//		g.fill(energyBackground);
//		
//		g.setColor(Color.blue);
//		g.fill(energyForeground);
	}
	
	private void renderEmotion(Graphics2D g) { 
		String emotion = _parent.getUserData("emotion", String.class);
		if (_currentEmotion == null || !_currentEmotion.equals(emotion)) { 
			logger.debug(_parent.getName() + " changing emotion from " + _currentEmotion + " to " + emotion + " " + _numRenders);
			_currentEmotion = emotion;
			_emotionImage = ImageManager.inst().getImage("data/images/emotion/" + emotion + ".png");
			_numRenders = 0;
		}
		
		if (_numRenders > _maxRenders)
			return;
		
		
		AffineTransform save = new AffineTransform(g.getTransform());

		AffineTransform transform = new AffineTransform(g.getTransform());
		transform.translate(_parent.getPosition().x, _parent.getPosition().y);
		g.setTransform(transform);
		
		float speechScale = 0.45f;
		AffineTransform t = AffineTransform.getScaleInstance(speechScale, speechScale);
		t.translate(-_speechBubble.getWidth(), -_speechBubble.getHeight());
		g.drawImage(_speechBubble, t, null);		
		
		Vec2 offset = new Vec2();
		offset.x = -_speechBubble.getWidth() * 1.3f;
		offset.y = -_speechBubble.getHeight() * 1.2f;
			
		float emotionScale = 0.3f;
		t = AffineTransform.getScaleInstance(emotionScale, emotionScale);
		t.translate(offset.x, offset.y);
		g.drawImage(_emotionImage, t, null);

		g.setTransform(save);
		
		++_numRenders;
	}

	@Override
	public void fromXML(Element e) {

	}
}
