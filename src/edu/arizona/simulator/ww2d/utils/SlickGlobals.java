package edu.arizona.simulator.ww2d.utils;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class SlickGlobals {
	public static UnicodeFont textFont;

	static { 
		java.awt.Font font = new java.awt.Font("Courier", java.awt.Font.PLAIN, 9);
		textFont = new UnicodeFont(font, 9, false, false);
		try {
			textFont.getEffects().add(new ColorEffect(java.awt.Color.white)); 
			textFont.addAsciiGlyphs(); 
			textFont.loadGlyphs();
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
