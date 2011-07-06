package edu.arizona.simulator.ww2d.experimental.blocksworld.objects;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jbox2d.dynamics.World;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.spawn.CreatePhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class BlockFactory {
	
	int _count;
	
	public BlockFactory() {
		_count = 0;
	}
	
	public void createBox(
			float x, float y, float angle, float width,
			float linearDamping, float angularDamping, 
			float density, float friction, float restitution,boolean hasMass) {
		Space systemSpace = Blackboard.inst().getSpace("system");
		World world = systemSpace.get(Variable.physicsWorld).get(World.class);
		
		Element blockDef = blockDef(
				x,y,angle,width,width,
				linearDamping, angularDamping,
				density,friction,restitution, hasMass);
		
		Event e = new CreatePhysicsObject(world, blockDef);
		EventManager.inst().dispatchImmediate(e);
	}
	
	public void createRectangle(float x, float y, float angle, float width, float height,
			float linearDamping, float angularDamping, 
			float density, float friction, float restitution,boolean hasMass){
		
	}

	Element blockDef(
			float x, float y, float angle, float width, float height,
			float linearDamping, float angularDamping, 
			float density, float friction, float restitution,boolean hasMass) {
		
		Document document = DocumentHelper.createDocument();
		Element template = document.addElement( "physicsObject" );
		
		template.addAttribute("name", "block-"+_count++);
		template.addAttribute("renderPriority", "100"); // 2 ?
		template.addAttribute("type", "dynamic");
		template.addAttribute("hasMass", Boolean.toString(hasMass));
		
		// create location for body
		template.addElement("bodyDef")
			.addAttribute("x", Float.toString(x))
			.addAttribute("y", Float.toString(y))
			.addAttribute("angle", Float.toString(angle))
			.addAttribute("linearDamping", Float.toString(linearDamping))
			.addAttribute("angularDamping", Float.toString(angularDamping));
		
		// create shape definition, with physics
		Element shape = template.addElement("shapeDef")
			.addAttribute("type", "polygon")
			.addAttribute("density", Float.toString(density))
			.addAttribute("friction", Float.toString(friction))
			.addAttribute("restitution", Float.toString(restitution));
		
		// add vertices for shape
		float w = width/2;
		float h = height / 2;
		shape.addElement("vertex")
			.addAttribute("x", Float.toString(-w))
			.addAttribute("y", Float.toString(-h));
		shape.addElement("vertex")
			.addAttribute("x", Float.toString(w))
			.addAttribute("y", Float.toString(-h));
		shape.addElement("vertex")
			.addAttribute("x", Float.toString(w))
			.addAttribute("y", Float.toString(h));
		shape.addElement("vertex")
			.addAttribute("x", Float.toString(-w))
			.addAttribute("y", Float.toString(h));
		
		// Add color - default blue
		Element components = template.addElement("components");
		Element c = components.addElement("component")
			.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.ShapeVisual")
			.addAttribute("fromPhysics", "true");
		c.addElement("color")
			.addAttribute("r", "0.0")
			.addAttribute("g", "0.0")
			.addAttribute("b", "1.0")
			.addAttribute("a", "1.0");
		
		return template;
	}
	
}
