package edu.arizona.simulator.ww2d.object.component;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.events.EventListener;
import edu.arizona.simulator.ww2d.events.player.EnergyEvent;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

/**
 * The internal component listens for collisions between this agent 
 * and other agents and updates the health correctly.  In addtion
 * this component also checks the moveModifier and turnModifiers
 * to see if we need to decrement the power of the parent.
 * 
 * @author wkerr
 *
 */
public class InternalComponent extends Component {
    private static Logger logger = Logger.getLogger( InternalComponent.class );

    private float _energyDelta;
    
	public InternalComponent(GameObject obj) { 
		super(obj);
		
		// Add in the energy updates that come in from eating and what not
		EventManager.inst().register(EnergyEvent.class, _parent, new EventListener() {
			@Override
			public void onEvent(Event e) {
				EnergyEvent event = (EnergyEvent) e;
				_energyDelta += event.getAmount();
			} 
		});
	}
	
	@Override
	public void update(int elapsed) {
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());

//		if (_energyDelta > 0) { 
//			logger.debug("Starting with a bonus to health " + _energyDelta);
//		}
		
		// First look for any collisions to see if we need to adjust the health
		// of the parent object.
//		for (CollisionEntry entry : space.getCollisions()) { 
//			if (entry.processed())
//				continue;
//			
//			
//			// I want to look at some of the internals of the collisions....
////			logger.debug(entry.getObject1().getName() + " - " + entry.getObject2().getName() + " " + entry.getNormal() + " " + entry.getVelocity());
//			
//			ObjectType type = entry.getOther(_obj).getType();
//			
//			// if this a collision between two deliberate agents... then the damage
//			// will be determined by the velocity 
//			if (type == ObjectType.cognitiveAgent || type == ObjectType.reactiveAgent) {
//				Vec2 v1 = entry.getPosition().sub(_parent.getPosition());
//				Vec2 v2 = MathUtils.toVec2(_parent.getHeading());
//
//				// Being hit from behind is more painful then front on
//				// collisions.
//				float angle = Math.abs(MathUtils.angleBetween(v1, v2));
//				if (angle > Math.PI / 4 && angle < (3 * Math.PI / 4)) { 
////					logger.debug("Collision from the front : " + entry.getObject1().getName() + " - " + entry.getObject2().getName());
//					_energyDelta -= 2;
//				} else if (angle > (3 * Math.PI / 4)) { 
//					_energyDelta -= 4;
////					logger.debug("Collision from behind : " + entry.getObject1().getName() + " - " + entry.getObject2().getName());
//				}
//				
//				entry.processed(true);
//			}
//		}
		
		// now see if we are running hot and therefore burning power
		// see if we are actually moving first.
		float eps = (float) elapsed / 3000f;
		BoundedEntry energy = space.getBounded(Variable.energy);
		if (space.get(Variable.isMoving).get(Boolean.class)) {
			float moveModifier = space.get(Variable.moveModifier).get(Float.class);
			float turnModifier = space.get(Variable.turnModifier).get(Float.class);
			float tmp = 0;
			if (moveModifier > 1 && turnModifier > 1) { 
				tmp -= (moveModifier*turnModifier);
			} else if (moveModifier > 1) { 
				tmp -= (moveModifier*moveModifier);
			} else if (turnModifier > 1) { 
				tmp -= (turnModifier*turnModifier);
			}		
			tmp *= eps;
			_energyDelta += tmp;
		}
		energy.change(_energyDelta);

		// TODO: if health <= 0 then we need to disable this one.  
		// Should it be done here or somewhere else?
		if (energy.getValue() <= 0) { 
			space.get(Variable.moveModifier).setValue(0.5f);
			space.get(Variable.turnModifier).setValue(0.5f);
		}

		// clear out the deltas so that next time tick they will be
		// fresh.
		_energyDelta = 0;
	}
	
	@Override 
	public void render(Graphics g) { 
		AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		BoundedEntry energy = space.getBounded(Variable.energy);

		// first draw a black bar that represents 100% of the agent's health
		// then draw a red bar that represents what percent of the agent's health remains
		// each tick is worth a single pixel.
		float halfPower = energy.getMax() / 2;
		float quarterPower = halfPower / 2;
		
		Vec2 position = _parent.getPosition().sub(new Vec2(quarterPower,13));

		g.setColor(Color.black);
		g.fillRect(position.x-1, position.y-1, halfPower, 8);
		
		g.setColor(Color.blue);
		g.fillRect(position.x, position.y, energy.getValue()*0.5f, 7);
		
	}

	@Override
	public void fromXML(Element e) {

	}

}
