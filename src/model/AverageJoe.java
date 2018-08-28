package model;

import view.Map;

/**
 * FatBoy Enemy. Heavy and tough
 * @author Kyle
 *
 */
@SuppressWarnings("serial")
public class AverageJoe extends Enemy {
	
	public AverageJoe(Map map,int scaleSize) {
		//Health, weight, armor
		super(200, 10, 7, 15, 10, "Average Joe", map, scaleSize);
		//setImage("file:images/joeSprite.png");
	}
	
}
