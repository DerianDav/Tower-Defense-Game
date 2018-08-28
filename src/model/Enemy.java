package model;

import java.awt.Point;
import java.awt.Toolkit;
import java.io.Serializable;
import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import view.Map;
import view.Tiles;

/**
 * Abstract class for all enemies to extend from
 * Enemies have health, weight, armor, goldReward, and damage to player
 * @author Kyle
 *
 */
@SuppressWarnings("serial")
public abstract class Enemy implements Comparable<Enemy>, Serializable {
	protected int maxHealth;
	protected int health;
	protected int weight;
	private int armor;
	private int moneys;
	private int damage;
	private boolean poisoned;
	private Point startingPoint;
	private Map map;
	private String name;
	
	private Tiles[][] grid;
	private int gridSize;
	protected int ticsSinceBirth;
	
	//For Drawing Image	
	private int scaleSize;
	//private double scaleSize = 65;
	protected Direction direction;
	
	private int x;
	private int y;
	
	Random rand = new Random();
	
	
	/**
	 * Empty Constructor 
	 * @param health - health of enemy
	 * @param weight  - weight (how many units it moves every tic
	 * @param armor  - armor level
	 */
	public Enemy(){
		this.health = 100;
		this.weight = 5;
		this.armor = 5;
		this.moneys = 50;
		this.poisoned = false;
		this.damage = 10;
		grid = null;
		gridSize = 0;
	}
	
	/**
	 * Filled Constructor 
	 * @param health - health of enemy
	 * @param weight  - weight (how many units it moves every tic
	 * @param armor  - armor level
	 */
	public Enemy(int health, int weight, int armor, int moneys, int damage, String name, Map map, int scaleSize) {
		this.health = health;
		this.maxHealth = health;
		this.weight = weight;
		this.armor = armor;
		this.moneys = moneys;
		this.name = name;
		this.damage = damage;
		this.poisoned = false;
		this.scaleSize = scaleSize;
		grid = null;
		gridSize = 0;
		ticsSinceBirth = 0;
		
		this.map = map;
		getMapInfo();
	}
	
	/**
	 * Given the map from the constructor, this gets the grid and starting location
	 */
	private void getMapInfo() {
		grid = map.getMap();
		gridSize = grid.length;
		this.setLoc(map.getStartingPoint());
	}
	
	/**
	 * Sets the new location of the object
	 * @param x - new x coord
	 * @param y - new y coord
	 */
	private void setLoc(Point point) {
		this.x = (int) point.getX();
		this.y = (int) point.getY()-1;
		startingPoint = point;
	}
	
	/**
	 * Updates the tics since birth. Used to track movement
	 */
	public void updateCounter() {
		ticsSinceBirth = (ticsSinceBirth + 1) % 100;
		if (poisoned && ticsSinceBirth % 10 == 0)
			health -= 10;
	}
	
	/**
	 * Returns the current tic of the enemy
	 * @return ticsSinceBirth - current tic number
	 */
	public int getTic() {
		return ticsSinceBirth;
	}
	
	/**
	 * Gets the scaleSize for images
	 * @return scaleSize - in pixels
	 */
	public double getScaleSize() {
		return scaleSize;
	}
	
	/**
	 * Returns the direction the enemy is facing
	 * @return direction - where the enemy is facing
	 */
	public Direction getDirection() {
		return direction;
	}
	
	//LOCATION METHODS 
	
	/**
	 * Based on the tiles around the enemy, it calculates which way to face
	 * @return direction - direction of next spot 
	 */
	public Direction getNextDirection() {
		if (y >= gridSize)
			return Direction.DOWN;
		if (canGoDown())
			return Direction.DOWN;
		if (canGoRight())
			return Direction.RIGHT;
		if (canGoLeft())
			return Direction.LEFT;
		if (canGoDownRight() && canGoDownLeft()) {
			int num = rand.nextInt(2); // 0 or 1
			if (num == 0)
				return Direction.DOWN_LEFT;
			else
				return Direction.DOWN_RIGHT;
		}
		if (canGoDownRight())
			return Direction.DOWN_RIGHT;
		if (canGoDownLeft())
			return Direction.DOWN_LEFT;
		return Direction.DOWN;
	}
	
