package edu.arizona.simulator.ww2d.replay.awt.component;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.replay.awt.AWTGameObject;
import edu.arizona.simulator.ww2d.utils.AWTShapeUtils;

public class AWTShapeVisual extends AWTComponent {
    private static Logger logger = Logger.getLogger( AWTShapeVisual.class );

	private Shape _shape;
	private Color _color;

	public AWTShapeVisual(AWTGameObject owner) {
		super(owner);
		
		_renderPriority = 100;
	}
	
	public void set(Shape shape, Color color) { 
		_shape = shape;
		_color = color;
	}
	
	@Override
	public void render(Graphics2D g) {
		Vec2 position = _parent.getPosition();
		float heading = _parent.getHeading();

		AffineTransform save = new AffineTransform(g.getTransform());

		AffineTransform transform = new AffineTransform(g.getTransform());
		transform.rotate(heading, position.x, position.y);
		transform.translate(position.x, position.y);
		g.setTransform(transform);

		g.setColor(_color);
		g.fill(_shape);
		
		g.setColor(Color.black);
		g.draw(_shape);
		
		g.setTransform(save);
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
		_shape = AWTShapeUtils.shapeFromXML(shapeDef, scaleValue);
		_color = AWTShapeUtils.colorFromXML(e.element("color"));
	}
}
