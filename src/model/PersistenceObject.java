/**
 * 
 */
package model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * @author Braxton
 *
 * Methods for Persistence with enemies, towers, and player
 */
public class PersistenceObject {
	
	/**
	 * Reads enemies from data/enemyObj file and returns ArrayList of all the enemies
	 */
	public static ArrayList<Enemy> readEnemiesFromFile() {
		ArrayList<Enemy> enemies = new ArrayList<Enemy>();
		try {
			FileInputStream inFile = new FileInputStream("data/enemyObj");
			ObjectInputStream inStream = new ObjectInputStream(inFile);
			enemies = (ArrayList<Enemy>) inStream.readObject();
			inStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: reading enemies");
		} catch (IOException e) {
			System.out.println("IOException error: reading enemies");
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException error: reading enemies");
		}
		System.out.println("Read enemies from file. Length: " + enemies.size());
		return enemies;
	}
	
	/**
	 * Given an arraylist of enemies, it will write it data/enemiesObj
	 * @param enemies - ArrayList of enemies to be written to a file
	 */
	public static void writeEnemiesToFile(ArrayList<Enemy> enemies) {
		try {
			// ObservableList<E> is not serializable, a pain again.
			FileOutputStream outFile = new FileOutputStream("data/enemyObj");
			ObjectOutputStream outStream = new ObjectOutputStream(outFile);
			outStream.writeObject(enemies);
			outStream.close();
			System.out.println("Wrote Enemies to file. Length: "+ enemies.size());
		} catch (FileNotFoundException e) {
			System.out.println("File not found: writing enemies");
		} catch (IOException io) {
			io.printStackTrace();
			System.out.println("IOException error: writing enemies");
		}
	}
	
	/**
	 * Reads towers from data/towerObj file and returns ArrayList of all the towers
	 */
	public static ArrayList<Tower> readTowersFromFile() {
		ArrayList<Tower> towers = new ArrayList<Tower>();
		try {
			FileInputStream inFile = new FileInputStream("data/towerObj");
			ObjectInputStream inStream = new ObjectInputStream(inFile);
			towers = (ArrayList<Tower>) inStream.readObject();
			inStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: reading towers");
		} catch (IOException e) {
			System.out.println("IOException error: reading towers");
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException error: reading towers");
		}
		System.out.println("Read towers from file. Length: " + towers.size());
		return towers;
	}
	
	/**
	 * Given an arraylist of towers, it will write it data/towerObj
	 * @param arrayList - ArrayList of towers to be written to a file
	 */
	public static void writeTowersToFile(ArrayList<Tower> towers) {
		try {
			// ObservableList<E> is not serializable, a pain again.
			FileOutputStream outFile = new FileOutputStream("data/towerObj");
			ObjectOutputStream outStream = new ObjectOutputStream(outFile);
			outStream.writeObject(towers);
			outStream.close();
			System.out.println("Wrote towers to file. Length: "+ towers.size());
		} catch (FileNotFoundException e) {
			System.out.println("File not found: writing towers");
		} catch (IOException io) {
			System.out.println("IOException error: writing towers");
		}	
	}
	
	/**
	 * Reads player from data/playerObj file and returns player
	 */
	public static Player readPlayerFromFile() {
		Player player = new Player();
		try {
			FileInputStream inFile = new FileInputStream("data/playerObj");
			ObjectInputStream inStream = new ObjectInputStream(inFile);
			player = (Player) inStream.readObject();
			inStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: reading player");
		} catch (IOException e) {
			System.out.println("IOException error: reading player");
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException error: reading player");
		}
		System.out.println("Read player from file.");
		return player;
	}
	
	/**
	 * Given a player object, it will write it data/playerObj
	 * @param player - player object to be written to a file
	 */
	public static void writePlayerFromFile(Player player) {
		try {
			// ObservableList<E> is not serializable, a pain again.
			FileOutputStream outFile = new FileOutputStream("data/playerObj");
			ObjectOutputStream outStream = new ObjectOutputStream(outFile);
			outStream.writeObject(player);
			outStream.close();
			System.out.println("Wrote player to file.");
		} catch (FileNotFoundException e) {
			System.out.println("File not found: writing player");
		} catch (IOException io) {
			System.out.println("IOException error: writing player");
		}	
	}
	
}
