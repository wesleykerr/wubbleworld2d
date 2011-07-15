package edu.arizona.simulator.ww2d.utils;

import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;

public class SlickShapeUtils {
    private static Logger logger = Logger.getLogger( SlickShapeUtils.class );

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
		Polygon result = new Polygon();
		List list = e.elements("vertex");
		for (int i = 0; i < list.size(); ++i) { 
			Element tmp = (Element) list.get(i);
			float x = Float.parseFloat(tmp.attributeValue("x"));
			float y = Float.parseFloat(tmp.attributeValue("y"));
			result.addPoint(x*scale, y*scale);
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
		return new Circle(x*scale,y*scale,radius*scale);
	}
}