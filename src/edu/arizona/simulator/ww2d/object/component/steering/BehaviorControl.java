package edu.arizona.simulator.ww2d.object.component.steering;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.object.component.Component;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Align;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Arrive;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Behavior;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Flee;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.FleeFrom;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.ObstacleAvoidance;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Pursue;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Seek;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Separation;
import edu.arizona.simulator.ww2d.object.component.steering.behaviors.Wander;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.EventListener;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.SteeringOutput;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class BehaviorControl extends Component {
    private static Logger logger = Logger.getLogger( BehaviorControl.class );

    private PhysicsObject _entity;
    
    private Map<Class,Behavior> _behaviors;
    private Map<Class,Float> _weights;
    
	public BehaviorControl(GameObject entity) { 
		super(entity);
		_entity = (PhysicsObject) entity;
		
		_behaviors = new HashMap<Class,Behavior>();
		_weights = new HashMap<Class,Float>();
		_renderPriority = 100;

		addAll();
		addListeners();
	}
	
	private void addListeners() { 
		EventManager.inst().register(EventType.BEHAVIOR_EVENT, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				Class c = (Class) e.getValue("name");
				_behaviors.get(c).onEvent(e);
			} 
		});

		EventManager.inst().register(EventType.BEHAVIOR_WEIGHT_EVENT, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				Class c = (Class) e.getValue("name");
				Float weight = (Float) e.getValue("weight");
				_weights.put(c, weight);
			} 
		});
	}
	
	/**
	 * Initialize the off map with the possible behaviors.
	 */
	private void addAll() { 
		_behaviors.put(Align.class, new Align(_entity,0));
		_behaviors.put(Arrive.class, new Arrive(_entity, new Vec2()));
		_behaviors.put(Seek.class, new Seek(_entity, new Vec2()));
		_behaviors.put(Flee.class, new Flee(_entity, new Vec2()));
		_behaviors.put(ObstacleAvoidance.class, new ObstacleAvoidance(_entity));
		_behaviors.put(Pursue.class, new Pursue(_entity));
		_behaviors.put(FleeFrom.class, new FleeFrom(_entity));
		_behaviors.put(Wander.class, new Wander(_entity));
		_behaviors.put(Separation.class, new Separation(_entity));
		
		for (Class c : _behaviors.keySet()) { 
			_weights.put(c, 1.0f);
		}
	}

	@Override
	public void update(int delta) {
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _entity.getName());
		float moveModifier = space.get(Variable.moveModifier).get(Float.class);
		float turnModifier = space.get(Variable.turnModifier).get(Float.class);

		SteeringOutput so = new SteeringOutput(new Vec2(), 0);
		for (Behavior b : _behaviors.values()) { 
			if (b.isOn()) { 
				so.blend(b.getSteering(moveModifier, turnModifier), _weights.get(b.getClass()));
			}
		}
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		float maxAccel = systemSpace.get(Variable.maxAcceleration).get(Float.class);
		float maxAngularAccel = systemSpace.get(Variable.maxAngularAcceleration).get(Float.class);
		
		float length = so.getVelocity().length();
		if (length > (maxAccel*moveModifier)) { 
			so.getVelocity().normalize();
			so.getVelocity().mulLocal(maxAccel*moveModifier);
		}
		_entity.getBody().applyForce(so.getVelocity(), _entity.getBody().getPosition());
		
		float angular = so.getAngular();
		float angularAcceleration = Math.abs(angular);
		if (angular > (maxAngularAccel*turnModifier)) {
			angular /= angularAcceleration;
			angular *= (maxAngularAccel*turnModifier);
		}
		_entity.getBody().applyTorque(angular);
	}
	
	@Override
	public void render(Graphics g) { 
		// if we happen to be the controlled agent, then let's put 
		// the names of the active behaviors in some corner of the room.
		int count = 0;
		StringBuffer buf = new StringBuffer();
		for (Behavior b : _behaviors.values()) { 
			if (b.isOn()) { 
				buf.append(b.getClass().getSimpleName() + "\n");
				b.render(g);
				++count;
			}
		}
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		int index = systemSpace.get(Variable.controlledObject).get(Integer.class);
		PhysicsObject us = (PhysicsObject) _parent;
		PhysicsObject con = objectSpace.getCognitiveAgents().get(index);
		if (us == con) { 
			Vec2 pos = us.getPosition();
			Color blackAlpha = new Color(Color.black);
			blackAlpha.a = 0.5f;
			g.setColor(blackAlpha);
			g.fillRect(pos.x-100, pos.y-200, 200, count*25);
			
			GameGlobals.textFont.drawString(pos.x-98, pos.y-198, buf.toString(), Color.white);
		}
	}

	@Override
	public void fromXML(Element e) {
		// TODO Auto-generated method stub
		
	}

}
