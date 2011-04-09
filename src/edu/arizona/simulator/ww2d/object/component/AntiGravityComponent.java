package edu.arizona.simulator.ww2d.object.component;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.dom4j.Element;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import edu.arizona.simulator.ww2d.blackboard.Blackboard;
import edu.arizona.simulator.ww2d.blackboard.spaces.Space;
import edu.arizona.simulator.ww2d.object.GameObject;
import edu.arizona.simulator.ww2d.object.PhysicsObject;
import edu.arizona.simulator.ww2d.utils.enums.Variable;

public class AntiGravityComponent extends Component {

	private static Logger logger = Logger.getLogger( AntiGravityComponent.class );
	
	private Body _body;
	private Space _systemSpace;
	
	public AntiGravityComponent(GameObject owner) {
		super(owner);
		
		_body = ((PhysicsObject) owner).getBody();
		_systemSpace = Blackboard.inst().getSpace("system");
	}

	@Override
	public void update(int elapsed) {
		float mass = _body.getMass();
		Vec2 gravity = _systemSpace.get(Variable.physicsGravity).get(Vec2.class);
		Vec2 antiGravity = new Vec2(gravity);
		
		// multiply by:
		//    -1 to make it opposite direction
		//    mass to scale to mass of object 
		antiGravity.mulLocal(-1*mass);
		
		// logger.log(Priority.DEBUG, "gravity: " + gravity + ", antiGravity: " + antiGravity);
		
		_body.applyForce(antiGravity, _body.getPosition());
	}

	@Override
	public void fromXML(Element e) {
		// Nothing to parse here
	}

}
