package edu.arizona.simulator.ww2d.object.component;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Segment;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.RaycastResult;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.entry.ValueEntry;
import edu.arizona.simulator.ww2d.blackboard.spaces.AgentSpace;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.SonarReading;
import edu.arizona.simulator.ww2d.utils.enums.ObjectType;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

/**
 * Attaches a set of sonar to the Entity.  These
 * will start at the orientation and go 45 degrees
 * in both directions.
 * 
 * One key is that we will be operating on a subset of all possible
 * shapes in this class.  We will determine the candidate Shapes that
 * <bold>could</bold> be close enough.  
 * @author wkerr
 *
 */
public class PerceptionComponent extends Component {
    private static Logger logger = Logger.getLogger( PerceptionComponent.class );

	/** the number of degrees we move for each sonar */
	public static final int DELTA_DEGREES = 1;  // don't screw this up since angles are integer.
	public static final float PI2 = (float) (Math.PI / 2.0);
	
	private PhysicsObject _obj;
	private List<SonarReading> _sonar;
	
	private Shape _sonarShape;
	
	public PerceptionComponent(GameObject owner) { 
		super(owner);
		_obj = (PhysicsObject) owner;
		_sonar = new LinkedList<SonarReading>();

		_updatePriority = 0;
		
		for (int angle = 45; angle > -46; angle -= DELTA_DEGREES) {
			int name = angle < 0 ? angle+360 : angle;
			_sonar.add(new SonarReading(name+"", (float) Math.toRadians(angle)));
		}
		
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		agentSpace.put(Variable.sonar, new ValueEntry(_sonar));
	}

	@Override
	public void update(int elapsed) {
		Set<PhysicsObject> visibleSet = new HashSet<PhysicsObject>();

		Space systemSpace = Blackboard.inst().getSpace("system");
		World world = systemSpace.get(Variable.physicsWorld).get(World.class);
		
		// the near list should be populated from the distance results
		// now we can test all of the sonar to see if any of the near
		// entities are within our visual area.
		Segment s = new Segment();
		RaycastResult rs = new RaycastResult();
		
		for (SonarReading sr : _sonar) { 
			sr.reset();
			
			float sonarAngle = _obj.getHeading() + sr.getAngle();
			
			Vec2 ep1 = _obj.getPPosition();
			Vec2 ep2 = new Vec2(
					ep1.x + (float) (SonarReading.DISTANCE*Math.cos(sonarAngle)), 
					ep1.y + (float) (SonarReading.DISTANCE*Math.sin(sonarAngle)));

			s.p1.set(ep1);
			s.p2.set(ep2);
						
			Shape collision = world.raycastOne(s, rs, false, null);
			if (collision != null) {
				PhysicsObject obj = (PhysicsObject) collision.getUserData();
				visibleSet.add(obj);
				
				Vec2 cp = s.p1.mul(1-rs.lambda).add(s.p2.mul(rs.lambda));
				
				sr.setDistance(cp.sub(s.p1).length());
				sr.setInteracts(obj);
			}
		}			

		Vec2 position = _obj.getPPosition();
		AABB aabb = new AABB(position.sub(new Vec2(5,5)), position.add(new Vec2(5,5)));
		Shape[] near = world.query(aabb, 30);
		
		Set<PhysicsObject> auditory = new HashSet<PhysicsObject>();
		Set<PhysicsObject> scent = new HashSet<PhysicsObject>();
		for (Shape shape : near) { 
			PhysicsObject obj = (PhysicsObject) shape.getUserData();
			if (obj == _obj)
				continue;
			
			// TODO: Actually add other dynamic objects... only if they are moving.
			if (obj.getType() == ObjectType.reactiveAgent || obj.getType() == ObjectType.cognitiveAgent)
				auditory.add(obj);
			
			if (obj.getType() == ObjectType.food) {
				// make sure that there is food in the store before
				// we actually add this one.
				float store = obj.getUserData("store", Float.class);
				if (store > 0) { 
					scent.add(obj);
				}
			}
		}
		
		// Now that we have finished we need to store off the visible set information
		// write to our space in the blackboard....
		AgentSpace agentSpace = Blackboard.inst().getSpace(AgentSpace.class, _parent.getName());
		agentSpace.addSelfExperience(_obj);
		agentSpace.addVisualExperience(_obj, visibleSet);
		agentSpace.addAuditoryExperience(_obj, auditory);
		agentSpace.addScentExperience(_obj, scent);
	}
	
	public Shape getShape() { 
		return _sonarShape;
	}
	
	
	/**
	 * Override this method if the component is
	 * responsible for drawing anything....
	 */
	public void render(Graphics g) { 
		Space systemSpace = Blackboard.inst().getSpace("system");
		float scale = systemSpace.get(Variable.physicsScale).get(Float.class);
		
		Vec2 p = _parent.getPosition();
		float h = _parent.getHeading();

		Color c = new Color(Color.white);
		c.a = 0.25f;
		
		g.setColor(c);
		for (SonarReading sr : _sonar) { 
			float x = (scale * sr.getDistance()) * (float) Math.cos(h+sr.getAngle());
			float y = (scale * sr.getDistance()) * (float) Math.sin(h+sr.getAngle());
			
			g.drawLine(p.x, p.y, p.x + x, p.y + y);
		}
		
		g.setColor(Color.black);
		g.drawRect(p.x-(5*scale), p.y-(5*scale), 10*scale, 10*scale);
		
		
	}

	/**
	 * Get access to the SonarReadings
	 * @return
	 */
	public List<SonarReading> getSonar() { 
		return _sonar;
	}

	@Override
	public void fromXML(Element e) {
		// TODO Auto-generated method stub
		
	}
}
