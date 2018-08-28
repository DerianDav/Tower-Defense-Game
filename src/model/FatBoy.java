package model;

import view.Map;

/**
 * FatBoy Enemy. Heavy and tough
 * @author Kyle
 *
 */
@SuppressWarnings("serial")
public class FatBoy extends Enemy {
	
	public FatBoy(Map map, int scaleSize) {
		//Health, weight, armor, cash reward
		super(350, 25, 10, 30, 20, "Fat Boy", map, scaleSize);
		//setImage("file:images/fatSprite.png");
	}
	
	
}
