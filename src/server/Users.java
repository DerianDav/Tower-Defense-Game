package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps a list of all the Users
 * This class handles the list of Users 
 * includes any login checks and add/removal of users
 * 
 * @author Derian Davila Acuna
 *
 */
public class Users {	
	private File userFile;
	private List<User> userList;
	
	/**
	 * If their is a valid save file then the list of users will be loaded from that file.
	 * Otherwise this will create a new list of users and create a new savefile if their wasn't one.
	 */
	public Users() {
		userFile = new File("src/server/serverUsersSave.txt");
		try {
			if(!userFile.createNewFile())
				readInput();
			else {
				userList = new ArrayList<User>();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	/**
	 * checks whether or not the username matches with any user in the list then whether or not given password is correct
	 * @return the true if the user was in file and has the right password
	 */
	public boolean logIn(String username, String password) {
		for(User curUser : userList) {
			if(curUser.getUsername().equals(username)) {
				if(curUser.checkPassword(password))
					return true;
				else 
					return false;
			}
		}
		
		//if the username was not valid
		return false;
	}

	/**
	 * Adds a user to the userList with the given username and password
	 * @return returns true if a new user was added, returns false if their exist a user with the given username
	 */
	public boolean addUser(String username, String password) {
		for(User curUser : userList) {
			if(curUser.getUsername().equals(username)) {
				return false;
			}
		}
		
		userList.add(new User(username, password));
		writeToFile();
		return true;
	}

	/**
	 * Looks for a user with the username given by the given username
	 * @param username name of the user this looks for
	 * @return the user with the username or null if their is no user with the given username
	 */
	public User getUser(String username) {
		for(User curUser : userList) {
			if(curUser.getUsername().equals(username))
				return curUser;
		}
		return null;
	}
	
	public void markLost(String username) {
		for(User curUser : userList) {
			if(curUser.getUsername().equals(username))
				curUser.lost();
		}
		writeToFile();
	}
	
	public void markWon(String username) {
		for(User curUser : userList) {
			if(curUser.getUsername().equals(username))
				curUser.won();
		}
		writeToFile();
	}
	
	/**
	 * readInput reads from the Users save file. If the save file is blank then we catch the IO exception when we are reading. 
	 * If there is an IOException then we call createNewUserList 
	 */
	private void readInput() {
		try { 

			FileInputStream input = new FileInputStream(userFile);
			ObjectInputStream objInput = new ObjectInputStream(input);
			userList = (List<User>) objInput.readObject();
			objInput.close();
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			userList = new ArrayList<User>();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * writes the list of users into the Users.txt save file
	 */
	public void writeToFile() {
		try {
			FileOutputStream output = new FileOutputStream(userFile);
			ObjectOutputStream objOutput = new ObjectOutputStream(output);
			objOutput.writeObject(userList);
			objOutput.close();
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	
}
