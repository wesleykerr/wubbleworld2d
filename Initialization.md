# Introduction #

In Wubble World 2D the agents are capable of acting autonomously.  This is accomplished by adding a GoalBehavior component onto the agent.  Rather than having to modify code to change the behaviors of the agents, we provide XML initialization files.  In fact there are two initialization files that are necessary to prepare the simulation.

  * Level Initialization - These files contain the description of the level (or room) and the static and dynamic objects that inhabit the level.  Some of the objects are purely cosmetic (such as the rendering of the background), while others are physical entities that the agent can interact with.  Examples of level initialization include:

```
src/data/levels/Room-Box.xml
src/data/levels/Room-Columns.xml
src/data/levels/Room-Food.xml
...
```

  * Agent Initialization - These files contain the description of the agents inhabiting the environment.  Each agent is composed of several components, some for visualization, others for physical parameters, and some to describe the AI.  The GoalBehavior component applied to an agent acts as an arbiter for competing goals.  Here is a sample agent definition:

```
<physicsObject name="agent1" renderPriority="100" type="cognitiveAgent" hasMass="true"  initAgent="initStaticAgent" >
	<bodyDef random="true" radius="0.5" />
	<shapeDef type="circle" density="0.2" friction="0.5" restitution="0.25" radius="0.5" />
		
	<components>
		<component className="edu.arizona.simulator.ww2d.object.component.ShapeVisual" fromPhysics="true">
			<renderPriority value="99" />
			<color r="1.0" g="0.0" b="0.0" a="1.0" />
		</component>
		<component className="edu.arizona.simulator.ww2d.object.component.SpriteVisual">
			<renderPriority value="100" />
			<image name="data/images/half-circle.png" scale="0.03125" />
		</component>
				
		<component className="edu.arizona.simulator.ww2d.object.component.GoalBehavior" >
			<goals>
				<goal className="edu.arizona.simulator.ww2d.object.component.goals.WanderGoal" />
				<goal className="edu.arizona.simulator.ww2d.object.component.goals.KickBallGoal" />
			</goals>
		</component>
		<component className="edu.arizona.simulator.ww2d.object.component.steering.BehaviorControl" />
		<component className="edu.arizona.simulator.ww2d.object.component.PerceptionComponent" />
		<component className="edu.arizona.simulator.ww2d.object.component.TopDownControl" />
		<component className="edu.arizona.simulator.ww2d.object.component.InternalComponent" />
	</components>
</physicsObject> 
```


This agent has seven components.  The ShapeVisual component specifies how to render this agent.  It will render the polygonal shape that is also used for the physics.  An additional half-circle SpriteVisual is added in order to determine the direction that the agent is facing (since it is a circle).

The next component is the GoalBehavior component.  There are two competing goals that are added to the component, specifically wander and kick ball.  Goals are selected based on insistence and when this agent sees a ball, it will want to kick it (given that it has enough energy).

The BehaviorControl component provides the mechanisms to move the agent around the screen.  PerceptionComponent gives the agent eyes, ears and an ability to smell.  The InternalComponent gives the agent a limited amount of energy.  Energy is lost by moving quickly and energy is gained by eating food.