package server;

import java.io.Serializable;

/**
 * User stores data of certain users. This includes their user name 
 *  and their passwords
 *  
 * @author Derian Davila Acuna
 *
 */
public class User implements Serializable{
	private String username;
	private String password;
	private int wins;
	private int loses;
	public boolean readyState;
	public boolean connected;
	public boolean gameOver;
	public boolean didWin;
	
	/**
	 * Used in the creation of new users.
	 */
	public User(String username, String password) {
		this.username = username; 
		this.password = password;	
		wins = 0;
		loses = 0;
		readyState = false;
		connected = false;
		gameOver = false;
	}
	
	/**
	 * increase wins by 1
	 */
	public void won() {
		wins++;
	}
	
	/**
	 * increase loses by 1
	 */
	public void lost() {
		loses++;
	}

	/**
	 * checks if the input string matches the password 
	 * @param password input string to check 
	 * @return returns true if the input and the user password matches else it returns false
	 */
	public boolean checkPassword(String password) {
		return password.equals(this.password);
	}
	
	/**
	 * returns the user's username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * returns how many times the player has won
	 */
	public int getWins() {
		return wins;
	}
	
	/**
	 * returns how many times the player has lost
	 */
	public int getLoses() {
		return loses;
	}
}
