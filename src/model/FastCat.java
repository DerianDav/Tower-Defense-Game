package model;

import view.Map;

/**
 * FatBoy Enemy. Heavy and tough
 * @author Kyle
 *
 */
@SuppressWarnings("serial")
public class FastCat extends Enemy {
	
	public FastCat(Map map, int scaleSize) {
		//Health, weight, armor, money, damage
		super(100, 5, 5, 10, 5,"Fast Cat", map,scaleSize);
		//setImage("file:images/catSprite.png");
	}
	
	
	
}
