package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import model.AverageJoe;
import model.BossMonster;
import model.Enemy;
import model.FastCat;
import model.FatBoy;
import model.LaserTower;
import model.Player;
import model.PoisonTower;
import model.TeleporterTower;
import model.Tower;
import view.Map;
import view.Tiles;

/**
 * Holds info about game and controls it
 * @author Kyle
 *
 */
public class GameBoy {
	private ArrayList<Enemy> enemies;
	private ArrayList<Tower> towers;
	private Player player;
	private int gameTic;
	private Map map;
	private GraphicsContext gc;
	private int scaleSize;
	
	//For multiplayer
	private boolean playerDC = false;
	
	//Variables for generating waves
	private Random rand = new Random();
	private int waveDifficulty = 1;
	private int waveNumber;
	private final int CATCUTOFF = 35;
	private final int JOECUTOFF = 75;
	private final int FATCUTOFF = 100;
	
	
	private boolean waveDone;
	private int enemiesToSpawn;
	private int bossWave = 7;
	BossMonster boss = null;
	
	//Stats
	private int enemiesKilled = 0;
	private int towersBought = 0;
	
	//Images
	Image fatBoy = new Image("file:images/fatSprite.png");
	Image fastCat = new Image("file:images/catSprite.png");
	Image averageJoe = new Image("file:images/joeSprite.png");
	Image bossImage = new Image("file:images/boss.png");
	Image laserTower1 = new Image("file:images/laserTower1.png");
	Image laserTower2 = new Image("file:images/laserTower2.png");
	Image poisonTower1 = new Image("file:images/poisonTower1.png");
	Image poisonTower2 = new Image("file:images/poisonTower2.png");
	Image teleportTower1 = new Image("file:images/teleportTower1.png");
	Image teleportTower2 = new Image("file:images/teleportTower2.png");
	Image explodeImage = new Image("file:images/explode.png");
	
	
	/**
	 * Constructor for game
	 * @param map - map to play game on
	 * @param player - player
	 */
	public GameBoy(int scaleSize, Map map, Player player, GraphicsContext g) {
		this.scaleSize = scaleSize;
		this.map = map;
		this.player = player;
		this.gameTic = 0;
		this.gc = g;
		this.waveNumber = 0;
		this.player.setWaveNumber(0);
		enemies = new ArrayList<Enemy>();
		towers = new ArrayList<Tower>();
	}
	
	/////////////////////////// Public Methods ///////////////////////////////////

	
	/**
	 * Runs a single tic of the game. Runs all required actions
	 */
	public void singleTic() {
		addEnemy();
		moveEnemies();
		Collections.sort(enemies);
		bossDestroyTowers();
		rotateTowers();
		drawObjects();
		towersShoot();
		enemiesDead();
		checkWaveDone();
		enemiesPassed();
		objectTicUpdate(); //updates tic for towers and enemies
		gameTic++;
	}
	
	/**
	 * Determines when a new wave can start
	 * @return if can generate next wave
	 */
	public boolean canGenWave() {
		return waveDone;
	}
	
	/**
	 * Get's the enemies currently spawned
	 * @return enemies - arraylist of enemies
	 */
	public ArrayList<Enemy> getEnemies(){
		return enemies;
	}
	
	/**
	 * Gets towers that are on the mao
	 * @return towers - arraylist of towers
	 */
	public ArrayList<Tower> getTowers(){
		return towers;
	}
	
	/**
	 * Used for loading in enemies from saved game state
	 * @param enemies - arrayList of enemies to put on map
	 */
	public void setEnemies(ArrayList<Enemy> enemies){
		this.enemies = enemies;;
	}
	
	/**
	 * Used for loading in towers from saved game state
	 * @param towers - arrayList of towers to put on map
	 */
	public void setTowers(ArrayList<Tower> towers){
		this.towers = towers;
	}
	
	public void setMap(Map m){
		map = m;
	}
	
