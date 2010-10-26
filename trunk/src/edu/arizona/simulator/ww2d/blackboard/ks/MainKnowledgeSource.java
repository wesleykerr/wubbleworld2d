package edu.arizona.simulator.ww2d.blackboard.ks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.ContactPoint;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.AuditoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.BoundedEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.CollisionEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.DistanceEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.FoodEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.MemoryEntry;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.ObjectSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.system.EventManager;
import edu.arizona.simulator.ww2d.utils.Event;
import edu.arizona.simulator.ww2d.utils.MathUtils;
import edu.arizona.simulator.ww2d.utils.enums.EventType;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class MainKnowledgeSource implements KnowledgeSource {
    private static Logger logger = Logger.getLogger( MainKnowledgeSource.class );
    
	private Space       _systemSpace;
	private ObjectSpace _objectSpace;
	
	@Override
	public void update() { 
		_systemSpace = Blackboard.inst().getSpace("system");
		_objectSpace = Blackboard.inst().getSpace(ObjectSpace.class, "object");
		
		for (PhysicsObject obj : _objectSpace.getCognitiveAgents()) { 
			AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, obj.getName());

			determineMoving(agentSpace, obj);
			determineAttackable(agentSpace, obj);
			determineApproaching(agentSpace, obj);
			
			if (obj.getType() == ObjectType.cognitiveAgent) { 
				// combine all of that information into one source that 
				// updates the emotional state of the agent				
				determineNovelItems(agentSpace, obj);
				processCollisions(agentSpace, obj);
				
				// TODO: over time arousal goes done and valence returns towards 0.
				BoundedEntry valence = agentSpace.getBounded(Variable.valence);
				if (!valence.hasUpdated()) { 
					float modifier = 1;
					if (valence.getValue() > 0.5f) { 
						modifier = -1;
					}
					valence.change(modifier*0.0005f);
				}
				
				BoundedEntry arousal = agentSpace.getBounded(Variable.arousal);
				if (!arousal.hasUpdated()) { 
					arousal.change(-0.0005f);
				}
				
			}
		}
	}
	
	private void determineMoving(AgentSpace agentSpace, PhysicsObject obj) { 
		LinkedList<MemoryEntry> selfMemory = agentSpace.getSelfMemory();
		
		if (selfMemory.size() < 2) {
			return;
		}

		float epsilon = 0.001f;
		// determine the delta from the previous time step until
		// now.  If it is greater than some amount... we are moving
		
		Vec2 oldPosition = selfMemory.get(1).position;
		Vec2 newPosition = selfMemory.get(0).position;
		Vec2 delta = newPosition.sub(oldPosition);
		
		boolean moving = false;
		if (delta.length() > epsilon) 
			moving = true;

		ValueEntry entry = agentSpace.get(Variable.isMoving);
		entry.setValue(moving);
	}
	
	private void determineApproaching(AgentSpace agentSpace, PhysicsObject obj) { 
		LinkedList<Map<String,MemoryEntry>> memories = agentSpace.getVisualMemories();
		LinkedList<Map<String,AuditoryEntry>> auditory = agentSpace.getAuditoryMemories();
	
		if (memories.isEmpty() || memories.size() < 2 || auditory.isEmpty() || auditory.size() < 2) 
			return;
		
		// first determine the set of PhysicsObjects who could be approaching.
		// they must be within auditory or visual range in the previous time tick
		// as well as the current time tick.
		Map<String,MemoryEntry> previousMemories = memories.get(1);
		Map<String,MemoryEntry> currentMemories = memories.getFirst();
		
		Set<String> set1 = new HashSet<String>(previousMemories.keySet());
		set1.retainAll(currentMemories.keySet());
		
		Map<String,AuditoryEntry> previousAuditory = auditory.get(1);
		Map<String,AuditoryEntry> currentAuditory = auditory.get(0);

		Set<String> set2 = new HashSet<String>(previousAuditory.keySet());
		set2.retainAll(currentAuditory.keySet());
		
		// Now take the union to make sure that we have the set of all
		// agents who could be approaching us or we could be approaching.
		set1.addAll(set2);

		for (String name : set1) { 
			PhysicsObject obj2 = _objectSpace.getPhysicsObject(name);
			
			DistanceEntry current = _objectSpace.findOrAddDistance(obj, obj2);
			DistanceEntry previous = _objectSpace.getDistance(obj, obj2, 1);
			
			if (current.getDistance() < previous.getDistance()) {
				agentSpace.addApproaching(obj2.getName());
			}
		}
	}
	
	private void determineAttackable(AgentSpace agentSpace, PhysicsObject obj) {
		LinkedList<Map<String,MemoryEntry>> memories = agentSpace.getVisualMemories();
		LinkedList<Map<String,AuditoryEntry>> auditory = agentSpace.getAuditoryMemories();
	
		if (memories.isEmpty() && auditory.isEmpty()) 
			return;
		
		// iterate through the objects stored in the memory entries.
		// if they happen to be a dynamic entity with some health then
		// they can be attacked.
		Set<PhysicsObject> attackable = new HashSet<PhysicsObject>();
		Map<String,MemoryEntry> map = memories.getFirst();
		for (MemoryEntry entry : map.values()) { 
			// If this is a dynamic entity
			if (entry.obj.getType() == ObjectType.cognitiveAgent) {
				AgentSpace tmp = Blackboard.inst().getSpace(AgentSpace.class, entry.obj.getName());
				float energy = tmp.getBounded(Variable.energy).getValue();
				if (energy > 0) { 
					attackable.add(entry.obj);
				}
			}
		}

		Map<String,AuditoryEntry> auditoryMap = auditory.getFirst();
		for (AuditoryEntry entry : auditoryMap.values()) { 
			// If this is a dynamic entity
			if (entry.obj.getType() == ObjectType.cognitiveAgent) {
				AgentSpace tmp = Blackboard.inst().getSpace(AgentSpace.class, entry.obj.getName());
				float energy = tmp.getBounded(Variable.energy).getValue();
				if (energy > 0) { 
					attackable.add(entry.obj);
				}
			}
		}
		
		ValueEntry tmpEntry = agentSpace.get(Variable.attackable);
		if (tmpEntry == null) { 
			agentSpace.put(Variable.attackable, new ValueEntry(attackable));
		} else { 
			tmpEntry.setValue(attackable);
		}
	}
	
	private void determineNovelItems(AgentSpace agentSpace, PhysicsObject obj) { 
		long millis = System.currentTimeMillis();
		
		LinkedList<Map<String,MemoryEntry>> memories = agentSpace.getVisualMemories();
		if (memories.size() < 2) 
			return;
		
		// determine the novel visual items that we've just started seeing this
		// time tick.
		Map<String,MemoryEntry> previousMemories = memories.get(1);
		Map<String,MemoryEntry> currentMemories = memories.getFirst();
		
		Set<String> set1 = new HashSet<String>(currentMemories.keySet());
		set1.removeAll(previousMemories.keySet());
		
		LinkedList<Map<String,AuditoryEntry>> auditory = agentSpace.getAuditoryMemories();
		Map<String,AuditoryEntry> previousAuditory = auditory.get(1);
		Map<String,AuditoryEntry> currentAuditory = auditory.get(0);

		Set<String> set2 = new HashSet<String>(currentAuditory.keySet());
		set2.removeAll(previousAuditory.keySet());

		LinkedList<Map<String,FoodEntry>> scent = agentSpace.getScentMemories();
		Map<String,FoodEntry> previousFood = scent.get(1);
		Map<String,FoodEntry> currentFood = scent.get(0);
		
		Set<String> set3 = new HashSet<String>(currentFood.keySet());
		set3.removeAll(previousFood.keySet());
		
		Set<String> set = new HashSet<String>(set1);
		set.addAll(set2);
		set.addAll(set3);
		
		// get the previously novel items from the agent space and 
		// remove those that are no longer novel.
		Map<String,Long> oldNovelSet = agentSpace.getNovelItems();
		Map<String,Long> newNovelSet = new HashMap<String,Long>();
		for (String s : set) {
			newNovelSet.put(s, millis);
		}
		
		for (Map.Entry<String,Long> entry : oldNovelSet.entrySet()) { 
			// you get to stay novel for roughly a second
			if (millis - entry.getValue() < 1000) { 
				newNovelSet.put(entry.getKey(), entry.getValue());
			}
		}
		
		agentSpace.setNovelItems(newNovelSet);
		
		// now we are going to update our arousal by some value, specifically the
		// number of novel things that we have seen.
		if (newNovelSet.size() > 0)	
			agentSpace.getBounded(Variable.arousal).change((float) newNovelSet.size()/ 1000f);
	}
	
	/**
	 * Processing collisions will do a lot more than just updating emotional
	 * state.  We also have to send the correct health adjustments to the
	 * listeners.
	 * @param agentSpace
	 * @param obj
	 * @param deltas
	 */
	private void processCollisions(AgentSpace agentSpace, PhysicsObject obj) { 
		float energyDelta = 0;
		for (CollisionEntry ce : agentSpace.getCollisions()) { 
			if (ce.processed() || ce.getNormalImpulse() < 0.001) 
				continue;
			
			float vModifier = 0.0f;
			float aModifier = 0.0f;
			ObjectType type = ce.getOther(obj).getType();
			switch (type) { 
			case obstacle:
				aModifier = 0.2f;
				vModifier = -0.2f;

				break;
			case dynamic:
				vModifier = 0.1f;
				aModifier = 0.0f;
				
				break;
			case wall:
				aModifier = 0.01f;
				vModifier = -0.01f;

				break;
			case cognitiveAgent:
			case reactiveAgent:
				Vec2 v1 = ce.getPosition().sub(obj.getPPosition());
				Vec2 v2 = MathUtils.toVec2(obj.getHeading());
				
				// normalize the vectors before passing them off.
				v1.normalize();
				v2.normalize();

				// Being hit from behind is more painful then front on collisions.
				// Math.PI / 2 is from the Sides
				// 0 is directly from behind
				// Math.PI is directly from the front
				
				float angle = MathUtils.angleBetween(v1, v2);
//				logger.debug("@@@@@@@ - Collision : " + ce.getPosition() + " Obj Position: " + obj.getPPosition());
//				logger.debug("@@@@@@@ - v1: " + v1 + " v2: " + v2);
//				logger.debug("@@@@@@@ - angle: " + angle);
				if (angle <= MathUtils.PI4) {  // front collision
					energyDelta -= 4*ce.getNormalImpulse();
					aModifier = 0.2f;
				} else if (angle <= MathUtils.PI34) { // side collision
					energyDelta -= 8*ce.getNormalImpulse();
					aModifier = 0.3f;
				} else { // rear collision
					energyDelta -= 16*ce.getNormalImpulse();
					aModifier = 0.4f;
				}
				
				// the valence modifier is contingent on whether or not
				// we actually wanted to hit this other agent.
				String target = agentSpace.get(Variable.target).get(String.class);
				if (target.equals(ce.getOther(obj).getName())) { 
					vModifier = 0.3f;
				} else { 
					vModifier = -0.3f;
				}
				
//				logger.debug("@@@@@@@ - " + ce.getNormalImpulse() + " energy: " + energyDelta + " arousal: " +  aModifier + " valence: " + vModifier);
				break;
			}
			
			agentSpace.getBounded(Variable.arousal).change(ce.getNormalImpulse()*aModifier);
			agentSpace.getBounded(Variable.valence).change(ce.getNormalImpulse()*vModifier);
		}
	
		if (energyDelta != 0) { 
			Event e = new Event(EventType.ENERGY_EVENT);
			e.addRecipient(obj);
			e.addParameter("amount", energyDelta);
			EventManager.inst().dispatch(e);
		}
	}
	
}