package model;

import java.io.Serializable;

/**
 * Player Class
 * 		Contains the Lives and Moneys that the current player has
 * 
 * @author Braxton Lazar
 * @version 1.0
 * @since 11-7-2017
 *
 */

@SuppressWarnings("serial")
public class Player implements Serializable{
	private int health;
	private int moneys;
	private int totalMoneys;
	private int mapNumber;
	private int waveNumber;
	
	/**
	 * Creates player with given health and staring money
	 */
	public Player(){
		health = 100;
		moneys = 100;
		totalMoneys = 100;
	}
	
	/**
	 * Returns current life player has
	 * @return health - health as int
	 */
	public int getHealth(){
		return health;
	}
	
	/**
	 * Returns how much money the player has
	 * @return
	 */
	public int getMoneys(){
		return moneys;
	}
	
	/**
	 * Returns the total money received over lifetime of player
	 * @return
	 */
	public int getTotalMoneys() {
		return totalMoneys;
	}
	
	/**
	 * Method for subtracting cost from the moneys
	 * 
	 * @param cost
	 */
	public void subtractCost(int cost){
		moneys -= cost;
	}
	
	/**
	 * Method for added the paycheck from an enemy to the moneys of the player to spend
	 * 
	 * @param paycheck
	 */
	public void addMoneys(int paycheck){
		moneys += paycheck;
		totalMoneys += paycheck;
	}
	
	/**
	 * Checks if the player has any lives left
	 * 
	 * @return
	 */
	public boolean isDead(){
		return health <= 0;
	}
	
	/**
	 * Removes health from player
	 * @param damage - ammount of health to remove
	 */
	public void takeDamage(int damage){
		if(damage > health)
			health = 0;
		else
			health -= damage;
	}

	/**
	 * Returns which map player is on
	 * @return
	 */
	public int getMapNumber() {
		return mapNumber;
	}

	/**
	 * Sets the map the player is on
	 * @param mapNumber - map player is on
	 */
	public void setMapNumber(int mapNumber) {
		this.mapNumber = mapNumber;
	}
	
	public int getWaveNumber(){
		return waveNumber;
	}
	
	public void setWaveNumber(int w){
		waveNumber = w;
	}
	
	public void incrementWave(){
		waveNumber++;
	}
}
