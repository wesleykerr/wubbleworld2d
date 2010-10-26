package edu.arizona.simulator.ww2d.object.component;

import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.utils.SlickGlobals;

public class TextVisual extends Component {

	private String _varName;
	private Vec2 _offset;
	
	public TextVisual(GameObject obj) { 
		super(obj);

		_renderPriority = 100;
		_varName = null;
		_offset = new Vec2(0,0);
	}
	
	public void set(String varName, Vec2 offset) { 
		_varName = varName;
		_offset = offset;
	}
	
	@Override
	public void update(int elapsed) {

	}

	@Override
	public void render(Graphics g) {
		Object value = _parent.getUserData(_varName);
		if (value != null) {
			Vec2 offset = _parent.getPosition().add(_offset);
			SlickGlobals.textFont.drawString(offset.x, offset.y, value.toString(), Color.black);
		}
	}
	
	@Override
	public void fromXML(Element e) { 
		if (e.element("renderPriority") != null) 
			_renderPriority = Integer.parseInt(e.element("renderPriority").attributeValue("value"));

		String name = e.attributeValue("varName");
		float x = Float.parseFloat(e.attributeValue("offset-x"));
		float y = Float.parseFloat(e.attributeValue("offset-y"));
		
		set(name, new Vec2(x,y));
	}
}
