package edu.arizona.simulator.ww2d.blackboard;

import org.apache.log4j.Logger;
import org.jbox2d.dynamics.World;

import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class AgentHelper {
    private static Logger logger = Logger.getLogger( AgentHelper.class );

	/**
	 * This method is responsible for randomly generating the initial
	 * parameters of an cognitive agent in the world.  
	 * 
	 * Health
	 *   max
	 * Energy
	 *   max
	 * 
	 * Aggressiveness
	 * Mood - default emotional state (homeostatis point)
	 * 
	 * @param agent
	 */
	public static void initAgent(PhysicsObject agent) {		
		// the minimum for health is always 0.
		// the maximum for health is a random value between 20 and 100
		float maxEnergy= (float) 20 * (MathUtils.random.nextInt(4)+1);
		BoundedEntry energy = new BoundedEntry(maxEnergy*0.5f, 0, maxEnergy);
		
		AgentSpace space = new AgentSpace(agent.getName());
		space.put(Variable.energy, energy);
		
		// we have a set of big five personality traits...
		space.put(Variable.openness, new ValueEntry(MathUtils.random.nextFloat()));
		space.put(Variable.conscientiousness, new ValueEntry(MathUtils.random.nextFloat()));
		space.put(Variable.extroversion, new ValueEntry(MathUtils.random.nextFloat()));
		space.put(Variable.agreeableness, new ValueEntry(MathUtils.random.nextFloat()));
		space.put(Variable.neuroticism, new ValueEntry(MathUtils.random.nextFloat()));
		
		float valence = MathUtils.random.nextFloat();
		BoundedEntry vBounded = new BoundedEntry(valence, 0, 1);
		space.put(Variable.valence, vBounded);
		
		float arousal = MathUtils.random.nextFloat();
		BoundedEntry aBounded = new BoundedEntry(arousal, 0, 1);
		space.put(Variable.arousal, aBounded);
		
		space.put(Variable.goal, new ValueEntry("ThinkGoal"));
		space.put(Variable.state, new ValueEntry("Thinking"));
		space.put(Variable.target, new ValueEntry(""));
		
		space.put(Variable.isMoving, new ValueEntry(false));

		space.put(Variable.moveModifier, new ValueEntry(1.0f));
		space.put(Variable.turnModifier, new ValueEntry(1.0f));
		
		Blackboard.inst().addSpace(agent.getName(), space);	
	}
	
	/**
	 * This method is responsible for randomly generating the initial
	 * parameters of an cognitive agent in the world.  
	 * 
	 * Health
	 *   max
	 * Energy
	 *   max
	 * 
	 * Aggressiveness
	 * Mood - default emotional state (homeostatis point)
	 * 
	 * A static agent has no randomess in it
	 * 
	 * @param agent
	 */
	public static void initStaticAgent(PhysicsObject agent) {		
		// the minimum for health is always 0.
		// the maximum for health is a random value between 20 and 100
		BoundedEntry energy = new BoundedEntry(100, 0, 100);
		
		AgentSpace space = new AgentSpace(agent.getName());
		space.put(Variable.energy, energy);
		
		// we have a set of big five personality traits...
		space.put(Variable.openness, new ValueEntry(0.5f));
		space.put(Variable.conscientiousness, new ValueEntry(0.5f));
		space.put(Variable.extroversion, new ValueEntry(0.5f));
		space.put(Variable.agreeableness, new ValueEntry(0.5f));
		space.put(Variable.neuroticism, new ValueEntry(0.5f));
		
		space.put(Variable.valence, new BoundedEntry(0.5f, 0, 1));
		space.put(Variable.arousal, new BoundedEntry(0.5f, 0, 1));
		
		space.put(Variable.goal, new ValueEntry("ThinkGoal"));
		space.put(Variable.state, new ValueEntry("Thinking"));
		space.put(Variable.target, new ValueEntry(""));
		
		space.put(Variable.isMoving, new ValueEntry(false));

		space.put(Variable.moveModifier, new ValueEntry(1.0f));
		space.put(Variable.turnModifier, new ValueEntry(1.0f));
		
		Blackboard.inst().addSpace(agent.getName(), space);	
	}
	
	/**
	 * This method is responsible for randomly generating the initial
	 * parameters of an cognitive agent in the world.  
	 * 
	 * Health
	 *   max
	 * Energy
	 *   max
	 * 
	 * Aggressiveness
	 * Mood - default emotional state (homeostatis point)
	 * 
	 * A static agent has no randomess in it
	 * 
	 * @param agent
	 */
	public static void initAttackAgent(PhysicsObject agent) {		
		// the minimum for health is always 0.
		// the maximum for health is a random value between 20 and 100
		float randValue = MathUtils.random.nextInt(5) + 1;
		float maxEnergy = 50f + (randValue * 10f);
		BoundedEntry energy = new BoundedEntry(maxEnergy, 0, maxEnergy);
		
		AgentSpace space = new AgentSpace(agent.getName());
		space.put(Variable.energy, energy);
		
		// we have a set of big five personality traits...
		space.put(Variable.openness, new ValueEntry(0.5f));
		space.put(Variable.conscientiousness, new ValueEntry(0.5f));
		space.put(Variable.extroversion, new ValueEntry(0.5f));
		space.put(Variable.agreeableness, new ValueEntry(0.5f));
		space.put(Variable.neuroticism, new ValueEntry(0.5f));
		
		space.put(Variable.valence, new BoundedEntry(0.5f, 0, 1));
		space.put(Variable.arousal, new BoundedEntry(0.5f, 0, 1));
		
		space.put(Variable.goal, new ValueEntry("ThinkGoal"));
		space.put(Variable.state, new ValueEntry("Thinking"));
		space.put(Variable.target, new ValueEntry(""));
		
		space.put(Variable.isMoving, new ValueEntry(false));

		space.put(Variable.moveModifier, new ValueEntry(1.0f));
		space.put(Variable.turnModifier, new ValueEntry(1.0f));
		
		Blackboard.inst().addSpace(agent.getName(), space);	
	}
	
	public static void recordSystem(AgentSpace agentSpace) { 
		// this should only be called after the PhysicsSubsystem
		Space systemSpace = Blackboard.inst().getSpace("system");
		World world = systemSpace.get(Variable.physicsWorld).get(World.class);
		
		// Here we will log some of the PhysicsWorld information.
		agentSpace.getFluentStore().record("gravityX", "physics", true, world.getGravity().x);
		agentSpace.getFluentStore().record("gravityY", "physics", true, world.getGravity().y);
		agentSpace.getFluentStore().record("aabbLowerBoundX", "physics", true, world.getWorldAABB().lowerBound.x);
		agentSpace.getFluentStore().record("aabbLowerBoundY", "physics", true, world.getWorldAABB().lowerBound.y);
		agentSpace.getFluentStore().record("aabbUpperBoundX", "physics", true, world.getWorldAABB().upperBound.x);
		agentSpace.getFluentStore().record("aabbUpperBoundY", "physics", true, world.getWorldAABB().upperBound.y);
	}
}
