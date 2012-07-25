package edu.arizona.simulator.ww2d.experimental.blocksworld.objects;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class FallFactory {
	private int count = 0;
	public FallFactory(){
		
	}
	
	public void generateXML(){
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("level");
		root.addElement("physicsScale").addAttribute("value", "12");
		root.addElement("physics");
		root.element("physics").addElement("aabb");
		root.element("physics").element("aabb").addElement("min").addAttribute("x", "-2");
		root.element("physics").element("aabb").element("min").addAttribute("y", "-2");
		
		root.element("physics").element("aabb").addElement("max").addAttribute("x", "52");
		root.element("physics").element("aabb").element("max").addAttribute("y", "52");
		root.element("physics").addElement("gravity").addAttribute("x", "0");
		root.element("physics").element("gravity").addAttribute("y", "9.81");
		root.element("physics").addElement("doSleep").addAttribute("value","true");
		
		root.addElement("foodSubsystem").addAttribute("value","false");
		
		Element objects = root.addElement("objects");
		objects.addElement("gameObject").addAttribute("name", "background");
		objects.element("gameObject").addAttribute("renderPriority", "0");
		objects.element("gameObject").addAttribute("type", "visual");
		objects.element("gameObject").addElement("components");
		
		objects.element("gameObject").element("components").addElement("component");
		objects.element("gameObject").element("components").element("component").addAttribute("className", "edu.arizona.simulator.ww2d.object.component.ShapeVisual");
		objects.element("gameObject").element("components").element("component").addAttribute("scale","true");
		objects.element("gameObject").element("components").element("component").addElement("shapeDef").addAttribute("type", "polygon");
		//objects.element("gameObject").element("components").element("component").addElement("shapeDef").addElement();
		
		
	}

}