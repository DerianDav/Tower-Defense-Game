package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import model.Enemy;
import view.Map;


/**
 * Creates a server that forwards traffic between multiple clients
 * @author Derian Davila Acuna
 *
 */
public class Server {
	
	private static ServerSocket serverSocket;
	private static TreeMap<Integer,UserData> lobbyI = new TreeMap<Integer, UserData>();
	private static TreeMap<String,UserData> lobbyS = new TreeMap<String, UserData>();
	private static TreeMap<String, User> generalUsers = new TreeMap<String, User>();
	private static TreeMap<Integer,ObjectOutputStream> outputStreams = new TreeMap<Integer,ObjectOutputStream>();
	private static TreeMap<String, ObjectOutputStream> connectedStream = new TreeMap<String, ObjectOutputStream>();//if two users are in the same game they will both be added
																												   //to the tree with the username of one user and the output of the other
	private static Users users = new Users();
	
	public static void main(String[] args) throws IOException {
		serverSocket = new ServerSocket(4004);
		
		System.out.println("Server is running!");
		// Setup the server to accept many clients
		int lastAllocatedID = 0;
		while (true) {
			Socket socket = serverSocket.accept();
			ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream outputToClient = new ObjectOutputStream(socket.getOutputStream());

			lastAllocatedID++;
			
			outputStreams.put(lastAllocatedID,outputToClient);
			ClientHandler clientHandler = new ClientHandler(inputFromClient, outputToClient, lastAllocatedID);
			Thread thread = new Thread(clientHandler);
			thread.start();

		}
	}

	/**
	 * Listens for any client data being transmitted
	 * @author Derian Davila Acuna
	 *
	 */
	private static class ClientHandler implements Runnable {

		private ObjectInputStream input;
		private ObjectOutputStream output;
		private int userID;
		private User curUser;
		private String connectedTo = null;
		
		public ClientHandler(ObjectInputStream input, ObjectOutputStream output, int userID) {
			this.input = input;
			this.output = output;
			this.userID = userID;
		}

