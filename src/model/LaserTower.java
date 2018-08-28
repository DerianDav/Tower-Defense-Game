package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


@SuppressWarnings("serial")
public class LaserTower extends Tower{

	public LaserTower(int x, int y, int scaleSize) {
		//String name, int towerCost,int range, int damage, int delay, int x, int y, Image image
		super("Laserbeam Tower", 80, 2,50,1, x, y,scaleSize); 
	}
	
	/**
	 * Draws the laser from the tower to the location given
	 */
	@Override
	public void drawProjectile(GraphicsContext gc, double x, double y) {
		gc.setStroke(Color.RED);
		gc.setLineWidth(2);
		gc.strokeLine(getScaleSize()*getX() + (getScaleSize()/2), getScaleSize()*getY() + (getScaleSize()/2), x, y);
	}
	
	/**
	 * Boosts the towers stats and changes its image
	 */
	@Override
	public void upgradeTower() {
		//range,damage,delay
		super.upgradeTower(4,80,1);
	}
	
	/**
	 * activateTower will only deal the damage
	 */
	@Override
	public Enemy activateTower(Enemy curEnemy) {
		curEnemy.takeDamage(super.getDamage());
		return curEnemy;
	}
	
	
	
}
