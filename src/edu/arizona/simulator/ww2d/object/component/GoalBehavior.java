package edu.arizona.simulator.ww2d.object.component;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

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
import edu.arizona.simulator.ww2d.object.component.goals.AvoidOthersGoal;
import edu.arizona.simulator.ww2d.object.component.goals.DefendGoal;
import edu.arizona.simulator.ww2d.object.component.goals.EatGoal;
import edu.arizona.simulator.ww2d.object.component.goals.Goal;
import edu.arizona.simulator.ww2d.object.component.goals.IdleGoal;
import edu.arizona.simulator.ww2d.object.component.goals.KickBallGoal;
import edu.arizona.simulator.ww2d.object.component.goals.PursueGoal;
import edu.arizona.simulator.ww2d.object.component.goals.WanderGoal;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.enums.GoalEnum;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class GoalBehavior extends Component {
    private static Logger logger = Logger.getLogger( GoalBehavior.class );

	private boolean _firstTime;

	private PhysicsObject _obj;
	
	private List<Goal> _potentialGoals;
	
	private Goal _activeGoal;
	private float _insistence;
	
	
	public GoalBehavior(GameObject obj) { 
		super(obj);
		_firstTime = true;
		_obj = (PhysicsObject) obj;

		_potentialGoals = new ArrayList<Goal>();
		_potentialGoals.add(new WanderGoal(_obj));
		_potentialGoals.add(new EatGoal(_obj));
		_potentialGoals.add(new DefendGoal(_obj));
		_potentialGoals.add(new IdleGoal(_obj));
		_potentialGoals.add(new PursueGoal(_obj));
		_potentialGoals.add(new AvoidOthersGoal(_obj));
		_potentialGoals.add(new KickBallGoal(_obj));
		
		_insistence = Float.MIN_VALUE;
	}
	
	@Override
	public void update(int elapsed) {
		if (_firstTime) {
			_firstTime = false;
		}
		
		arbitrate();
		if (_activeGoal != null) {
			GoalEnum status = _activeGoal.process();

			if (status != GoalEnum.active) { 
//				logger.debug("Goal: " + _activeGoal + " exited with " + status);
				_activeGoal.terminate();
				_activeGoal = null;
				_insistence = Float.MIN_VALUE;
			} else { 
				AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
				space.get(Variable.goal).setValue(_activeGoal.getClass().getSimpleName());
			}
		}
		
	}

	@Override
	public void fromXML(Element e) {
		// if we have specified then we should only load those classes that are
		// actually important for this agent.
		if (e.element("goals") != null) { 
			_potentialGoals = new ArrayList<Goal>();

			List goals = e.element("goals").elements("goal");
			for (Object obj : goals) { 
				Element goal = (Element) obj;
				String className = goal.attributeValue("className");
				try { 
					Constructor constructor = Class.forName(className).getConstructor(PhysicsObject.class);
					_potentialGoals.add((Goal) constructor.newInstance(_obj));
				} catch (Exception exception) { 
					logger.error("Unable to load class " + className);
					throw new RuntimeException(exception.getMessage());
				}
			}
		}
	}

	/**
	 * Arbitrate selects the current goal
	 */
	public void arbitrate() { 
		float max = -1;
		Goal newGoal = null;
		
		for (Goal g : _potentialGoals) { 
			float d = g.desireability();
			if (d > max) {
				newGoal = g;
				max = d;
			}
		}
		
		//  we will only change the current goal if
		// the new goal has a higher insistence
		if (max < _insistence) { 
			return;
		}
		
		_insistence = max;
		if (newGoal != _activeGoal) { 
			if (_activeGoal != null) { 
				_activeGoal.terminate();
			}
			
			_activeGoal = newGoal;
			_activeGoal.activate();
		}
	}
	
	@Override
	public void render(Graphics g) { 
		if (_activeGoal == null)
			return;
		
		// if we happen to be the controlled agent, then let's put 
		// the names of the active behaviors in some corner of the room.
		Space systemSpace = Blackboard.inst().getSpace("system");
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _obj.getName());
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		int index = systemSpace.get(Variable.controlledObject).get(Integer.class);
		PhysicsObject us = (PhysicsObject) _parent;
		PhysicsObject con = objectSpace.getCognitiveAgents().get(index);
		if (us == con) { 
			Vec2 pos = us.getPosition();
			Color blackAlpha = new Color(Color.black);
			blackAlpha.a = 0.5f;
			g.setColor(blackAlpha);
			g.fillRect(pos.x+100, pos.y-200, 200, 50);
			
			StringBuffer buf = new StringBuffer();
			buf.append(_activeGoal.getClass().getSimpleName() + "\n");
			buf.append(agentSpace.get(Variable.state).get(String.class) + "\n");
			buf.append("Move : " + agentSpace.get(Variable.moveModifier).get(Float.class) + " ");
			buf.append("Turn : " + agentSpace.get(Variable.turnModifier).get(Float.class) + "\n");
			GameGlobals.textFont.drawString(pos.x+102, pos.y-198, buf.toString(), Color.white);
		}
	}
}
