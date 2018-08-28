package server;

/**
 * The header for a message between the client and server
 * 
 * Client Server Message Protocol:
 * Type       	 |Message To Server (From Client)	  | Message to Client (From Server)
 * ==============|====================================|=================================
 * Connect	  	 |String(username)					  |String(username)
 * Disconnect 	 |N/A                                 |N/A
 * Message    	 |String(message)					  |String(message)
 * EnemyListStart|N/A								  |N/A
 * EnemyList  	 |Enemy								  |Enemy
 * EnemyListEnd	 |N/A								  |N/A
 * TowerListStart|N/A								  |N/A
 * TowerList  	 |Tower								  |Tower
 * TowerListEnd  |N/A								  |N/A
 * gameSpeed  	 |int(0,1,2)						  |int(0,1,2)
 * GameOver		 |N/A								  |N/A
 * Lobby      	 |N/A								  |List<UserData>(lobby)
 * CreateLobby	 |Integer(map)						  |N/A
 * LobbyReady 	 |boolean(true/false)				  |boolean(true/false)
 * LobbyStart 	 |N/A								  |N/A
 * LobbyDisconect|N/A  							      |N/A
 * Login      	 |String(username), String(password)  |boolean(as a response to the client)
 * NewUser    	 |String(username), String(password)  |boolean(as a response to the client)
 * Money	  	 |int(money)						  |int(money)
 * Health		 |int(health)						  |int(health)
 * GameWon		 |N/A								  |N/A
 * GameLost		 |N/A								  |N/A
 * 
 * @author Derian Davila Acuna
 *
 */
public enum PacketHeader {
	CONNECT, DISCONNECT, MESSAGE, ENEMYLISTSTART, ENEMYLIST,ENEMYLISTEND, TOWERLISTSTART, TOWERLIST, TOWERLISTEND, GAMESPEED, GAMEOVER,
	LOBBY, CREATELOBBY, LOBBYREADY, LOBBYDISCONNECT, LOBBYSTART, LOGIN, NEWUSER, MONEY, HEALTH, GAMEWON, GAMELOST
}
