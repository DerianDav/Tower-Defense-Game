package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import model.BossMonster;
import model.Enemy;
import model.Player;
import model.Tower;
import server.PacketHeader;
import server.UserData;

/**
 * Creates a connection between the user(client) and the the server
 * Has methods that are used to communicate to the server 
 * Also processes and message from the server
 * @author Derian Davila Acuna
 *
 */
public class Client{

	private static final String Address = "localhost";
	
	private Socket socket;
	private ObjectOutputStream outputToServer;
	private ObjectInputStream inputFromServer;
	private int loginSuccessStatus;
	private boolean isConnected;
	private ObservableList<UserData> lobbyList;
	private TableView<UserData> table;

	private ObservableList<String> connectedPlayers;
	private boolean connectedToUser;
	private String usernameConnectedTo;
	private String username;
	private boolean bothPlayersReady;
	private boolean readyState;
	
	private TowerDefenseMain mainInstance;
	
	private ListView<String> chatList;
	private ObservableList<String> inGameMessages;
	
	private ArrayList<Enemy> enemyList;
	private ArrayList<Tower> towerList;
	
	public Client(TowerDefenseMain mainInstance) throws Exception {
		isConnected = false;
		this.mainInstance = mainInstance;
		// Set up this app to connect to a server
		openConnection();
		username = "";
		loginSuccessStatus = 0;
		lobbyList = FXCollections.observableArrayList();
		connectedToUser = false;
	}

	/**
	 * establishes the connection between the client and the server
	 */
	private void openConnection() {
		// Our server is on our computer, but make sure to use the same port.
		try {
			socket = new Socket(Address, 4004);
			outputToServer = new ObjectOutputStream(socket.getOutputStream());
			inputFromServer = new ObjectInputStream(socket.getInputStream());

			// SeverListener will have a while(true) loop
			ServerListener listener = new ServerListener();

			// Note: Need setDaemon when started with a JavaFX App, or it crashes.
			Thread thread = new Thread(listener);
			thread.setDaemon(true);
			thread.start();

			isConnected = true;

		} catch (IOException e) {isConnected = false;
		}
	}

	/**
	 * changes the currently saved lobbyList with newLobby
	 */
	private void changeLobbyList(ArrayList<UserData> newLobby) {
		lobbyList = FXCollections.observableArrayList();
		lobbyList.addAll(newLobby);
		table.setItems(lobbyList);
	}
	
	/**listens for any data coming in from the server
	 */
	// Because JavaFX is not Thread-safe. This must be started in a new Thread
	private class ServerListener extends Task<Object> {

		@Override
		public void run() {
			try {
				while(true) {
					PacketHeader header = (PacketHeader) inputFromServer.readObject();

					switch(header) {
					case CONNECT:
						usernameConnectedTo = (String) inputFromServer.readObject();
						connectedToUser = true;
						connectedPlayers.add(usernameConnectedTo);
						break;
						
					case DISCONNECT:
						mainInstance.endGame();
						connectedToUser = false;
						break;
						
					case MESSAGE:
						Platform.runLater(new addMessage((String) inputFromServer.readObject()));
						break;
						
					case GAMESPEED:
						mainInstance.changeMiniMapSpeed((int) inputFromServer.readObject());
						break;
					
					case GAMEOVER:
						mainInstance.setPlayerTwoGameover((boolean) inputFromServer.readObject(), (Player) inputFromServer.readObject(),
								(int) inputFromServer.readObject(), (int) inputFromServer.readObject());
						
					case ENEMYLISTSTART:
						enemyList = new ArrayList<Enemy>();
						break;
						
					case ENEMYLIST:
						Enemy enemy = (Enemy) inputFromServer.readObject();
						if(enemy instanceof BossMonster) {
							if(enemy.getX() != 5)
								System.out.println("got boss monster with x = " + enemy.getX());
						}
						enemy.setX(inputFromServer.readInt());
						System.out.println("enemy setx = " + enemy.getX());
						enemyList.add(enemy);
						break;
						
					case ENEMYLISTEND:
						mainInstance.setNewEnemiesList(enemyList);
						break;
						
					case TOWERLISTSTART:
						towerList = new ArrayList<Tower>();
						break;
						
					case TOWERLIST:
						towerList.add((Tower) inputFromServer.readObject());
						break;
						
					case TOWERLISTEND:
						mainInstance.setNewTowersList(towerList);
						break;
						
					case LOBBY:
						changeLobbyList((ArrayList<UserData>) inputFromServer.readObject());
						System.out.println("recieved List");
						for(int i =0; i < lobbyList.size(); i++)
							System.out.println("username =" + lobbyList.get(i));
						break;
						
					case LOBBYREADY:
						readyState = inputFromServer.readBoolean();
						mainInstance.playerTwoReady(readyState, "X");
						System.out.println("change state to " + readyState);
						break;
					
					case LOBBYDISCONNECT:
						readyState = false;
						mainInstance.playerTwoReady(false, "Player Two has disconnected");
						break;
						
					case LOBBYSTART:
						mainInstance.startMultiplayer();
						break;
						
					case LOGIN:
						boolean confirmation = inputFromServer.readBoolean();
						System.out.println("confirmation recv: " + confirmation);
						if(confirmation)
							loginSuccessStatus = 1;
						else
							loginSuccessStatus = 2;
						break;

					case NEWUSER:
						if(inputFromServer.readBoolean()) {
							loginSuccessStatus = 1;
						}
						else 
							loginSuccessStatus = 2;
						System.out.println("loginSuccess = " + loginSuccessStatus);
						break;
					
					case MONEY:
						Platform.runLater(new addMoney((int) inputFromServer.readObject()));
						break;
					}

				}
			}
			catch(IOException io) {isConnected = false;}
			catch(ClassNotFoundException cnfe) {}
		}

