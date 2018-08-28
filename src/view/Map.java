package view;

import java.awt.Point;
import java.io.Serializable;
import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Map Class
 * 		Creates the Maps used for the Game, sets the images and paths
 * 
 * 
 * @author Braxton Lazar
 * @version 1.0
 * @since 11-7-2017
 *
 */
@SuppressWarnings("serial")
public abstract class Map implements Serializable {
	private Tiles[][] grid;
	//private Image image;
	private Point startingPointOne;
	private Point startingPointTwo;
	public int scaleSize;
	private int gridSize;
	
	/**
	 * Constructor for the Map Class
	 * 		Creates the grid then populates it with the correct Tiles
	 * 
	 * @param scaleSize map tile to pixel ratio
	 * @param strImage
	 */
	Map(int scaleSize) {
		gridSize = 11;
		grid = new Tiles[gridSize][gridSize];
		createMap();
		this.scaleSize = scaleSize;
	}
	
	/**
	 * returns grid of tiles that makes up the map
	 * 
	 * @return grid - 2d grid of tiles
	 */
	public Tiles[][] getMap() {
		return grid;
	}
	
	/**
	 * Returns the map level
	 * @return int - map level
	 */
	public abstract int getMapInt();
	
	/**
	 * Returns the starting point for enemies to spawn at
	 * @return point - starting location
	 */
	public Point getStartingPoint(){
		Random rand = new Random();
		int  n = rand.nextInt(2) + 1;
		if(n == 1)
			return startingPointOne;
		else
			return startingPointTwo;
	}
	
	/**
	 * Setter for startingPointOne
	 * 
	 * @param p - starting loc for map
	 */
	public void setStartingPointOne(Point p){
		startingPointOne = p;
	}
	
	/**
	 * Setter for startingPointTwo
	 * 
	 * @param p - point
	 */
	public void setStartingPointTwo(Point p){
		startingPointTwo = p;
	}
	
	/**
	 * Gets the type of tile at coordinate (i, j)
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public Tiles get(int i, int j) {
		return grid[i][j];
	}
	
	/**
	 * Draws the map
	 * @param gc - canvas to draw on
	 * @param image - image that is the background
	 */
	public void draw(GraphicsContext gc, Image image){
		gc.drawImage(image, 0, 0, scaleSize*gridSize, scaleSize*gridSize);
			
		drawPath(gc);
	}
	
	/**
	 * Abstract method for creating each map
	 */
	public abstract void createMap();
	
	/**
	 * Draws the path enemies can go down
	 * @param gc - canvas to draw on
	 */
	public void drawPath(GraphicsContext gc){
		Color c = new Color(0, 0, 0, 0.25);
		gc.setFill(c);
		for(int i = 0; i < grid.length; i++)
			for(int j = 0; j < grid[0].length; j++)
				if(grid[i][j].equals(Tiles.PATH))
					gc.fillRect(scaleSize*i, scaleSize*j, scaleSize, scaleSize);
				else if(grid[i][j].equals(Tiles.UNBUILDABLE))
					gc.fillRect(scaleSize*i, scaleSize*j, scaleSize, scaleSize);
	}


}
