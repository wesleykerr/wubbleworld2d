package edu.arizona.simulator.ww2d.states;

import org.newdawn.slick.Input;
import org.newdawn.slick.state.BasicGameState;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.movement.BackwardEvent;
import edu.arizona.simulator.ww2d.events.movement.ForwardEvent;
import edu.arizona.simulator.ww2d.events.movement.LeftEvent;
import edu.arizona.simulator.ww2d.events.movement.RightEvent;
import edu.arizona.simulator.ww2d.events.movement.StrafeLeftEvent;
import edu.arizona.simulator.ww2d.events.movement.StrafeRightEvent;
import edu.arizona.simulator.ww2d.gui.FengWrapper;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public abstract class BHGameState extends BasicGameState {
	/** Each GameState has access to the FengWrapper since
	 *  there is only one needed per Game.
	 */
	protected FengWrapper _feng;
	
	public BHGameState(FengWrapper feng) { 
		_feng = feng;
	}
	
	protected void handleKey(int key, boolean state) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		ValueEntry entry = systemSpace.get(Variable.controlledObject);
		PhysicsObject obj = objectSpace.getControllableObject(entry.get(Integer.class));
		
		switch (key) { 
		case Input.KEY_W:
			EventManager.inst().dispatch(new ForwardEvent(state, obj));
			break;
		case Input.KEY_S:
			EventManager.inst().dispatch(new BackwardEvent(state, obj));
			break;
		case Input.KEY_A:
			EventManager.inst().dispatch(new LeftEvent(state, obj));
			break;
		case Input.KEY_D:
			EventManager.inst().dispatch(new RightEvent(state, obj));
			break;
		case Input.KEY_Q:
			EventManager.inst().dispatch(new StrafeLeftEvent(state, obj));
			break;
		case Input.KEY_E:
			EventManager.inst().dispatch(new StrafeRightEvent(state, obj));
			break;
		}
	}
	
	public abstract void finish();
}