		@Override
		protected Object call() throws Exception {
			// Not using this call, but we need to override it to compile
			return null;
		}
	}

	/**
	 * Sends a message to the server to check if the user can log in given the username and password
	 * @param username
	 * @param password
	 */
	public void login(String username, String password){
		loginSuccessStatus = 0;
		try {
			outputToServer.writeObject(PacketHeader.LOGIN);
			outputToServer.writeObject(username);
			outputToServer.writeObject(password);
			outputToServer.flush();
			this.username = username;
		}catch(IOException e) {isConnected = false;}
	}


	/**
	 * Sends a message to the server to check if the user can create an account given the username and password
	 * @param username 
	 * @param password
	 */
	public void newUserLogin(String username, String password){
		loginSuccessStatus = 0;
		try {
			outputToServer.writeObject(PacketHeader.NEWUSER);
			outputToServer.writeObject(username);
			outputToServer.writeObject(password);
			outputToServer.flush();
			this.username = username;
		}catch(IOException e) {isConnected = false;}
	}

	/**
	 * Gets the status of the user login attempt
	 * @return 0 = no response yet, 1 = login success, 2 = login failed
	 */
	public int getLoginConfirmation() {
		return loginSuccessStatus;
	}

	/**
	 * returns if the user is connected to the server or not
	 */
	public boolean getConnectionStatus() {
		return isConnected;
	}

	/**
	 * returns if the user is connected to the server or not
	 */
	public boolean getConnectionToUserStatus() {
		return connectedToUser;
	}
	/**
	 * Tries to open a connection to the server if the user is currently not connected
	 */
	public void reconnect() {
		System.out.println("reconnecting");
		if(!isConnected)
			openConnection();
	}

	/**
	 * Sends the packet header to the server
	 * Used for when you are joining the lobby and want the lobby list or creating a new lobby
	 * @param header - what you want to send to the server
	 */
	public void sendPacketHeader(PacketHeader header) {
		try {
			outputToServer.writeObject(header);
		} catch (IOException e) {
		}
	}

	/**
	 * sets up the table with the name of everyone in the lobby
	 * @param table - the table that displays the lobbylist
	 */
	public void setupLobbyList(TableView table) {
		this.table = table;
		this.table.setItems(lobbyList);
	}

