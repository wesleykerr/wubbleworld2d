package edu.arizona.simulator.ww2d.events.player;

import org.jbox2d.common.Vec2;

import edu.arizona.simulator.ww2d.events.Event;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.component.kinematics.KinematicsComponent;

public class KinematicEvent extends Event {
	 private Vec2 _target;
	 private KinematicsComponent.Behavior _movement;
	 
	 public KinematicEvent(Vec2 target, KinematicsComponent.Behavior movement) { 
		 _target = target;
		 _movement = movement;
	 }
	 
	 public KinematicEvent(Vec2 target, KinematicsComponent.Behavior movement, GameObject... objects) { 
		 super(objects);
		 
		 _target = target;
		 _movement = movement;
	 }
	 
	 public Vec2 getTarget() { 
		 return _target;
	 }
	 
	 public KinematicsComponent.Behavior getMovement() { 
		 return _movement;
	 }
	 
}
