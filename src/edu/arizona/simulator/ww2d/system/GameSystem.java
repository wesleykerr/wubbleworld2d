package edu.arizona.simulator.ww2d.system;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.events.system.FinishEvent;
import edu.arizona.simulator.ww2d.events.system.UpdateEnd;
import edu.arizona.simulator.ww2d.events.system.UpdateStart;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.GameGlobals;
import edu.arizona.simulator.ww2d.utils.SlickGlobals;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.SubsystemType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class GameSystem {
    private static Logger logger = Logger.getLogger( GameSystem.class );

	private Map<SubsystemType,Subsystem> _systems;
	private Map<SubsystemType,Subsystem> _enabled;

	private boolean _cameraMode;
	private Vec2 _cameraPos;
	private float _scale;

	public GameSystem(int width, int height, boolean global) { 
		logger.debug("New GameSystem");
		_systems = new TreeMap<SubsystemType,Subsystem>();
		_enabled = new TreeMap<SubsystemType,Subsystem>();
		
		final Space systemSpace = new Space();
		final ObjectSpace objectSpace = new ObjectSpace();
		
		systemSpace.put(Variable.random, new ValueEntry(new Random()));

		systemSpace.put(Variable.screenWidth, new ValueEntry(width));
		systemSpace.put(Variable.screenHeight, new ValueEntry(height));
		
		systemSpace.put(Variable.centerX, new ValueEntry(width/2));
		systemSpace.put(Variable.centerY, new ValueEntry(height/2));
		
		systemSpace.put(Variable.maxAcceleration, new ValueEntry(1.0f));
		systemSpace.put(Variable.maxAngularAcceleration, new ValueEntry(0.4f));
		systemSpace.put(Variable.maxSpeed, new ValueEntry(5.0f));
		systemSpace.put(Variable.maxRotation, new ValueEntry(2.0f));
		
		systemSpace.put(Variable.logicalTime, new ValueEntry(1L));

		if (global) 
			systemSpace.put(Variable.controlledObject, new ValueEntry(new Integer(-1)));
		else 
			systemSpace.put(Variable.controlledObject, new ValueEntry(new Integer(0)));
		
		Blackboard.inst().addSpace("system", systemSpace);
		Blackboard.inst().addSpace("object", objectSpace);

		// Add the SpawnSubsystem into the gameplay system
		// so that everything will work properly.
		addSubsystem(SubsystemType.SpawnSubsystem, new SpawnSubsystem());
		
		_cameraMode = GameGlobals.cameraMode;
		_cameraPos = GameGlobals.cameraPos;
		_scale = GameGlobals.cameraScale;
	}

	/**
	 * Add a subsystem to our map.  By default the subsystem is
	 * automatically enabled.
	 * @param id
	 * @param s
	 */
	public void addSubsystem(SubsystemType id, Subsystem s) {
		_systems.put(id, s);
		_enabled.put(id, s);
	}
	
	/**
	 * Return the subsystem associated with the given id.
	 * @param id
	 * @return
	 */
	public Subsystem getSubsystem(SubsystemType id) { 
		return _systems.get(id);
	}
	
	/**
	 * Enable the specific subsystem so that the update
	 * method is called.
	 * @param id
	 */
	public void enable(SubsystemType id) { 
		_enabled.put(id, _systems.get(id));
	}
	
	/**
	 * Disable the specific subsystem so that the update
	 * method is temporarily not called.
	 * @param id
	 */
	public void disable(SubsystemType id) { 
		_enabled.remove(id);
	}
	
	/**
	 * Increment or decrement the scale value by delta
	 * @param delta
	 */
	public void scale(float delta) { 
		_scale += delta;
		_scale = Math.max(0.01f, _scale);
		_scale = Math.min(3, _scale);
	}
	
	/**
	 * Set the scale for rendering purposes.
	 * @param scale
	 */
	public void setScale(float scale) { 
		_scale = scale;
	}
	
	/**
	 * Switch between a free-moving camera and a 
	 * camera that follows a specific agent.
	 */
	public void flipCameraMode() {
		_cameraMode = !_cameraMode;
	}
	
	/**
	 * Set the camera mode to the desired setting.
	 * @param cameraMode
	 */
	public void setCameraMode(boolean cameraMode) { 
		_cameraMode = cameraMode;
	}
	
	/**
	 * Get the current camera mode.
	 * @return
	 */
	public boolean getCameraMode() { 
		return _cameraMode;
	}
	
	/**
	 * Move the camera by some delta value.
	 * @param delta
	 */
	public void translate(Vec2 delta) { 
		_cameraPos.addLocal(delta);
	}
	
	/**
	 * Set the camera position.
	 * @param pos
	 */
	public void setCamera(Vec2 pos) { 
		_cameraPos = pos;
	}
	
	public void update(int elapsed) {
		// Let those who are interested know that we are starting a brand new
		// update.
		EventManager.inst().dispatchImmediate(new UpdateStart());
		
		for (Subsystem s : _enabled.values()) { 
			s.update(elapsed);
		}
		
		// Dispatch all of the messages from the physics system and other subsystems.
		EventManager.inst().update(elapsed);

		Blackboard.inst().update(elapsed);

		// Dispatch all of the messages generated by the blackboard system.
		EventManager.inst().update(elapsed);
		
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		for (GameObject obj : objectSpace.getAll()) { 
			obj.update(elapsed);
		}
		
		EventManager.inst().dispatchImmediate(new UpdateEnd());
		
		Space systemSpace = Blackboard.inst().getSpace("system");
		long currentTime = systemSpace.get(Variable.logicalTime).get(Long.class);
		systemSpace.get(Variable.logicalTime).setValue(currentTime+1);
	}
	
	/**
	 * Prepare the world for rendering.  Can be useful when we want to project
	 * screen coordinates into world coordinates.
	 * @param g
	 */
	private void startRender(Graphics g) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");

		g.pushTransform();
		g.resetTransform();
		
		float centerX = systemSpace.get(Variable.centerX).get(Integer.class);
		float centerY = systemSpace.get(Variable.centerY).get(Integer.class);

		
		Vec2 pos = null;
		if (_cameraMode) { 
			pos = _cameraPos.mul(_scale);
		} else { 
			// Follow a specific agent determined by the current index.
			PhysicsObject pobj = null; 
			int index = systemSpace.get(Variable.controlledObject).get(Integer.class);
			if (index >= 0) {
				pobj = objectSpace.getControllableObject(index);
				pos = pobj.getPosition().mul(_scale);
			}
		}
		g.translate(-pos.x+centerX, -pos.y+centerY);
		g.scale(_scale, _scale);
	}

	/**
	 * Undo the translations/rotations/scaling that was done to 
	 * the graphics context.
	 * @param g
	 */
	private void finishRender(Graphics g) { 
		g.popTransform();
	}
	
	public void render(Graphics g) { 
		if (g == null)
			return;

		startRender(g);

		Space systemSpace = Blackboard.inst().getSpace("system");
		ObjectSpace objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		for (GameObject obj : objectSpace.getRenderObjects()) { 
			obj.render(g);
		}
		
		// Now render each of the subsystems
		for (Subsystem sub : _enabled.values()) { 
			sub.render(g);
		}
		
		// Go ahead and finish rendering now.  Anything following will
		// be working in Screen coordinates.
		finishRender(g);
		
		int index = systemSpace.get(Variable.controlledObject).get(Integer.class);
		if (index >= 0) {
			PhysicsObject pobj = objectSpace.getControllableObject(index);
			if (pobj.getType() == ObjectType.cognitiveAgent) {
				// now I would like to render some information about the currently controlled
				// agent....
				AgentSpace space = Blackboard.inst().getSpace(AgentSpace.class, pobj.getName());
				BoundedEntry energy = space.getBounded(Variable.energy);

				BoundedEntry valence = space.getBounded(Variable.valence);
				BoundedEntry arousal = space.getBounded(Variable.arousal);

				String target = space.get(Variable.target).get(String.class);

				Color blackAlpha = new Color(Color.black);
				blackAlpha.a = 0.5f;
				g.setColor(blackAlpha);
				g.fillRect(490, 0, 220, 60);

				SlickGlobals.textFont.drawString(500, 0, "Name: " + pobj.getName());
				SlickGlobals.textFont.drawString(500, 10, "Valence: " + GameGlobals.nf.format(valence.getValue()) + 
						" Arousal: " + GameGlobals.nf.format(arousal.getValue()));
				SlickGlobals.textFont.drawString(500, 20, "Energy: " + GameGlobals.nf.format(energy.getValue()));
				SlickGlobals.textFont.drawString(500, 30, "Target: " + target);
			}
		}
	}
	
	public void finish() { 
		EventManager.inst().dispatchImmediate(new FinishEvent());
		
		EventManager.inst().finish();
		Blackboard.inst().finish();
	}
}
