package edu.arizona.simulator.ww2d.external;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.verbs.mdp.OOMDPObjectState;
import edu.arizona.verbs.mdp.OOMDPState;

public class TestHarness {
	
	public static void test1Agent() { 
		// Create simple initial environment. Single agent as a circle.
		OOMDPObjectState obj1 = new OOMDPObjectState("agent1", "agent");
		obj1.setAttribute("x", "10");
		obj1.setAttribute("y", "10");
		obj1.setAttribute("angle", "0.00");
		obj1.setAttribute("shape-type", "circle");
		obj1.setAttribute("radius", "0.25");

		List<OOMDPObjectState> objects = new ArrayList<OOMDPObjectState>();
		objects.add(obj1);
		
		WW2DEnvironment env = new WW2DEnvironment(true);
		OOMDPState state = env.initializeEnvironment(objects);

		System.out.println("Initialization complete...");
		System.out.println(state.toString());
		
		System.out.println("Printing actions...");
		System.out.println(env.getActions());
		
		System.out.println(env.performAction("agent1 1000"));
		System.out.println(env.performAction("agent1 1000"));
		System.out.println(env.performAction("agent1 1000"));
		System.out.println(env.performAction("agent1 1000"));
		
		env.cleanup();
	}

	public static void test2Agent() { 
		OOMDPObjectState obj1 = new OOMDPObjectState("agent1", "agent");
		obj1.setAttribute("x", "10");
		obj1.setAttribute("y", "10");
		obj1.setAttribute("angle", "0.00");
		obj1.setAttribute("shape-type", "circle");
		obj1.setAttribute("radius", "0.25");

		OOMDPObjectState obj2 = new OOMDPObjectState("agent2", "agent");
		obj2.setAttribute("x", "20");
		obj2.setAttribute("y", "10");
		obj2.setAttribute("angle", "3.14");
		obj2.setAttribute("shape-type", "circle");
		obj2.setAttribute("radius", "0.25");
		
		List<OOMDPObjectState> objects = new ArrayList<OOMDPObjectState>();
		objects.add(obj1);
		objects.add(obj2);
		
		WW2DEnvironment env = new WW2DEnvironment(true);
		OOMDPState state = env.initializeEnvironment(objects);

		System.out.println("Initialization complete...");
		System.out.println(state.toString());
		
		System.out.println("Printing actions...");
		List<String> actions = env.getActions();
		System.out.println(actions.size() + " actions...");
		System.out.println(env.getActions());

		System.out.println(env.performAction("agent1 1000;agent2 1000"));
		System.out.println(env.performAction("agent1 1000;agent2 1000"));
		System.out.println(env.performAction("agent1 1000;agent2 1000"));
		System.out.println(env.performAction("agent1 1000;agent2 1000"));
		
//		env.cleanup();
	}
	
	public static void main(String[] args) { 
//		test1Agent();
		test2Agent();
	}
}