		@Override
		public void run(){
			try {
				while(true) {
					PacketHeader header = null;
					Boolean temp;
					String usernameTemp, password;
					header = (PacketHeader) input.readObject();
					
					switch(header) {

					case CONNECT:
						connectedTo = (String) input.readObject();
						outputStreams.get(lobbyS.get(connectedTo).getID()).writeObject(PacketHeader.CONNECT);
						outputStreams.get(lobbyS.get(connectedTo).getID()).writeObject(curUser.getUsername());
						connectedStream.put(lobbyS.get(connectedTo).getUserName(), output);
						connectedStream.put(curUser.getUsername(), outputStreams.get(lobbyS.get(connectedTo).getID()));
						curUser.readyState = false;
						lobbyI.remove(lobbyS.get(connectedTo).getID());
						lobbyS.remove(connectedTo);
						output.writeObject(PacketHeader.LOBBYREADY);
							output.writeBoolean(generalUsers.get(connectedTo).readyState);
							output.flush();
						break;
						
					case DISCONNECT:
						if(connectedStream.get(curUser.getUsername()) != null && connectedStream.get(connectedTo) != null) {
							connectedStream.get(curUser.getUsername()).writeObject(PacketHeader.DISCONNECT);
							connectedStream.get(curUser.getUsername()).flush();
							connectedStream.remove(curUser.getUsername());
							connectedStream.remove(connectedTo);
							connectedTo = "";
						}
						break;
												
					// these five cases will end up doing the same thing, they just forward information to the other user
					case MESSAGE:
					case TOWERLIST:
					case GAMESPEED:
					case MONEY:
					case HEALTH:
						System.out.println(header);
						if(connectedStream.get(curUser.getUsername()) != null) {
							connectedStream.get(curUser.getUsername()).writeObject(header);
							connectedStream.get(curUser.getUsername()).writeObject(input.readObject());	
							connectedStream.get(curUser.getUsername()).flush();
						}
						break;

					case ENEMYLIST:
						if(connectedStream.get(curUser.getUsername()) != null) {
							connectedStream.get(curUser.getUsername()).writeObject(header);
							connectedStream.get(curUser.getUsername()).writeObject(input.readObject());	
							connectedStream.get(curUser.getUsername()).writeInt(input.readInt());
							connectedStream.get(curUser.getUsername()).flush();
						}
						break;
					case ENEMYLISTSTART:
					case ENEMYLISTEND:
					case TOWERLISTSTART:
					case TOWERLISTEND:
						if(connectedStream.get(curUser.getUsername()) != null) {
							connectedStream.get(curUser.getUsername()).writeObject(header);
							connectedStream.get(curUser.getUsername()).flush();
						}
						break;

					case GAMEOVER:
						if(connectedStream.get(curUser.getUsername()) != null) {
							connectedStream.get(curUser.getUsername()).writeObject(header);
							connectedStream.get(curUser.getUsername()).writeObject(input.readObject());
							connectedStream.get(curUser.getUsername()).writeObject(input.readObject());
							connectedStream.get(curUser.getUsername()).writeObject(input.readObject());
							connectedStream.get(curUser.getUsername()).writeObject(input.readObject());
							connectedStream.get(curUser.getUsername()).flush();
						}
					case LOBBY:
						System.out.println(userID + " has connected to the lobby");
						List<UserData> userList = getLobbyList();
						output.writeObject(PacketHeader.LOBBY);
						output.writeObject(userList);
						output.flush();
						break;
						
					case CREATELOBBY:
						int map = input.readInt();
						if(!lobbyI.containsKey(userID)) {
							lobbyI.put(userID, new UserData(userID, curUser.getUsername(), map, curUser.getWins(), curUser.getLoses()));
							lobbyS.put(curUser.getUsername(), new UserData(userID, curUser.getUsername(), map, curUser.getWins(), curUser.getLoses()));
						}
						generalUsers.get(curUser.getUsername()).readyState = false;
						sendLobbyUpdate();
						break;
					
					case LOBBYREADY:
						generalUsers.get(curUser.getUsername()).readyState = input.readBoolean();
						System.out.println("" + generalUsers.get(curUser.getUsername()).readyState);
						if(connectedStream.containsKey(curUser.getUsername())) {
							connectedStream.get(curUser.getUsername()).writeObject(PacketHeader.LOBBYREADY);
							connectedStream.get(curUser.getUsername()).writeBoolean(generalUsers.get(curUser.getUsername()).readyState);
							connectedStream.get(curUser.getUsername()).flush();
						}
						break;

					case LOBBYDISCONNECT:
						if(connectedStream.containsKey(curUser.getUsername())) {
							connectedStream.get(curUser.getUsername()).writeObject(PacketHeader.LOBBYDISCONNECT);
							connectedStream.get(curUser.getUsername()).flush();
							connectedStream.remove(curUser.getUsername());
							connectedStream.remove(connectedTo);
							connectedTo = "";
						}
						if(lobbyS.containsKey(curUser.getUsername())) {
							lobbyI.remove(userID);
							lobbyS.remove(curUser.getUsername());
						}
						sendLobbyUpdate();
						break;
						
					case LOBBYSTART:
						connectedStream.get(curUser.getUsername()).writeObject(PacketHeader.LOBBYSTART);
						connectedStream.get(curUser.getUsername()).flush();
						break;
						
					case LOGIN:
						usernameTemp = (String) input.readObject();
						password = (String) input.readObject();
						
						temp = users.logIn(usernameTemp, password);
						if(temp) {
							curUser = users.getUser(usernameTemp);
							if(curUser == null)
									System.out.println("error");
							generalUsers.put(usernameTemp, curUser);
						}
						output.writeObject(PacketHeader.LOGIN);
						output.writeBoolean(temp);
						output.flush();
						break;	
				
					case NEWUSER:
						usernameTemp = (String) input.readObject();
						password = (String) input.readObject();
						temp = users.addUser(usernameTemp, password);
						if(temp) {
							curUser = users.getUser(usernameTemp);
							generalUsers.put(usernameTemp, curUser);
						}
						output.writeObject(PacketHeader.NEWUSER);
						output.writeBoolean(temp);
						output.flush();

					case GAMELOST:
						users.markLost(curUser.getUsername());
						System.out.println("player " + curUser.getUsername() + " has Lost");
						break;
						
					case GAMEWON:
						users.markWon(curUser.getUsername());
						break;
					}
						
				}
			}catch(Exception e) {
				lobbyI.remove(userID);
				lobbyS.remove(curUser.getUsername());
				try {
					if(connectedStream.get(curUser.getUsername()) != null) {
						connectedStream.get(curUser.getUsername()).writeObject(PacketHeader.DISCONNECT);
						connectedStream.get(curUser.getUsername()).flush();
					}
				} catch (IOException e1) {}
				connectedStream.remove(curUser.getUsername());
				connectedStream.remove(connectedTo);
				connectedTo = "";
				outputStreams.remove(userID);
				generalUsers.remove(curUser.getUsername());
				curUser.connected = false;
				sendLobbyUpdate();
			}
		}

		/**
		 * If the lobbyList updates then it is sent to everyone 
		 */
		private void sendLobbyUpdate() {
			List<ObjectOutputStream> outputList = new ArrayList<ObjectOutputStream>(outputStreams.values());
			System.out.println("sending lobby info");
			for(ObjectOutputStream output : outputList) {
				try {													
					output.writeObject(PacketHeader.LOBBY);
					output.writeObject(getLobbyList());
					output.flush();
				} catch (IOException e) { 
				}
				
			}
		}

		/**
		 * returns the list of the names of users that are in the lobby
		 * @return the list of the names 
		 */
		private List<UserData> getLobbyList() {
			return new ArrayList<UserData>(lobbyI.values());
		}
	}		
}