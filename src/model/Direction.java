package model;

import java.io.Serializable;

/**
 * Direction used for which way an enemy is facing, it knows which sprite to draw
 * @author Kyle
 *
 */
public enum Direction implements Serializable {
	LEFT, RIGHT, UP, DOWN, DOWN_RIGHT, DOWN_LEFT;
}
