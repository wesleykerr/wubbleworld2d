package edu.arizona.simulator.ww2d.external;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.verbs.shared.OOMDPObjectState;
import edu.arizona.verbs.shared.OOMDPState;
import edu.arizona.verbs.shared.Relation;

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

	public static void testAgentObstacle() { 
		OOMDPObjectState obj1 = new OOMDPObjectState("agent1", "agent");
		obj1.setAttribute("x", "50");
		obj1.setAttribute("y", "50");
		obj1.setAttribute("heading", "0");
		obj1.setAttribute("vx", "0");
		obj1.setAttribute("vy", "0");
		obj1.setAttribute("vtheta", "0");
		obj1.setAttribute("shape-type", "circle");
		obj1.setAttribute("radius", "0.25");

//		OOMDPObjectState obj2 = new OOMDPObjectState("agent2", "agent");
//		obj2.setAttribute("x", "20");
//		obj2.setAttribute("y", "10");
//		obj2.setAttribute("heading", "3.14");
//		obj2.setAttribute("shape-type", "circle");
//		obj2.setAttribute("radius", "0.25");
		
		OOMDPObjectState obj2 = new OOMDPObjectState("target", "obstacle");
		obj2.setAttribute("x", "75");
		obj2.setAttribute("y", "50");
		obj2.setAttribute("heading", "0");
		obj2.setAttribute("vx", "0");
		obj2.setAttribute("vy", "0");
		obj2.setAttribute("vtheta", "0");
		obj2.setAttribute("shape-type", "circle");
		obj2.setAttribute("radius", "0.5");
		
		List<OOMDPObjectState> objects = new ArrayList<OOMDPObjectState>();
		objects.add(obj1);
		objects.add(obj2);
		
		WW2DEnvironment env = new WW2DEnvironment(true);
		OOMDPState state = env.initializeEnvironment(objects);

//		System.out.println("Initialization complete...");
//		System.out.println(state.toString());
//		
//		System.out.println("Printing actions...");
//		List<String> actions = env.getActions();
//		System.out.println(actions.size() + " actions...");
//		System.out.println(env.getActions());

		System.out.println("BEGIN PERFORM");
		System.out.println(state);
		for (int i = 0; i < 9; i++) {
			OOMDPState newState = env.performAction("agent1 1000"); //;agent2 0000");
			System.out.println(newState);
		}
		
		System.out.println("BEGIN SIMULATE");
		OOMDPState simState = env.initializeEnvironment(objects);
		System.out.println(simState);
		for (int i = 0; i < 10; i++) {
//			System.out.println("================ LOOP " + i);
//			simState = env.simulateAction(simState, "agent1 1000");
			simState = env.simulateAction("agent1 1000");
//			System.out.println("TEST 1: " + simState);
//			env.simulateAction(simState, "agent1 0100");
//			System.out.println("TEST 2: " + simState);
			System.out.println(simState);
		}
		
		env.cleanup();
	}
	
	public static void test3Agent() { 
		OOMDPObjectState obj1 = new OOMDPObjectState("agent1", "agent");
		obj1.setAttribute("x", "10");
		obj1.setAttribute("y", "10");
		obj1.setAttribute("heading", "0.00");
		obj1.setAttribute("shape-type", "circle");
		obj1.setAttribute("radius", "0.25");

		OOMDPObjectState obj2 = new OOMDPObjectState("agent2", "agent");
		obj2.setAttribute("x", "20");
		obj2.setAttribute("y", "10");
		obj2.setAttribute("heading", "3.14");
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
		
		obj1.setAttribute("vx", "-30.00");
		obj1.setAttribute("vy", "0.00");
		obj1.setAttribute("vtheta", "0.000");

		obj2.setAttribute("vx", "1.00");
		obj2.setAttribute("vy", "0.00");
		obj2.setAttribute("vtheta", "0.000");

		System.out.println(env.simulateAction(new OOMDPState(objects, new ArrayList<Relation>()), "agent1 1000;agent2 1000"));
		
		env.cleanup();
	}
	
	
	public static void main(String[] args) { 
//		test1Agent();
		testAgentObstacle();
//		test3Agent();
	}
}
