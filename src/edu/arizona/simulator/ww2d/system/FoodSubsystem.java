package edu.arizona.simulator.ww2d.system;

import java.util.Random;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.spawn.CreatePhysicsObject;
import edu.arizona.simulator.ww2d.events.spawn.RemoveGameObject;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

/**
 * This class is responsible for periodically respawning
 * new food into the world.  We must take care not to overload
 * the world with food.
 * @author wkerr
 *
 */
public class FoodSubsystem implements Subsystem {
    private static Logger logger = Logger.getLogger( FoodSubsystem.class.getName() );
	
	private int _counter = 0;
	
	private int _active = 0;
	private int _minSize;

	private Element _template;
	private Element _storeComponent;

	public FoodSubsystem(int min) { 
		_minSize = min;
		
		Document document = DocumentHelper.createDocument();
        _template = document.addElement( "physicsObject" );

        _template.addAttribute("name", "empty");
        _template.addAttribute("renderPriority", "2");
        _template.addAttribute("type", "food");

        _template.addElement("bodyDef")
        	.addAttribute("x", "0")
        	.addAttribute("y", "0");
        
        _template.addElement("shapeDef")
        	.addAttribute("type", "circle")
        	.addAttribute("isSensor", "true")
        	.addAttribute("radius", "2.0");
        
        Element components = _template.addElement("components");
        components.addElement("component")
        	.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.SpriteVisual")
        	.addElement("image")
        	   .addAttribute("name", "data/images/health-circle.png")
        	   .addAttribute("scale", "0.0625");

        _storeComponent = components.addElement("component")
        	.addAttribute("className", "edu.arizona.simulator.ww2d.object.component.FoodComponent")
        	.addAttribute("store", "0.0");

        addListeners();
	}
	
	private void addListeners() { 
		EventManager.inst().registerForAll(RemoveGameObject.class, new EventListener() { 
			@Override
			public void onEvent(Event e) { 
				RemoveGameObject event = (RemoveGameObject) e;
				GameObject obj = event.getGameObject();
				if (obj.getType() != ObjectType.food)
					return;

				PhysicsObject pobj = (PhysicsObject) obj;
				ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
				objectSpace.remove(pobj);
				
				pobj.finish();
				
				--_active;
			}
		});
	}
	
	@Override
	public SubsystemType getId() {
		return SubsystemType.FoodSubsystem;
	}

	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(int eps) {
		if (_active < _minSize && Math.random() < 0.01) { 
			spawnFoodPlot();
		}
	}
	
	private void spawnFoodPlot() { 
		++_counter;
		++_active;
		
		// each food plot is going to be the same size.  A circle with radius 2.
		// lets randomly select a position to drop this food plot.
		Space systemSpace = Blackboard.inst().getSpace("system");
		Random r = systemSpace.get(Variable.random).get(Random.class);
		World world = systemSpace.get(Variable.physicsWorld).get(World.class);
		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);
		
		Vec2 min = systemSpace.get(Variable.physicsMin).get(Vec2.class);
		Vec2 max = systemSpace.get(Variable.physicsMax).get(Vec2.class);
		
		Vec2 buffer = new Vec2(6,6);
		Vec2 position = MathUtils.random(r, min.add(buffer), max.sub(buffer));
		
		float store = 10*(r.nextInt(9)+1);
		
		_template.attribute("name").setText("foodplot"+_counter);
		_template.element("bodyDef").attribute("x").setText(position.x+"");
		_template.element("bodyDef").attribute("y").setText(position.y+"");
		_storeComponent.attribute("store").setText(store+"");
		
		Event e = new CreatePhysicsObject(world, _template);
		EventManager.inst().dispatchImmediate(e);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}
}