	/**
	 * Tells the server that the user wants to join the lobby/game of username
	 * @param username username of a different user who we want to connect with
	 */
	public void connectToUser(String username) {
		try {
			outputToServer.writeObject(PacketHeader.CONNECT);
			outputToServer.writeObject(username);
			outputToServer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * sends a createlobby message to the server 
	 * @param mapNumb number of the map that was selected
	 * @param multiplayerLobbyScreen 
	 */
	public void createLobby(int mapNumb, ObservableList<String> connectedPlayers ) {
		try {
			System.out.println("sending info");
			outputToServer.writeObject(PacketHeader.CREATELOBBY);
			outputToServer.writeInt(mapNumb);
			outputToServer.flush();
			this.connectedPlayers = connectedPlayers;
			connectedPlayers.add(username);
			bothPlayersReady = false;
			readyState = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @return returns true if the user is connected to a lobby or returns false if the user is not connected
	 */
	public boolean getLobbyConnection() {
		return connectedToUser;
	}

	/**
	 * @return the username of the player
	 */
	public String getUserName() {
		return username;
	}

	/**
	 * updates the ready status of the user while in the wait screen to the other user user
	 * @param ready - the ready status of the user
	 */
	public void updateReadyStatus(boolean ready) {
		try {
			outputToServer.writeObject(PacketHeader.LOBBYREADY);
			outputToServer.writeBoolean(ready);
			outputToServer.flush();
			} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns whether the other user is ready
	 */
	public boolean opponetReady() {
		return readyState;
	}

	/**
	 * sends a lobbydisconnect to the server
	 */
	public void lobbyDisconnect() {
		try {
			outputToServer.writeObject(PacketHeader.LOBBYDISCONNECT);
			outputToServer.flush();
			} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * sends a lobbystart message to the server
	 */
	public void startGame() {
		try {
			outputToServer.writeObject(PacketHeader.LOBBYSTART);
			outputToServer.flush();
			} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * tells the server you have disconnected from the game
	 */
	public void gameDisconnect() {
		try {
			outputToServer.writeObject(PacketHeader.DISCONNECT);
			outputToServer.flush();
			} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * sets up the chat list in a multiplayer game
	 * @param chatList the table that is set up in TowerDefenseMain
	 */
	public void setupChat(ListView<String> chatList) {
		this.chatList = chatList;
		inGameMessages = FXCollections.observableArrayList();
		chatList.setItems(inGameMessages);
	}
	
	/**
	 * sends a message to the other user
	 * if the message is "send X" then it sends the other user X amount of moneys
	 * @param message the message you want to send the other user
	 */
	public void sendMessage(String message) {
		if(message.length() > 5)
			if(message.substring(0, 5).toLowerCase().contains("send")){
				int money = Integer.parseInt(message.substring(5));
				if(money <= mainInstance.player.getMoneys()){
					mainInstance.player.subtractCost(money);
					mainInstance.updateMoneyGUI();
					message = "sent " + money + " moneys!";
					try {
						outputToServer.writeObject(PacketHeader.MONEY);
						outputToServer.writeObject(money);
						outputToServer.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
				else
					message = "Could not send " + money + " moneys! Insuffiecent funds.";
		}
		message = username + ": " + message;
		inGameMessages.add(message);
		try {
			outputToServer.writeObject(PacketHeader.MESSAGE);
			outputToServer.writeObject(message);
			outputToServer.flush();
			} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * sends one of two gamestate messages to the server 
	 * @param header - the type of gamestate message
	 * @param message - the payload, can be a message or the current game speed
	 */
	public void sendGameState(PacketHeader header, Object message) {
		try {
			outputToServer.writeObject(header);
			outputToServer.writeObject(message);
			outputToServer.flush();
			} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * sends a list of tower to the other user
	 * needs to be sent one at a time
	 * sends towerliststart first to indicate a new list and towerlistend to indicate the end of the list
	 * @param list the list of tower
	 */
	public void sendTowers(ArrayList<Tower> list) {
		try {
			outputToServer.writeObject(PacketHeader.TOWERLISTSTART);
			for(Tower tower : list) {
				outputToServer.writeObject(PacketHeader.TOWERLIST);
				outputToServer.writeObject(tower);
				outputToServer.flush();
			}
			outputToServer.writeObject(PacketHeader.TOWERLISTEND);
			outputToServer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * sends a list of enemies to the other user
	 * needs to be sent one at a time
	 * sends enemyliststart first to indicat a new list and enemylistend to indicate the end of the list
	 * @param list the list of enemies
	 */
	public void sendEnemies(ArrayList<Enemy> list) {
		try {
			outputToServer.writeObject(PacketHeader.ENEMYLISTSTART);
			for(Enemy enemy : list) {
				if(enemy instanceof BossMonster)
					if(enemy.getX() != 5)
						System.out.println("boss sent x = " + enemy.getX());
				outputToServer.writeObject(PacketHeader.ENEMYLIST);
				outputToServer.writeObject(enemy);
				outputToServer.writeInt(enemy.getX());
				outputToServer.flush();
			}
			outputToServer.writeObject(PacketHeader.ENEMYLISTEND);
			outputToServer.flush();
		} catch (IOException e) {
		}
	}
	
	/*
	 * adds message to the user's message window
	 */
	private class addMessage implements Runnable{
		String message;
		public addMessage(String message) {
			this.message = message;
		}
		@Override
		public void run() {
			inGameMessages.add(message);
		}
	}
	
	/**
	 * adds money to the user
	 */
	private class addMoney implements Runnable {
		int money;
		public addMoney(int m){
			money = m;
		}
		public void run(){
			mainInstance.player.addMoneys(money);
			mainInstance.updateMoneyGUI();
		}
	}

	/**
	 * sends a gameover message to the other user
	 * @param playerWon - if you won then this is true otherwise this is false
	 * @param player - the user's player class
	 * @param enemiesKilled - how many enemies the player's killed
	 * @param towersBought - how many towers the player's bought
	 */
	public void sendGameOver(boolean playerWon, Player player, int enemiesKilled, int towersBought) {
		try {
			outputToServer.writeObject(PacketHeader.GAMEOVER);
			outputToServer.writeObject(playerWon);
			outputToServer.writeObject(player);
			outputToServer.writeObject(enemiesKilled);
			outputToServer.writeObject(towersBought);
			outputToServer.flush();
		} catch (IOException e) {
		}
	}
	
	/**
	 * @return the string name of the other user
	 */
	public String getConnectedToUser() {
		return usernameConnectedTo;
	}

	/**
	 * sends a GameWon or GameLost message to the server
	 * @param header - the type of message send to the server
	 */
	public void sendServerGameOver(PacketHeader header) {
		try {
			outputToServer.writeObject(header);
			} catch (IOException e) {
		}
	}
}