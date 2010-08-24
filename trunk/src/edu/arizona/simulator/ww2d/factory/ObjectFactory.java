package edu.arizona.simulator.ww2d.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.FilterData;
import org.jbox2d.collision.MassData;
import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.collision.shapes.PolygonDef;
import org.jbox2d.collision.shapes.ShapeDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

import edu.arizona.simulator.ww2d.blackboard.AgentHelper;
import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.Component;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class ObjectFactory {
    private static Logger logger = Logger.getLogger( ObjectFactory.class );

    private static ObjectFactory _inst;
    
    private ObjectFactory() { 
    	// register for create object and create physics object events.
    	EventManager.inst().registerForAll(EventType.CREATE_GAME_OBJECT, new EventListener() {
			@Override
			public void onEvent(Event e) {
				makeGameObject((Element) e.getValue("element"));
			} 
    	});
    	
    	EventManager.inst().registerForAll(EventType.CREATE_PHYSICS_OBJECT, new EventListener() {
			@Override
			public void onEvent(Event e) {
				makePhysicsObject((Element) e.getValue("element"));
			} 
    	});
    	
    }
    
    public static ObjectFactory inst() { 
    	if (_inst == null) 
    		_inst = new ObjectFactory();
    	return _inst;
    }
    
    /**
     * Construct a new game object from the parameters specified within
     * the XML element given.
     * @param e
     */
    public void makeGameObject(Element e) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);
		e.addAttribute("scale", scale+"");
    	    	
    	String name = e.attributeValue("name");
    	int renderPriority = Integer.parseInt(e.attributeValue("renderPriority"));
    	ObjectType objType = ObjectType.valueOf(e.attributeValue("type"));
		
		logger.debug("Creating: " + name + " renderPriority: " + renderPriority);
		GameObject obj = new GameObject(name, objType, renderPriority);
		addComponents(e, obj);
		
		objectSpace.add(obj);
    }
    
    /**
     * Construct a physics object from the parameters specified within
     * the XML element given.
     * @param e
     */
	public void makePhysicsObject(Element e) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

		// The Element in the level file knows nothing about the scale, but
		// we will add in in order to make sure that the visual components
		// are put into the correct place.
		World physics = systemSpace.get(Variable.physicsWorld).get(World.class);
		AABB bounding = physics.getWorldAABB();
		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);
		e.addAttribute("scale", scale+"");
		
		String name = e.attributeValue("name");
    	int renderPriority = Integer.parseInt(e.attributeValue("renderPriority"));
    	ObjectType objType = ObjectType.valueOf(e.attributeValue("type"));
    	boolean hasMass = false;
    	if (e.attribute("hasMass") != null)
    		hasMass = Boolean.parseBoolean(e.attributeValue("hasMass"));
    	
		logger.debug("Creating: " + name + " renderPriority: " + renderPriority);
		PhysicsObject obj = new PhysicsObject(name, objType, renderPriority);
		
		// what is the type ... static or dynamic
		BodyDef bDef = parseBodyDefinition(e.element("bodyDef"), bounding);
		ShapeDef sDef = parseShapeDefinition(e.element("shapeDef"));

		sDef.userData = obj;

		Body b = physics.createBody(bDef);
		b.createShape(sDef);
		
		if (hasMass) { 
			b.setMassFromShapes();
			
			Element tmp = e.element("MassData");
			if (tmp != null) { 
				MassData m = new MassData();
				Element center = tmp.element("center");
				float x = Float.parseFloat(center.attributeValue("x"));
				float y = Float.parseFloat(center.attributeValue("y"));
				m.center.set(x,y);
				m.I = Float.parseFloat(tmp.element("I").attributeValue("value"));
				m.mass = Float.parseFloat(tmp.element("mass").attributeValue("value"));
				b.setMass(m);
			}
		}
		
		obj.setBody(b);
		b.setUserData(obj);

		// initialize the AgentSpace if we have specified it.
		// the parameter is actually the method we want to call in the AgentHelper class
    	if (e.attribute("initAgent") != null) {
    		String methodName = e.attributeValue("initAgent");
    		try { 
        		Method m = AgentHelper.class.getMethod(methodName, PhysicsObject.class);
        		m.invoke(null, obj);
    		} catch (Exception exception) { 
    			exception.printStackTrace();
    		}
//   			AgentHelper.initAgent(obj);
    	}
		
		addComponents(e, obj);
		objectSpace.add(obj);
	}

	
	private BodyDef parseBodyDefinition(Element e, AABB aabb) { 
		BodyDef b = new BodyDef();
		// set up a default body definition and then modify to be
		// specific to this element.
		b.position = new Vec2(0,0);
		b.linearDamping = 1f;
		b.angularDamping = 1f;
		b.angle = 0;
		
		if (e.attribute("random") != null && Boolean.parseBoolean(e.attributeValue("random"))) { 
			// need a better way for this, but hell.
			float radius = Float.parseFloat(e.attributeValue("radius"));
			Vec2 min = new Vec2(0.5f, 0.5f);
			Vec2 max = new Vec2(99.5f, 99.5f);
			Vec2 delta = max.sub(min).sub(new Vec2(2*radius, 2*radius));
			
			b.position.x = (radius + min.x) + (MathUtils.random.nextFloat() * delta.x);
			b.position.y = (radius + min.y) + (MathUtils.random.nextFloat() * delta.y);
			
			b.angle = (float) Math.toRadians(MathUtils.random.nextInt(360));
			
		} else { 
			if (e.attribute("x") != null) 
				b.position.x = Float.parseFloat(e.attributeValue("x"));
			
			if (e.attribute("y") != null) 
				b.position.y = Float.parseFloat(e.attributeValue("y"));
		
			if (e.attribute("angle") != null) 
				b.angle = Float.parseFloat(e.attributeValue("angle"));
		}
		
		if (e.attribute("linearDamping") != null) 
			b.linearDamping = Float.parseFloat(e.attributeValue("linearDamping"));
		
		if (e.attribute("angularDamping") != null) 
			b.angularDamping = Float.parseFloat(e.attributeValue("angularDamping"));
		
		return b;
	}
	
	private ShapeDef parseShapeDefinition(Element e) { 
		String type = e.attributeValue("type");
		
		ShapeDef def = null;
		if ("polygon".equals(type))
			def = parsePhysicsPolygonDefinition(e);
		else if ("circle".equals(type)) {
			def = parsePhysicsCircleDefinition(e);
		} else
			throw new RuntimeException("Unknown shape definition");
		
		// At this point we can grab the general specifics shared
		// by both definitions
		if (e.attribute("density") != null) { 
			def.density = Float.parseFloat(e.attributeValue("density"));
		}
		
		if (e.attribute("friction") != null) { 
			def.friction = Float.parseFloat(e.attributeValue("friction"));
		}
		
		if (e.attribute("restitution") != null) { 
			def.restitution = Float.parseFloat(e.attributeValue("restitution"));
		}

		if (e.attribute("isSensor") != null) { 
			def.isSensor = Boolean.parseBoolean(e.attributeValue("isSensor"));
		}
		
		// TODO grab the filter data if I decide that I need filtering.		
		Element tmp = e.element("FilterData");
		if (tmp != null) { 
			FilterData filter = new FilterData();
			filter.categoryBits = Integer.parseInt(tmp.attributeValue("category"));
			filter.maskBits = Integer.parseInt(tmp.attributeValue("mask"));
			filter.groupIndex = Integer.parseInt(tmp.attributeValue("groupIndex"));
			def.filter = filter;
		}
		
		return def;
	}
	
	private ShapeDef parsePhysicsPolygonDefinition(Element e) { 
		PolygonDef def = new PolygonDef();

		List list = e.elements("vertex");
		for (int i = 0; i < list.size(); ++i) { 
			Element tmp = (Element) list.get(i);
			float x = Float.parseFloat(tmp.attributeValue("x"));
			float y = Float.parseFloat(tmp.attributeValue("y"));
			def.addVertex(new Vec2(x,y));
		}
		return def;
	}
	
	private ShapeDef parsePhysicsCircleDefinition(Element e) { 
		CircleDef def = new CircleDef();
		def.localPosition = new Vec2(0,0);
		
		Element tmp = e.element("localPosition");
		if (tmp != null) {
			float x = Float.parseFloat(tmp.attributeValue("x"));
			float y = Float.parseFloat(tmp.attributeValue("y"));
			def.localPosition = new Vec2(x,y);
		}
		
		def.radius = Float.parseFloat(e.attributeValue("radius"));
		return def;
	}
	
	/**
	 * Pull the different components from the XML element,
	 * construct them and add them to the GameObject
	 * @param e
	 * @param obj
	 */
	private void addComponents(Element e, GameObject obj) { 
		List components = e.element("components").elements("component");
		for (int i = 0; i < components.size(); ++i) {
			Element comp = (Element) components.get(i);
			String className = comp.attributeValue("className");
//			logger.debug("adding " + className + " to " + obj.getName());
			try { 
				Class c = Class.forName(className);
				Constructor<Component> constructor  = c.getConstructor(GameObject.class);
				
				Component compInstance = constructor.newInstance(obj);
				compInstance.fromXML(comp);
				obj.addComponent(compInstance);
			} catch (Exception exp) { 
				exp.printStackTrace();
			}
		}
	}
	
	public void finish() { 
		_inst = null;
	}
}