	public void setPlayer(Player p){
		player = p;
	}
	
	/**
	 * Based on waveDifficulty, that many enemies will spawn
	 */
	public void generateWave() {
		if (!waveDone)
			return;
		if (waveNumber == bossWave) {
			boss = new BossMonster(map,scaleSize);
			enemies.add(boss);
		}
		else {
			waveDifficulty *= 2;
			enemiesToSpawn = waveDifficulty;
		}
		waveNumber++;
		player.incrementWave();
		waveDone = false;
		
	}
	
	/**
	 * checks to see if the player has died
	 * @return bool - player dead
	 */
	public boolean playerDead() {
		return player.isDead();
	}
	
	/**
	 * Gets the wave the boss spawns on
	 * @return bossWave - wave the boss spawns on
	 */
	public int getBossWaveNumber() {
		return bossWave;
	}
	
	/**
	 * player wins if the player gets to wave 7
	 * @return if the player has won
	 */
	public boolean playerWon() {
		if(waveNumber > bossWave +1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Used when the game has won. Draws explosions on boss ship
	 */
	public void fireWorks() {
		//System.out.println("fireworks!");
		boss.fireworks(gc, explodeImage);
	}
	
	/**
	 * checks if the player is dead or the player has won
	 * @return if the game is over
	 */
	public boolean gameOver() {
		if(playerDead() || playerWon() || playerDC)
			return true;
		return false;
	}
	
	/**
	 * Adds a new tower 
	 * @param newTower - new tower to be added to the list
	 */
	public void addTower(Tower newTower) {
		if(newTower.getTowerCost() > player.getMoneys())
			return;
		
		if(!validTowerLocation(newTower.getX(), newTower.getY()))
			return;
		
		player.subtractCost(newTower.getTowerCost());
		towers.add(newTower);
		towersBought++;

	}
	
	/**
	 * Determines if you can place a tower at the specified location
	 * @param x coordinate of where the user wants to place the tower
	 * @param y coordinate of where the user wants to place the tower
	 * @return returns true or false if the location of the potential tower is in a valid spot to place one
	 */
	public boolean validTowerLocation(int x, int y) {
		if(map.get(x,y) == Tiles.PATH || map.get(x,y) == Tiles.UNBUILDABLE)
			return false;
		for(Tower tower : towers ) {
			if(tower.getX() == x && tower.getY() == y)
				return false;
		}
		return true;
	}
	
	/**
	 * Getter for current wave number
	 * @return waveNumber - current wave
	 */
	public int getWaveNumber() {
		return waveNumber;
	}
	
	public void setWaveNumber(int w){
		waveNumber = w;
	}

	/**
	 * Given a specified location this returns the tower that is located at (x,y). 
	 * If there is no tower at the location given then this returns null.
	 * @param x coordinate of potential enemy location
	 * @param y coordinate of potential enemy location
	 * @return the tower located at (x,y) (if there is one) 
	 */
	public Tower getTower(int x, int y) {
		for(Tower tower : towers ) {
			if(tower.getX() == x && tower.getY() == y)
				return tower;
		}
		return null;
	}

	/**
	 * Given a specified location this returns the enemy that is located at (x,y). 
	 * If there is no enemy at the location given then this returns null.
	 * @param x coordinate of potential enemy location
	 * @param y coordinate of potential enemy location
	 * @return the enemy located at (x,y) (if there is one) 
	 */
	public Enemy getEnemy(int x, int y) {
		for(Enemy enemy : enemies) {
			if(enemy.getX() == x && enemy.getY() == y-1)
				return enemy;
		}
		return null;
	}
	
	/**
	 * @return get the amount of enemies killed
	 */
	public int getEnemiesKilled() {
		return enemiesKilled;
	}
	
	/**
	 * @return get the amount of towers bought
	 */
	public int getTowersBought() {
		return towersBought;
	}
	
	/**
	 * @return if the other person your playing with has dc'ed
	 */
	public boolean isPlayerDC() {
		return playerDC;
	}
	
	/**
	 * sets the other player as DC thus causing a game over screen
	 */
	public void setGameOver() {
		playerDC = true;
	}
	
	/**
	 * Draws all active objects on the screen
	 */
	public void drawObjects(){
		for (Tower tower : towers){
			if(tower.getClass() == LaserTower.class && tower.getTowerLvl() == 1)
				tower.draw(gc, laserTower1);
			else if(tower.getClass() == LaserTower.class && tower.getTowerLvl() == 2)
				tower.draw(gc, laserTower2);
			else if(tower.getClass() == PoisonTower.class && tower.getTowerLvl() == 1)
				tower.draw(gc, poisonTower1);
			else if(tower.getClass() == PoisonTower.class && tower.getTowerLvl() == 2)
				tower.draw(gc, poisonTower2);
			else if(tower.getClass() == TeleporterTower.class && tower.getTowerLvl() == 1)
				tower.draw(gc, teleportTower1);
			else if(tower.getClass() == TeleporterTower.class && tower.getTowerLvl() == 2)
				tower.draw(gc, teleportTower2);
		}
		for(Enemy enemy : enemies){
			if(enemy.getClass() == FatBoy.class)
				enemy.draw(gc, fatBoy);
			else if(enemy.getClass() == FastCat.class)
				enemy.draw(gc, fastCat);
			else if(enemy.getClass() == AverageJoe.class)
				enemy.draw(gc, averageJoe);
			else
				enemy.draw(gc, bossImage);
		}
	}

	
	/////////////////////////// Private Methods ///////////////////////////////////
	
	/**
	 * Sets the waveDone variable
	 */
	private void checkWaveDone() {
		waveDone = enemies.isEmpty() && enemiesToSpawn <= 0;
	}
	
	/**
	 * Add an enemy to the game, giving it the map and starting loc.
	 */
	private void addEnemy() {
		if (enemiesToSpawn <= 0)
			return;
		if (gameTic % 5 == 0) {
			int randNum = rand.nextInt(99); //between 1 and 100
			if (randNum < CATCUTOFF)
				enemies.add(new FastCat(map,scaleSize));
			else if (randNum < JOECUTOFF)
				enemies.add(new AverageJoe(map,scaleSize));
			else
				enemies.add(new FatBoy(map,scaleSize));
			enemiesToSpawn--;
		}
	}
	
	/**
	 * Tells all the enemies to move
	 */
	private void moveEnemies() {
		for (Enemy baddie:enemies)
			baddie.makeMove();
	}
	
	/**
	 * rotates each tower to its next target
	 */
	private void rotateTowers() {
		//Collections.sort(enemies);
		for (Tower tower:towers) {
			for (Enemy baddie:enemies) {
				if (tower.inRange(baddie)) {
					tower.setAngle(baddie);
					break;
				}
			}
		}
	}
	
	/**
	 * Tells all the towers to shoot
	 */
	private void towersShoot() {
		for (Tower tower:towers) {
			for (Enemy baddie:enemies) {
				if (!tower.readyToShoot()) {
					//System.out.println("Cant Shoot");
					break;
				}
				if (tower.inRange(baddie)) {
					//System.out.println("firing");
					tower.resetCounter();
					tower.drawProjectile(gc, baddie.getRealX() + baddie.getScaleSize()/2, baddie.getRealY() + baddie.getScaleSize()/2);
					if(tower.getClass() == LaserTower.class && tower.getTowerLvl() == 1)
						tower.draw(gc, laserTower1);
					else if(tower.getClass() == LaserTower.class && tower.getTowerLvl() == 2)
						tower.draw(gc, laserTower2);
					else if(tower.getClass() == PoisonTower.class && tower.getTowerLvl() == 1)
						tower.draw(gc, poisonTower1);
					else if(tower.getClass() == PoisonTower.class && tower.getTowerLvl() == 2)
						tower.draw(gc, poisonTower2);
					else if(tower.getClass() == TeleporterTower.class && tower.getTowerLvl() == 1)
						tower.draw(gc, teleportTower1);
					else if(tower.getClass() == TeleporterTower.class && tower.getTowerLvl() == 2)
						tower.draw(gc, teleportTower2);
					tower.activateTower(baddie);
					break;
					//baddie = tower.activateTower(baddie);
					//System.out.println(baddie.getName() + " was shot current health: " + baddie.getHealth());
				}
				
			}
		}
	}
	
	/**
	 * Checks if any enemies have died
	 */
	private void enemiesDead() {
		ArrayList<Enemy> toRemove = new ArrayList<Enemy>();
		for (Enemy baddie:enemies) {
			if (baddie.isDead()) {
				//do something w animation
				player.addMoneys(baddie.getReward());
				System.out.println("Killed " + baddie.getName());
				toRemove.add(baddie);
				enemiesKilled++;
			}
		}
		for (Enemy baddie:toRemove) {
			if(baddie.getClass() == FatBoy.class)
				baddie.explode(gc, fatBoy, explodeImage);
			else if(baddie.getClass() == FastCat.class)
				baddie.explode(gc, fastCat, explodeImage);
			else if(baddie.getClass() == AverageJoe.class)
				baddie.explode(gc, averageJoe, explodeImage);
			enemies.remove(baddie);
		}
	}
	
	/**
	 * Checks to see if any enemies finished the map
	 */
	private void enemiesPassed() {
		ArrayList<Enemy> toRemove = new ArrayList<Enemy>();
		for (Enemy baddie:enemies) {
			if (baddie.passedEdge())
				toRemove.add(baddie);
		}
		for (Enemy baddie:toRemove) {
			enemies.remove(baddie);
			player.takeDamage(baddie.getDamage());
		}
	}

	/**
	 * Updates tic for all objects so they move based on birth tic
	 */
	private void objectTicUpdate() {
		//System.out.println("Updating object tics");
		for (Enemy baddie:enemies)
			baddie.updateCounter();
		for (Tower tower:towers)
			tower.updateCounter();
	}
	
	/**
	 * If on the boss wave, goes through all the towers and blows up the ones that the boss goes through
	 */
	private void bossDestroyTowers() {
		if (boss == null)
			return;
		ArrayList<Tower> toRemove = new ArrayList<Tower>();
		for (Tower tower:towers) {
			if (inContact(tower, boss))
				toRemove.add(tower);
		}
		for (Tower badTower:toRemove) {
			if(badTower.getClass() == LaserTower.class && badTower.getTowerLvl() == 1)
				badTower.explode(gc, laserTower1, explodeImage);
			else if(badTower.getClass() == LaserTower.class && badTower.getTowerLvl() == 2)
				badTower.explode(gc, laserTower2, explodeImage);
			else if(badTower.getClass() == PoisonTower.class && badTower.getTowerLvl() == 1)
				badTower.explode(gc, poisonTower1, explodeImage);
			else if(badTower.getClass() == PoisonTower.class && badTower.getTowerLvl() == 2)
				badTower.explode(gc, poisonTower2, explodeImage);
			else if(badTower.getClass() == TeleporterTower.class && badTower.getTowerLvl() == 1)
				badTower.explode(gc, teleportTower1, explodeImage);
			else if(badTower.getClass() == TeleporterTower.class && badTower.getTowerLvl() == 2)
				badTower.explode(gc, teleportTower2, explodeImage);
			towers.remove(badTower);
		}
		
	}
	
	/**
	 * Determines if the given boss and tower hit eachother
	 * @param tower - tower
	 * @param boss - boss
	 * @return true if boss hits tower
	 */
	private boolean inContact(Tower tower, BossMonster boss) {
		if (tower.getX() != boss.getX())
			return false;
		int towerY = tower.getY()*scaleSize;
		return (towerY == boss.getRealY() + scaleSize);
	}
	
}
