package edu.arizona.simulator.ww2d.replay.awt.component;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.dom4j.Element;

import edu.arizona.simulator.ww2d.replay.awt.AWTGameObject;
import edu.arizona.simulator.ww2d.system.ImageManager;

public class AWTSpriteVisual extends AWTComponent {

	private float _scale;
	private BufferedImage _sprite;

	public AWTSpriteVisual(AWTGameObject owner) {
		super(owner);

		_renderPriority = 100;
	}
	
	public void set(String imageName, float scale) { 
		_sprite = ImageManager.inst().getImageCopy(imageName);
		_scale = scale;
	}


	@Override
	public void render(Graphics2D g) {
		AffineTransform save = new AffineTransform(g.getTransform());

		AffineTransform t = new AffineTransform();
		t.rotate(_parent.getHeading(), _parent.getPosition().x, _parent.getPosition().y);
		t.translate(_parent.getPosition().x, _parent.getPosition().y);
		t.scale(_scale, _scale);
		t.translate(-_sprite.getWidth()/2, -_sprite.getHeight()/2);
		g.drawImage(_sprite, t, null);
		
		g.setTransform(save);
	}
	
	@Override
	public void fromXML(Element e) { 
		if (e.element("renderPriority") != null) 
			_renderPriority = Integer.parseInt(e.element("renderPriority").attributeValue("value"));

		float overallScale = Float.parseFloat(e.getParent().getParent().attributeValue("scale"));

		String imageName = e.element("image").attributeValue("name");
		float scale = Float.parseFloat(e.element("image").attributeValue("scale"));
		set(imageName, scale*overallScale);
	}
}
