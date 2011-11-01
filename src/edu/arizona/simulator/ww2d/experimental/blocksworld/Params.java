package edu.arizona.simulator.ww2d.experimental.blocksworld;

public class Params {
	private boolean physics;
	private int duration;
	private String levelFile;
	
	public Params(String levelFile, boolean physics, int duration){
		this.levelFile = levelFile;
		this.physics = physics;
		this.duration = duration;
	}
	
	public boolean getPhysics(){
		return physics;
	}
	
	public int getDuration(){
		return duration;
	}
	
	public String getLevel(){
		return levelFile;
	}
}
