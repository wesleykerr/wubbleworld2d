package edu.arizona.simulator.ww2d.utils.enums;

public enum ObjectType {
	visual,
	cognitiveAgent,
	reactiveAgent,

	dynamic,
	// these could be things that "look" like dynamic objects
	// but they definitely don't behave like them.
	obstacle,

	// these are just the walls.
	wall,
	food,
}
