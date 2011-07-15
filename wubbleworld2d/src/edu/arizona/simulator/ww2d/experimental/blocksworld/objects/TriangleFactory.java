package edu.arizona.simulator.ww2d.experimental.blocksworld.objects;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jbox2d.dynamics.World;
import org.newdawn.slick.geom.Vector2f;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.spawn.CreatePhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class TriangleFactory {

	int _count;

	public TriangleFactory() {
		_count = 0;
	}

	public void create(Vector2f v1, Vector2f v2, Vector2f v3, float angle,
			float linearDamping, float angularDamping, float density,
			float friction, float restitution, boolean hasMass) {
		Space systemSpace = Blackboard.inst().getSpace("system");
		World world = systemSpace.get(Variable.physicsWorld).get(World.class);

		Element triDef = triDef(v1, v2, v3, angle, linearDamping,
				angularDamping, density, friction, restitution, hasMass);
		Event e = new CreatePhysicsObject(world, triDef);
		EventManager.inst().dispatchImmediate(e);
	}

	Element triDef(Vector2f v1, Vector2f v2, Vector2f v3, float angle,
			float linearDamping, float angularDamping, float density,
			float friction, float restitution, boolean hasMass) {

		Document document = DocumentHelper.createDocument();
		Element template = document.addElement("physicsObject");

		template.addAttribute("name", "triangle-" + _count++);
		template.addAttribute("renderPriority", "100"); // 2 ?
		template.addAttribute("type", "dynamic");
		template.addAttribute("hasMass", Boolean.toString(hasMass));

		// create location for body
		// Uses centroid of triangle
		Vector2f centroid = new Vector2f((v1.x + v2.x + v3.x) / 3,
				(v1.y + v2.y + v3.y) / 3);
		template.addElement("bodyDef")
				.addAttribute("x", Float.toString(centroid.x))
				.addAttribute("y", Float.toString(centroid.y))
				.addAttribute("angle", Float.toString(angle))
				.addAttribute("linearDamping", Float.toString(linearDamping))
				.addAttribute("angularDamping", Float.toString(angularDamping));

		// System.out.println(template.asXML());

		// create shape definition, with physics
		Element shape = template.addElement("shapeDef")
				.addAttribute("type", "polygon")
				.addAttribute("density", Float.toString(density))
				.addAttribute("friction", Float.toString(friction))
				.addAttribute("restitution", Float.toString(restitution));
		
		// Test ordering
		 if(!counterClockwiseTest(v1,v2,v3)){
			 Vector2f temp = v2;
			 v2 = v3;
			 v3 = temp;
		 }
		 if(!counterClockwiseTest(v1,v2,v3)){
			 Vector2f temp = v1;
			 v1 = v2;
			 v2 = temp;
		 }
		 if(!counterClockwiseTest(v1,v2,v3)){
			 Vector2f temp = v2;
			 v2 = v3;
			 v3 = temp;
		 }

		// add vertices for shape
		shape.addElement("vertex")
				.addAttribute("x", Float.toString(- centroid.x + v1.x))
				.addAttribute("y", Float.toString(- centroid.y + v1.y));
		shape.addElement("vertex")
				.addAttribute("x", Float.toString(- centroid.x + v2.x))
				.addAttribute("y", Float.toString(- centroid.y + v2.y));
		shape.addElement("vertex")
				.addAttribute("x", Float.toString(- centroid.x + v3.x))
				.addAttribute("y", Float.toString(- centroid.y + v3.y));
		System.out.println(shape.asXML());

		// Add color - default blue
		Element components = template.addElement("components");
		Element c = components
				.addElement("component")
				.addAttribute("className",
						"edu.arizona.simulator.ww2d.object.component.ShapeVisual")
				.addAttribute("fromPhysics", "true");
		c.addElement("color").addAttribute("r", "0.0").addAttribute("g", "0.0")
				.addAttribute("b", "1.0").addAttribute("a", "1.0");

		return template;
	}
	
	public static boolean counterClockwiseTest(Vector2f v1, Vector2f v2, Vector2f v3){
		 return  ((v2.x-v1.x) * (v2.y+v1.y) + (v3.x - v2.x) * (v3.y + v2.y) + (v1.x - v3.x) * (v1.y + v2.y)) < 0f;
	}

}
