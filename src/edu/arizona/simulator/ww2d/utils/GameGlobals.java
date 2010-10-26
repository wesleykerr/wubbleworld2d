package edu.arizona.simulator.ww2d.utils;

import java.text.NumberFormat;

public class GameGlobals {
	
	public static NumberFormat nf;
	
	public static boolean record = false;

    static { 
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
    }
	
}
