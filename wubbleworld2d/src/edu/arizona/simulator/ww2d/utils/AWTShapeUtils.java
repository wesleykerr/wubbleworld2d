package edu.arizona.simulator.ww2d.utils;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;

public class AWTShapeUtils {
    private static Logger logger = Logger.getLogger( AWTShapeUtils.class );

	public static Color colorFromXML(Element e) { 
		float r = Float.parseFloat(e.attributeValue("r"));
		float g = Float.parseFloat(e.attributeValue("g"));
		float b = Float.parseFloat(e.attributeValue("b"));
		float a = Float.parseFloat(e.attributeValue("a"));
		
		return new Color(r,g,b,a);
	}
	
	public static Shape shapeFromXML(Element e, float scale) { 
		String type = e.attributeValue("type");
		if ("polygon".equals(type))
			return polygonDef(e, scale);
		else if ("circle".equals(type)) 
			return circleDef(e, scale);
		else	
			throw new RuntimeException("Unknown shape type in shapeDef");
	}
		
	private static Shape polygonDef(Element e, float scale) { 
		GeneralPath result = new GeneralPath();

		Vec2 origin = new Vec2();
		List list = e.elements("vertex");
		for (int i = 0; i < list.size(); ++i) { 
			Element tmp = (Element) list.get(i);
			float x = scale*Float.parseFloat(tmp.attributeValue("x"));
			float y = scale*Float.parseFloat(tmp.attributeValue("y"));
			if (i == 0) {
				result.moveTo(x, y);
				origin.set(x,y);
			} else { 
				result.lineTo(x, y);
			}
		}
		return result;
	}
		
	private static Shape circleDef(Element e, float scale) { 
		Element tmp = e.element("localPosition");
		float x = 0; 
		float y = 0;
		if (tmp != null) {
			x = Float.parseFloat(tmp.attributeValue("x"));
			y = Float.parseFloat(tmp.attributeValue("y"));
		}
			
		float radius = Float.parseFloat(e.attributeValue("radius"));
		float d = 2 * radius;
		return new Ellipse2D.Float((x-radius)*scale,(y-radius)*scale,d*scale, d*scale);
	}
}