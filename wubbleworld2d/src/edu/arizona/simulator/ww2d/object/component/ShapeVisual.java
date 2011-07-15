package edu.arizona.simulator.ww2d.object.component;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Shape;

import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.utils.SlickShapeUtils;

public class ShapeVisual extends Component {
    private static Logger logger = Logger.getLogger( ShapeVisual.class );

	private Shape _shape;
	private Color _color;

	public ShapeVisual(GameObject owner) {
		super(owner);
		
		_renderPriority = 100;
	}
	
	public void set(Shape shape, Color color) { 
		_shape = shape;
		_color = color;
	}
	
	@Override
	public void update(int elapsed) {

	}

	@Override
	public void render(Graphics g) {
		Vec2 position = _parent.getPosition();
		float heading = (float) Math.toDegrees(_parent.getHeading());

		g.pushTransform();
		g.rotate(position.x, position.y, heading);
		g.translate(position.x, position.y);

		g.setColor(_color);
		g.fill(_shape);
		
		g.setColor(Color.black);
		g.draw(_shape);
		
		g.popTransform();
	}
	
	@Override
	public void fromXML(Element e) { 
		if (e.element("renderPriority") != null) 
			_renderPriority = Integer.parseInt(e.element("renderPriority").attributeValue("value"));

		Element object = e.getParent().getParent();
		float scaleValue = 1.0f;

		Element shapeDef = null;
		Attribute a = e.attribute("fromPhysics");
		if (a != null && "true".equals(a.getText())) { 
			shapeDef = object.element("shapeDef");
			scaleValue = Float.parseFloat(object.attributeValue("scale"));
		} else { 
			shapeDef = e.element("shapeDef");
			boolean doScale = Boolean.parseBoolean(e.attributeValue("scale"));
			if (doScale)  
				scaleValue = Float.parseFloat(object.attributeValue("scale"));
		}
		_shape = SlickShapeUtils.shapeFromXML(shapeDef, scaleValue);
		_color = SlickShapeUtils.colorFromXML(e.element("color"));
	}
}
