package view;

import java.awt.Point;

/**
 * 
 * Level 2 of the Tower Defense Game for CSC 335
 * 		extends Map class
 * 
 * @author Braxton Lazar
 * @version 1.0
 * @since 11-7-2017
 *
 */
@SuppressWarnings("serial")
public class LevelTwo extends Map{
	
	/**
	 * Constructor, super of Map
	 * 
	 * @param strImage
	 */
	public LevelTwo(int scaleSize) {
		super(scaleSize);
		//setImage("file:images/red_planet_surface.gif");
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
				if(((j == 0 || j == 4 || j == 5 || j == 8 || j == 9) && i == 1) || 
						((j == 2 || j  == 6) && i == 9) || 
						(j == 1 && (i > 0 && i < 10)) || 
						(j == 3 && (i > 0 && i < 10)) || 
						(j == 5 && (i > 0 && i < 10)) ||
						(j == 7 && (i > 0 && i < 10)) || 
						(j == 9 && (i > 0 && i < 10)) ||
						(j == 10 && i == 9))
					getMap()[i][j] = Tiles.PATH;
				else
					getMap()[i][j] = Tiles.FIELD;
			}
		setStartingPointOne(new Point(1, 0));
		setStartingPointTwo(new Point(1, 0));
	}

	/**
	 * Returns the map level
	 */
	@Override
	public int getMapInt() {
		return 2;
	}

}
