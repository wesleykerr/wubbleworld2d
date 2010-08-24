package edu.arizona.simulator.ww2d.object.component;

import org.dom4j.Element;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import edu.arizona.simulator.ww2d.object.GameObject;

public class SpriteVisual extends Component {

	private Image _sprite;

	public SpriteVisual(GameObject owner) {
		super(owner);
		_renderPriority = 100;
	}
	
	public void set(String imageName, float scale) { 
		try { 
			Image tmp = new Image(imageName);
			_sprite = tmp.getScaledCopy(scale);
		} catch (Exception exception) { 
			exception.printStackTrace();
		}
	}

	@Override
	public void update(int elapsed) {

	}

	@Override
	public void render(Graphics g) {
		_sprite.setRotation((float) Math.toDegrees(_parent.getHeading()));
		_sprite.drawCentered(_parent.getPosition().x, _parent.getPosition().y);
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
