package model;


import java.awt.Toolkit;
import java.io.Serializable;

import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * 
 * @author Derian Davila Acuna
 *
 */
@SuppressWarnings("serial")
public abstract class Tower implements Serializable{
	
	private String name;
	
	private int damage;
	private int range;
	private int delay;
	private int x, y;
	
	private int towerCost;
	private int upgradeCost;
	private int towerLvl;
	
	private int scaleSize;
	
	private double rotateVal = 0;
	private int curWaitTimer;
	
	
	/**
	 * @param name the name of the tower
	 * @param towerCost cost of placing the tower
	 * @param range the distance the tower can shoot
	 * @param damage the amount of damage the tower does
	 * @param delay how fast the tower shoots in seconds
	 * @param row the position of the tower's top left position on the map
	 * @param col the position of the tower's top left position on the map
	 */
	public Tower(String name, int towerCost,int range, int damage, int delay, int x, int y, int scaleSize) {
		this.delay = delay;
		this.x = x;
		this.y = y;
		this.range = range;
		this.damage = damage;
		this.name = name;
		this.towerCost = towerCost;
		this.upgradeCost = 2*towerCost;
		this.scaleSize = scaleSize;
		
		towerLvl = 1;
		
		curWaitTimer = 0;
	}
	

	/**
	 * updates curWaitTimer
	 */
	public void updateCounter() {
		curWaitTimer++;
	}
	
	/**
	 * After shooting, counter should be set back to 0
	 */
	public void resetCounter() {
		curWaitTimer = 0;
	}
	/**
	 * 
	 * @param enemy current enemy checking
	 * @return returns whether or not the enemy is in range
	 */
	public boolean inRange(Enemy enemy) {
		if(enemy.getY() > y+range)//add 10 if mobs are 10x10
			return false;
		if(enemy.getY() < y-range)
			return false;
		if(enemy.getX() > x+range)
			return false;
		if(enemy.getX() < x-range)
			return false;
		return true;
	} 
	
	
	
	/**
	 * Returns true if tower can shoot in current tic
	 * @return bool - true to shoot
	 */
	public boolean readyToShoot() {
		//System.out.println(curWaitTimer);
		if(curWaitTimer < delay) {
			//System.out.println("delay: " + delay + " and tic: " + curWaitTimer);
			//System.out.println("not shooting");
			return false;
		}
		return true;
	}
	
	/**
	 * The enemy passed into this method will be modified with any damage taken or any other effects a tower can have on the enemy itself
	 * @param curEnemy current Enemy that is being target by the tower
	 * @return modified version of curEnemy 
	 */
	public abstract Enemy activateTower(Enemy curEnemy);
	
	/**
	 * draws the tower image
	 * 
	 * @param gc - graphics
	 */
	public void draw(GraphicsContext gc, Image image) {
		/* LOL JK DON'T NEED ANY OF THIS
		double fourth = getRotateVal()%90;
		double turnRatio;
		//double turnRatio = ((rotateVal%90)%45.000)/45;
		if (fourth < 45)
			turnRatio = fourth/45;
		else {
			turnRatio = (90-fourth)/45;
		}
		double sideLen = getScaleSize() + (turnRatio * hypotMinusSide);
		//System.out.println("orig " + getRotateVal() + " turned: " + turnRatio);
		*/
		// Code for rotating an image. Used in drawing tower
		Image orig = image;
		ImageView iv = new ImageView(orig);
		iv.setRotate(getRotateVal());
		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		//System.out.println(scaleSize);
		params.setViewport(new Rectangle2D(0, 0, 75, 75));
		Image rotIm = iv.snapshot(params, null);
		
		gc.drawImage(rotIm, scaleSize*getX(), scaleSize*getY(), scaleSize, scaleSize);
	}
	
	/**
	 * draws the tower's projectile
	 * 
	 * @param gc
	 * @param x coordinate of the enemy
	 * @param y coordinate of the enemy
	 */
	
	public abstract void drawProjectile(GraphicsContext gc, double x, double y);
	
	/**
	 * upgrades the tower to a more powerful version of itself
	 */
	public abstract void upgradeTower();
	

	/**
	 * When the tower is upgraded, this will change the old tower damage,delay, and range values to the new specified values
	 * Does not need to be different then the old values
	 * @param damage new tower damage
	 * @param delay new tower shooting delay
	 * @param range new tower range
	 */
	protected void upgradeTower(int range, int damage, int delay) {
		this.damage = damage;
		this.delay = delay;
		this.range = range;
		this.upgradeCost *= 2;
		towerLvl++;
	}

	//GETTERS
	
	/**
	 * 
	 * @return returns the x cord the tower is located in
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * 
	 * @return returns the y cord the tower is located in
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * 
	 * @return returns the tower damage
	 */
	public int getDamage() {
		return damage;
	}
	
	/**
	 * 
	 * @return returns the name of the tower
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return returns the range of the tower
	 */
	public int getRange() {
		return range;
	}
	
	/**
	 * 
	 * @return returns the cost of placing a tower
	 */
	public int getTowerCost() {
		return towerCost;
	}
	
	/**
	 * 
	 * @return returns the cost of upgrading a tower
	 */
	public int getUpgradeCost() {
		return upgradeCost;
	}
	
	/**
	 * 
	 * @return returns the current level of the tower. The base level is level 1.
	 */
	public int getTowerLvl() {
		return towerLvl;
	}
	
	/**
	 * get the scale size
	 * @return scaleSize - the pixel count per grid space
	 */
	public int getScaleSize() {
		return scaleSize;
	}

	/**
	 * gets the rotation value
	 * @return - angle between tower and enemy
	 */
	public double getRotateVal() {
		return rotateVal;
	}
	
	/**
	 * gets the delay value
	 * @return delay - amount of time the tower spends between shots
	 */
	public int getDelay() {
		return delay;
	}
	
	/**
	 * Updates the angle with the baddie given
	 * @param baddie
	 */
	public void setAngle(Enemy baddie) {
		double newX = baddie.getRealX() - getX()*getScaleSize();
		double newY =  getY()*getScaleSize() - baddie.getRealY();
		double ret = 180 - Math.toDegrees(Math.atan2(newY, newX));
		//System.out.println("x: " + newX + " y:" + newY + " angle:" + ret);
		rotateVal = ret;
	}
	
	/**
	 * Draws animation of tower blowing up.
	 */
	public void explode(GraphicsContext gc, Image characterImage, Image explodeImage) {
		draw(gc, characterImage);
		gc.drawImage(explodeImage, getX()*scaleSize, getY()*scaleSize, scaleSize, scaleSize);
	}


	public void changeScale(int scaleSize) {
		this.scaleSize = scaleSize;
		
	}
}
