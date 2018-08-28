package server;

import java.io.Serializable;

/**
 * Data structure to store a single user's ID, username 
 * @author Derian Davila Acuna
 *
 */
public class UserData implements Serializable{
	private int userID;
	private String username;
	private int map;
	private int wins;
	private int loses;
	
	public UserData(int userID, String username, int map, int wins, int loses) {
		this.userID = userID; 
		this.username = username;
		this.map = map;
		this.wins = wins;
		this.loses = loses;
	}
	
	public int getID() {
		return userID;
	}
	
	public String getUserName() {
		return username;
	}
	
	public int getMap() {
		return map;
	}
	
	public int getWins() {
		return wins;
	}
	
	public int getLoses() {
		return loses;
	}
}