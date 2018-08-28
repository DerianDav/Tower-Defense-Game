package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@SuppressWarnings("serial")
public class PoisonTower extends Tower {
	

	/**
	 * Creates the new tower with the given x and y coord
	 * @param x - x location of tower
	 * @param y - y location of tower
	 */

	public PoisonTower(int x, int y, int scaleSize) {
		//name cost range damage delay
		super("Poison Tower", 250, 1,0,30, x, y, scaleSize); 

	}
	
	/**
	 * Draws the poison lazer from the tower to the given point
	 */
	@Override
	public void drawProjectile(GraphicsContext gc, double x, double y) {
		gc.setStroke(Color.GREEN);
		gc.setLineWidth(5);
		gc.strokeLine(getScaleSize()*getX() + (getScaleSize()/2), getScaleSize()*getY() + (getScaleSize()/2), x, y);
	}
	
	/**
	 * Adds stats to the tower and changes image
	 */
	@Override
	public void upgradeTower() {
		//range,damage,delay
		super.upgradeTower(3,100,10);
	}
	
	/**
	 * activateTower will deal damage not including the poison 
	 * will set poison to the enemy
	 */
	@Override
	public Enemy activateTower(Enemy curEnemy) {
		curEnemy.takeDamage(super.getDamage());
		curEnemy.poison();
		return curEnemy;
	}


	
	
}
