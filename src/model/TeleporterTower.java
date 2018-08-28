package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@SuppressWarnings("serial")
public class TeleporterTower extends Tower {


	/**
	 * Creates new teleport tower
	 * @param x - x loc of tower
	 * @param y - y loc of tower
	 */
	public TeleporterTower(int x, int y, int scaleSize) {
		//name cost range damage delay
		super("Teleporter Tower",100, 1,0,30, x, y, scaleSize); 
	}
	
	/**
	 * Draws the teleport lazer to given point
	 */
	@Override
	public void drawProjectile(GraphicsContext gc, double x, double y) {
		gc.setStroke(Color.YELLOW);
		gc.setLineWidth(10);
		gc.strokeLine(getScaleSize()*getX() + (getScaleSize()/2), getScaleSize()*getY() + (getScaleSize()/2), x, y);
	}
	
	/**
	 * Adds stats to tower
	 */
	@Override
	public void upgradeTower() {
		//range,damage,delay
		super.upgradeTower(2,0,20);
		
	}
	
	/**
	 * activateTower will only send the enemy back to the start of the enemy pathway
	 */
	@Override
	public Enemy activateTower(Enemy curEnemy) {
		curEnemy.backToStartingPoint();
		return curEnemy;
	}

}
