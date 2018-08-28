package model;

import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import view.Map;

/**
 * Boss Enemy. Healthy, heavy, randomly spawns, etc..
 * @author Kyle
 *
 */
@SuppressWarnings("serial")
public  class BossMonster extends Enemy {
	
	private Random rand = new Random();
	

	/**
	 * Creates a BossMOnster Enemy
	 * @param map - map enemy is being put on
	 */
	public BossMonster(Map map, int scaleSize) {
		//Health, weight, armor
		super(10000, 20, 20, 40, 100,"Boss", map, scaleSize);
	}
	
	/**
	 * Draws the enemy. Appears as larger so has its own draw method
	 */
	@Override
	public void draw(GraphicsContext gc, Image image) {
		gc.drawImage(image, getRealX()-(getScaleSize()/2), getRealY()-(getScaleSize()/2), getScaleSize()*2, getScaleSize()*2);
		drawHealthBar(gc);
	}
	
	/**
	 * Called from teleporter to move enemy back to starting point.
	 */
	public void backToStartingPoint(){
		Random rand = new Random();
		setX(rand.nextInt(11)); //HARCDODED SIZE OF GRID
		setY(-1);
	}
	
	
	/**
	 * Boss Monster will always go down 
	 */
	public void makeMove() {
		if (ticsSinceBirth % weight != 0)
			return;
		setY(getY()+1);
		direction = Direction.DOWN;
	}
	
	/**
	 * Draws explosions on the boss when defeated
	 */
	public void fireworks(GraphicsContext gc, Image image) {
		double randX = rand.nextInt((int)getScaleSize()) - (getScaleSize()/2);
		double randY = rand.nextInt((int)getScaleSize()) - (getScaleSize()/2);
		gc.drawImage(image, getRealX()+randX, getRealY()+randY, getScaleSize(), getScaleSize());
		//draw(gc);
	}
	
	
}
