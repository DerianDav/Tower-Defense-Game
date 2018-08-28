package view;

import java.awt.Point;


/**
 * 
 * Level 1 of the Tower Defense Game for CSC 335
 * 		extends Map class
 * 
 * @author Braxton
 * @version 1.0
 * @since 11-7-2017
 *
 */
@SuppressWarnings("serial")
public class LevelOne extends Map{
	
	/**
	 * Constructor, super of Map
	 * 
	 * @param strImage
	 */
	public LevelOne(int scaleSize) {
		super(scaleSize);
		//setImage("file:images/moon_surface.jpg");
	}

	/**
	 * 
	 * Method that will populate the grid with the correct Paths for the Enemies
	 * 
	 * @return None
	 */
	@Override
	public void createMap() {
		for(int i = 0; i < getMap().length; i++)
			for(int j = 0; j < getMap()[0].length; j++){
				if(i == getMap().length/2)
					getMap()[i][j] = Tiles.PATH;
				else
					getMap()[i][j] = Tiles.FIELD;
			}
		setStartingPointOne(new Point(getMap().length/2, 0));
		setStartingPointTwo(new Point(getMap().length/2, 0));
		
	}

	/**
	 * Returns the map level
	 */
	@Override
	public int getMapInt() {
		return 1;
		
	}

}
