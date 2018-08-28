package view;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 
 * Level 3 of the Tower Defense Game for CSC 335
 * 		extends Map class
 * 
 * @author Braxton Lazar
 * @version 1.0
 * @since 11-7-2017
 *
 */

@SuppressWarnings("serial")
public class LevelThree extends Map{
	
	/**
	 * Constructor, super of Map
	 * 
	 * @param strImage
	 */
	public LevelThree(int scaleSize) {
		super(scaleSize);
		//setImage("file:images/Arctic_photo_CRREL.jpg");
	}

	/**
	 * 
	 * Method that will populate the grid with the correct Paths for the Enemies
	 * 
	 * @return
	 */
	@Override
	public void createMap() {
		for(int i = 0; i < getMap().length; i++)
			for(int j = 0; j < getMap()[0].length; j++){
				if(i == j || j == getMap().length - i - 1)
					getMap()[i][j] = Tiles.PATH;
				else if (i == j - 1 || j == getMap().length - i - 2 || j == getMap().length - i || i == j + 1)
					getMap()[i][j] = Tiles.UNBUILDABLE;
				else
					getMap()[i][j] = Tiles.FIELD;
			}
		setStartingPointOne(new Point(getMap().length - 1, 0));
		setStartingPointTwo(new Point(0, 0));
	}
	
	/**
	 * Draws the path that enemies can travel down
	 */
	@Override
	public void drawPath(GraphicsContext gc){
		Color c = new Color(0, 0, 0, 0.25);

		//gc.setLineWidth(70.0);
		gc.setLineWidth(scaleSize);
		gc.setStroke(c);
		gc.strokeLine(0, 0, getMap().length*this.scaleSize, getMap().length*this.scaleSize);
		gc.strokeLine(getMap().length*this.scaleSize, 0, 0, getMap().length*this.scaleSize);
		gc.setLineWidth(1);
		
	}

	/**
	 * Returns the map level
	 */
	@Override
	public int getMapInt() {
		return 3;
	}

}