	/**
	 * Called from controller. Will decide where to move next. Controller should check after move
	 * to see if the enemy has gone off screen.
	 */
	public void makeMove() {
		if (ticsSinceBirth % weight != 0)
			return;
		if (canGoDown()) {
			y++;
		}
		else if (canGoRight()) {
			x++;
		}
		else if (canGoLeft()) {
			x--;
		}
		else if (canGoDownRight() && canGoDownLeft()) {
			if (direction == Direction.DOWN_LEFT) {
				x--;
				y++;
			}
			if (direction == Direction.DOWN_RIGHT) {
				x++;	
				y++;
			}
		}
		else if (canGoDownRight()) {
			x++;	
			y++;
		}
		else if (canGoDownLeft()) {
			x--;	
			y++;
		}
		direction = getNextDirection();
	}
	
	/**
	 * Determines if the enemy can go in this direction
	 * @return - true if can move that way
	 */
	private boolean canGoDown() {
		if (y == gridSize-1)
			return true;
		return grid[x][y+1] == Tiles.PATH;
	}
	
	/**
	 * Determines if the enemy can go in this direction
	 * @return - true if can move that way
	 */
	private boolean canGoRight() {
		if (direction == Direction.LEFT)
			return false;
		if (x == gridSize -1)
			return false;
		return grid[x+1][y] == Tiles.PATH;
	}
	
	/**
	 * Determines if the enemy can go in this direction
	 * @return - true if can move that way
	 */
	private boolean canGoLeft() {
		if (x == 0)
			return false;
		return grid[x-1][y] == Tiles.PATH;
	}
	
	/**
	 * Determines if the enemy can go in this direction
	 * @return - true if can move that way
	 */
	private boolean canGoDownRight() {
		if (x == gridSize -1)
			return false;
		int newX = x+1;
		int newY = y+1;
		return grid[newX][newY] == Tiles.PATH;
	}
	
	/**
	 * Determines if the enemy can go in this direction
	 * @return - true if can move that way
	 */
	private boolean canGoDownLeft() {
		if (x == 0)
			return false;
		int newX = x-1;
		int newY = y+1;
		return grid[newX][newY] == Tiles.PATH;
			
	}
	
	/**
	 * Determines if enemy has made it passed the end of the screen
	 */
	public boolean passedEdge() {
		return y >= gridSize;
	}
	
	/**
	 * Called when a teleport tower attacks enemy. Will spawn back to beginning
	 */
	public void backToStartingPoint(){
		this.x = startingPoint.x;
		this.y = startingPoint.y;
	}
	
	/**
	 * Changes the direction of enemy, given a direction.
	 * @param dir - direction to go to
	 */
	public void move(Direction dir) {
		if (dir == Direction.LEFT)
			x--;
		else if (dir == Direction.RIGHT)
			x++;
		else if (dir == Direction.UP)
			y--;
		else if (dir == Direction.DOWN)
			y++;
	}
	
	/**
	 * Get x location of enemy
	 * @return x - x coord
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * get y location of enemy
	 * @return y- y coord
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Calculates the x coordinate that the enemy should be drawn on the screen
	 */
	public double getRealX() {
		int baseX = (int) (x * scaleSize);
		int extraX = (int) ((getScaleSize() / getWeight()) * (getTic() % getWeight()));
		if (direction == Direction.DOWN)
			return baseX;
		if (direction == Direction.DOWN_RIGHT || direction == Direction.RIGHT)
			return baseX + extraX;
		else
			return baseX - extraX;
	}
	
	/**
	 * Calculates the y coordinate that the enemy should be drawn on the screen
	 */
	public double getRealY() {
		if (direction == Direction.DOWN || direction == Direction.DOWN_LEFT || direction == Direction.DOWN_RIGHT)
			return getY() * getScaleSize() + (getScaleSize() / getWeight()) * (getTic() % getWeight());
		return getY() * getScaleSize();
	}
	
	/**
	 * Set the x coord
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	/**
	 * set the y coord
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	//END LOCATIONS
	
	/**
	 * Get's the health of the enemy
	 * @return health - current hp
	 */
	public int getHealth() {
		return health;
	}
	
