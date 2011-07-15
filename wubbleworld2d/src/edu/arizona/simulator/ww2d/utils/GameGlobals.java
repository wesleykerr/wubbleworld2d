package edu.arizona.simulator.ww2d.utils;

import java.text.NumberFormat;

import org.jbox2d.common.Vec2;

public class GameGlobals {
	
	public static NumberFormat nf;
	
	public static boolean record = true;

	public static boolean cameraMode = true;
	public static Vec2 cameraPos = new Vec2(0,0);
	public static float cameraScale = 0.2f; 
	
    static { 
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
    }
	
}