	/**
	 * takes away health from enemy
	 * @param dmg - amount of damage to be taken 
	 */
	public void takeDamage(int dmg) {
		int newDmg = dmg/armor;
		if (newDmg == 0)
			newDmg = 1;
		health -= newDmg;
	}
	
	/**
	 * heigher weight, moves slower
	 * @return weight - current weight
	 */
	public int getWeight() {
		return weight;
	}
	
	/**
	 * Increase weight. Negative not allowed
	 * @param amt - amt to change by
	 */
	public void addWeight(int amt) {
		weight = Math.abs(weight + amt);
	}
	
	/**
	 * Get reward amount from enemy
	 * @return
	 */
	public int getReward() {
		return moneys;
	}
	
	/**
	 * Poisons the enemy for damage to slowly be taken
	 */
	public void poison() {
		poisoned = true;
	}
	
	/**
	 * Find out if enemy is poisoned 
	 * @return poisoned - if poisoned or not
	 */
	public boolean isPoisoned() {
		return poisoned;
	}
	
	/**
	 * Gets armor of enemy
	 * @return armor - current armor 
	 */
	public int getArmor() {
		return armor;
	}
	
	/**
	 * Gets name of enemy
	 * @return name - name of ship
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the amt of damage the enemy does to the player if it passed the end of screen
	 * @return
	 */
	public int getDamage() {
		return damage;
	}
	
	/**
	 * Decrease or increase armor by amt given/ negative not allowed
	 * @param amt - armor += amt
	 */
	public void addArmor(int amt) {
		armor = Math.abs(armor + amt);
	}
	
	
	/**
	 * Checks if the character is dead
	 * @return boolean - true if dead
	 */
	public boolean isDead() {
		return health <= 0;
	}
	
	/**
	 * Draws the enemy ship
	 * @param gc - draw commands
	 */
	public void draw(GraphicsContext gc, Image image) {
		int shipWidth = 350;
		int picNumber;
		if (getDirection() == Direction.DOWN_LEFT)
			picNumber = 1;
		else if (getDirection() == Direction.DOWN_RIGHT)
			picNumber = 2;
		else if (getDirection() == Direction.LEFT)
			picNumber = 3;
		else if (getDirection() == Direction.RIGHT)
			picNumber = 4;
		else
			picNumber = 0;
		
		/* Code for rotating an image. Used in drawing tower
		Image orig = getImage();
		ImageView iv = new ImageView(orig);
		iv.setRotate(90);
		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		Image rotIm = iv.snapshot(params, null);
		*/
		
		
		gc.drawImage(image, shipWidth * picNumber, 0, shipWidth, shipWidth,
				getRealX(), getRealY(), getScaleSize(), getScaleSize());
		drawHealthBar(gc);

	}	
	/**
	 * Draws the health bar above enemy
	 * @param gc - place to draw
	 */
	protected void drawHealthBar(GraphicsContext gc) {
		//HEALTH BAR BELOW
		double buffer = .1 * getScaleSize();
		double barWidth = .8 * getScaleSize();
		
		double healthPercent = ((health*1.00/maxHealth) * barWidth); //percent of health scaled to 80% of the scale size
		gc.setFill(Color.GREEN);
		gc.fillRect(buffer+getRealX(), getRealY(), healthPercent, 7);
		gc.setFill(Color.RED);
		gc.fillRect(buffer+getRealX() + healthPercent, getRealY(), barWidth-healthPercent, 7);
	}
	
	/**
	 * Draws animation of tower blowing up.
	 */
	public void explode(GraphicsContext gc, Image charaterImage, Image explodeImage) {
		draw(gc, charaterImage);
		gc.drawImage(explodeImage, getRealX(), getRealY(), scaleSize, scaleSize);
	}
	
	/**
	 * compares enemies based on y coordinate.
	 */
	public int compareTo(Enemy other) {
		return (int) (other.getRealY() - this.getRealY());
	}
	
	public void changeScale(int scaleSize) {
		this.scaleSize = scaleSize;
	}
}
