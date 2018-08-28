package controller;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.BossMonster;
import model.Enemy;
import model.LaserTower;
import model.Player;
import model.PoisonTower;
import model.TeleporterTower;
import model.Tower;
import model.PersistenceObject;
import server.PacketHeader;
import server.UserData;
import view.LevelOne;
import view.LevelThree;
import view.LevelTwo;
import view.Map;

/**
 * 
 * @author Braxton Lazar
 * @version 1.0
 * @since 11-7-2017
 *
 */

public class TowerDefenseMain extends Application {
	
	private Stage primaryStage;
	private Map map;
	private GameBoy controller;
	public Player player;
	private GraphicsContext gc;
	private Timeline timeline;
	private int lastTic;
	
	private StackPane window;
	private StackPane upgradeScreen;
	private StackPane enemyScreen;
	private StackPane escScreen;
	private StackPane gameOverScreen;
	private Button start;
	private Label moneyLabel;
	private Label waveLabel;
	private Label healthLabel;
	private Label playerTwoReady;
	private Line healthBar;
	private Button speedButton;
	private GridPane topPane;
	
	private final String rules = "1. Letting an enemy pass will cause damage to your health.\n" + 
			   "2. When your health goes to 0, you will lose the game.\n" + 
			   "3. On the 7th wave, a boss will spawn. Defeat it to win.\n" + 
			   "4. The boss will not follow the map path and will destroy towers it runs into.\n" +
			   "5. If teleported, the location will be random at the top\n\n" +
			   "Multiplayer\n" + 
			   "5. You and your partner share health and send money to eachother\n" +
			   "6. Enemies will spawn independently of each other.\n";
	
	private boolean multiplayer;
	private static int multiplayerSideShift;
	private boolean paused;
	private boolean escPaused;
	private Point2D mouseLoc;
	Shop shop;
	
	private Client client;
	private boolean loggedin;
	String passwordText;
	int oldPasswordSize;
	private boolean gameStart;
	private boolean loadedGame = false;
	
	private Player miniMapPlayer;
	private GameBoy miniMapController;
	private Timeline miniMapTimeline;
	
	private boolean playerTwoGameover;
	private boolean playerTwoWon;
	private String secondUsername;
	private int playerTwoEnemyKills;
	private int playerTwoTowersBought;
	
	//Images
	private Image laserTower1 = new Image("file:images/laserTower1.png");
	private Image laserTower2 = new Image("file:images/laserTower2.png");
	private Image poisonTower1 = new Image("file:images/poisonTower1.png");
	private Image poisonTower2 = new Image("file:images/poisonTower2.png");
	private Image teleportTower1 = new Image("file:images/teleportTower1.png");
	private Image teleportTower2 = new Image("file:images/teleportTower2.png");
	private Image map1Image = new Image("file:images/moon_surface.jpg");
	private Image map2Image = new Image("file:images/red_planet_surface.gif");
	private Image map3Image = new Image("file:images/Arctic_photo_CRREL.jpg");

	private final static int maxHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private final static double heightScale = ((double)maxHeight)/1080;
	private final static int scaleSize = (int) (90*heightScale); // 93 for a max height of 1080
	
	/**
	 * Main Class for the Tower Defense Game
	 * 
	 * @param args
	 */
	
	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * starts the GUI of the Tower Defense Game
	 * 
	 * @param primaryStage
	 */

	@Override
	public void start(Stage primaryStage) throws Exception {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.primaryStage = primaryStage;
		try {
			client  = new Client(this);
			loggedin = false;
		} catch (Exception e) {}
		
		primaryStage.setScene(new Scene(new mainMenuGUI(),(scaleSize*11)+150, (scaleSize*11) + 25));
		primaryStage.show();
		

		//skips menu
		map = new LevelTwo(scaleSize);
	//	startSinglePlayerGUI();
		//startMultiplayerGUI();
		//shows gameover screen
		//gameOverScreen = new gameOverScreen();
	//	gameOverScreen.setMaxSize(400, 400);
	//	window.getChildren().add(gameOverScreen);
	}
	
	/**
	 * Creates the main menu Gui
	 * @author Derian Davila Acuna
	 */
	private class mainMenuGUI extends StackPane{
		private int mapNumb = 1;
		private static final int previewScaleSize = 300/11;
		
		private GraphicsContext gc;
		
		//need fake buttons since the normal javafx buttons have a minimum size of ~50 pixels in length
		private Rectangle outerRect;
		private Rectangle fakeNewg;
		private Rectangle fakeLoadg;
		private Rectangle fakeMultig;
		private Rectangle fakeQuitg;
		private Rectangle fakeRulesg;
		private Button newg;
		private Button loadg;
		private Button multig;
		private Button quitg;
		private Button rulesg;
		
		private boolean extended;
		private Pane selectedPane;
		Color textColor = Color.GOLDENROD;
		
		double marginTop = 300*heightScale; //For buttons 
		
		
		public mainMenuGUI() {
			Canvas background = new Canvas((scaleSize*11)+150, (scaleSize*11) +25);
			gc = background.getGraphicsContext2D();
			extended = false;
			
			setAlignment(background, Pos.CENTER);
			getChildren().add(background);
			
			drawBackground(0);
			addButtons(0,0);
			
		}
		
		/**
		 * expands the menu screen
		 */
		private void expandMenu() {
			if(extended) {
				return;
			}
			
			Timeline menuTimeline = new Timeline(new KeyFrame(Duration.millis(25), new menuAnimation(true)));
			menuTimeline.setCycleCount(90);
			menuTimeline.play();
			extended = true;
		}
		
		/**
		 * shrinks back the menu to its original spot
		 */
		private void shrinkMenu() {
			if(!extended)
				return;
			Timeline menuTimeline = new Timeline(new KeyFrame(Duration.millis(25), new menuAnimation(false)));
			menuTimeline.setCycleCount(90);
			menuTimeline.play();
			getChildren().remove(selectedPane);
			extended = false;
		}
		
		/**
		 * adds the menu buttons 
		 * @param shiftLeft - pixels the menu box is shifted to the left
		 * @param buttonShrink shrinks to the button from 200x50 to 200*(1-buttonShrink/90)x50
		 */
		private void addButtons(int shiftLeft, int buttonShrink) {
			getChildren().remove(newg);
			getChildren().remove(loadg);
			getChildren().remove(multig);
			getChildren().remove(quitg);
			getChildren().remove(rulesg);
			
			newg = new Button("New Game");
			loadg = new Button("Load Game");
			multig = new Button("Multiplayer");
			quitg = new Button("Quit Game");
			rulesg = new Button("Rules");
			
			newg.setOnAction(event -> {
				expandMenu();
				selectedPane = new newGameScreen(450, 500);
			});

			loadg.setOnAction(event -> {
				player = PersistenceObject.readPlayerFromFile();
				controller.setEnemies(PersistenceObject.readEnemiesFromFile());
				controller.setTowers(PersistenceObject.readTowersFromFile());
				mapNumb = player.getMapNumber();
				if(mapNumb == 1)
					map = new LevelOne(scaleSize);
				else if(mapNumb == 2)
					map = new LevelTwo(scaleSize);
				else	
					map = new LevelThree(scaleSize);
				startSinglePlayerGUI();
			});
			
			multig.setOnAction(event ->{
				expandMenu();
				selectedPane = new multiplayerLobbyScreen(450,500);
			});
			quitg.setOnAction(event -> {primaryStage.close();});
			loadg.setOnAction(event -> {
				loadedGame = true;
				startSinglePlayerGUI();
			});
			
			//rulesg.setOnAction(event -> {changeSelectedPane(new singlePlayerRules());});
			rulesg.setOnAction(event ->{
				expandMenu();
				selectedPane = new singlePlayerRules();
			});
			
			double ratio = 1 -((double)buttonShrink/90);
			
			//if it is to small to read any text just draw grey boxes instead
			getChildren().remove(fakeNewg);
			getChildren().remove(fakeLoadg);
			getChildren().remove(fakeMultig);
			getChildren().remove(fakeQuitg);
			getChildren().remove(fakeRulesg);
			if(buttonShrink > 70) {
				Color defaultColor = new Color(0.894,0.894,0.894,1);
				fakeNewg = new Rectangle((200*ratio), 50);
				fakeLoadg = new Rectangle((200*ratio), 50);
				fakeMultig = new Rectangle((200*ratio), 50);
				fakeQuitg = new Rectangle((200*ratio), 50);
				fakeRulesg = new Rectangle((200*ratio), 50);
				
				fakeNewg.setFill(defaultColor);
				fakeLoadg.setFill(defaultColor);
				fakeMultig.setFill(defaultColor);
				fakeQuitg.setFill(defaultColor);
				fakeRulesg.setFill(defaultColor);
				
				setAlignment(fakeNewg, Pos.TOP_CENTER);
				setMargin(fakeNewg, new Insets(marginTop,shiftLeft,0,0));
				setAlignment(fakeLoadg, Pos.TOP_CENTER);
				setMargin(fakeLoadg, new Insets(marginTop +75,shiftLeft,0, 0));
				setAlignment(fakeMultig, Pos.TOP_CENTER);
				setMargin(fakeMultig, new Insets(marginTop+150,shiftLeft,0,0));
				setAlignment(fakeQuitg, Pos.TOP_CENTER);
				setMargin(fakeQuitg, new Insets(marginTop+225,shiftLeft, 0,0));
				setAlignment(fakeRulesg, Pos.TOP_CENTER);
				setMargin(fakeRulesg, new Insets(marginTop+300,shiftLeft, 0,0));
				
				getChildren().add(fakeNewg);
				getChildren().add(fakeLoadg);
				getChildren().add(fakeMultig);
				getChildren().add(fakeQuitg);
				getChildren().add(fakeRulesg);
				
				return;
			}
			newg.setMaxSize((200*ratio), 50);
			newg.setFont(new Font("Ariel", 20));
			loadg.setPrefSize((200*ratio), 50);
			loadg.setFont(new Font("Ariel", 20));
			multig.setPrefSize((200*ratio), 50);
			multig.setFont(new Font("Ariel", 20));
			quitg.setPrefSize(200*ratio, 50);
			quitg.setFont(new Font("Ariel", 20));
			rulesg.setPrefSize(200*ratio, 50);
			rulesg.setFont(new Font("Ariel", 20));

			quitg.setOnAction(event -> {
				player = PersistenceObject.readPlayerFromFile();
				controller.setEnemies(PersistenceObject.readEnemiesFromFile());
				controller.setTowers(PersistenceObject.readTowersFromFile());
				mapNumb = player.getMapNumber();
				if(mapNumb == 1)
					map = new LevelOne(scaleSize);
				else if(mapNumb == 2)
					map = new LevelTwo(scaleSize);
				else	
					map = new LevelThree(scaleSize);
				startSinglePlayerGUI();
			});
			
			setAlignment(newg, Pos.TOP_CENTER);
			setAlignment(loadg, Pos.TOP_CENTER);
			setAlignment(multig, Pos.TOP_CENTER);
			setAlignment(quitg, Pos.TOP_CENTER);
			setAlignment(rulesg, Pos.TOP_CENTER);
			
			setMargin(newg, new Insets(marginTop,shiftLeft,0,0));
			setMargin(loadg, new Insets(marginTop + 75,shiftLeft,0,0));
			setMargin(multig, new Insets(marginTop + 150,shiftLeft,0,0));
			setMargin(quitg, new Insets(marginTop + 225,shiftLeft,0,0));
			setMargin(rulesg, new Insets(marginTop + 300,shiftLeft,0,0));
			
			getChildren().add(newg);
			getChildren().add(loadg);
			getChildren().add(multig);
			getChildren().add(quitg);
			getChildren().add(rulesg);
			
		}
		
		/**
		 * draws in the overall background to the menu and the box surrounding the menu options
		 * @param shiftLeft
		 */
		private void drawBackground(int shiftLeft) {
			getChildren().remove(outerRect);
			gc.drawImage(new Image("file:images/12.jpg"), 0, 0, (scaleSize*11)+150, (scaleSize*11) +25);
			Color bg = new Color(0.2,0.2,0.2,0.5);
			outerRect = new Rectangle(300+shiftLeft, 500);
			outerRect.setArcHeight(25);
			outerRect.setArcWidth(25);
			outerRect.setFill(bg);
			
			setAlignment(outerRect, Pos.CENTER);
			setMargin(outerRect, new Insets(0, 0, 0, shiftLeft/10));
			getChildren().add(outerRect);
		}
		
		/**
		 * changes the selectedPane to the given pane
		 * @param newSelection - the pane we want to change selectedPane to
		 */
		private void changeSelectedPane(Pane newSelection) {
			getChildren().remove(selectedPane);	
			selectedPane = newSelection;
			newSelection.setMaxSize(700, 500);
			setAlignment(newSelection, Pos.CENTER);
			setMargin(newSelection, new Insets(50,0,0,(450)/10));
			getChildren().add(newSelection);
	
		}
		
		/**
		 *	Animates the menu to expand or shrink  
		 *
		 */
		private class menuAnimation implements EventHandler<ActionEvent>{
			int tic = 0;
			int increaseRate = 5;
			int previousExpansionSize = 0;
			int ratio = 0;
			boolean grow;
			
			public menuAnimation(boolean grow) {
				this.grow = grow;
				if(!grow) {
					increaseRate *= -1;
					previousExpansionSize = 450;
				}
			}
			@Override
			public void handle(ActionEvent arg0) {
				tic++;
				if(grow)
					ratio = tic;
				else
					ratio = 90-tic;
				drawBackground((tic*increaseRate) + previousExpansionSize);
				addButtons((tic*increaseRate) + previousExpansionSize, ratio);
				
				if(tic == 90 && grow) {
					selectedPane.setMaxSize(700, 500);
					setAlignment(selectedPane, Pos.CENTER);
					setMargin(selectedPane, new Insets(50,0,0,(tic*increaseRate)/10));
					getChildren().add(selectedPane);
					
					getChildren().removeAll(newg,loadg, multig, quitg);
				}
			}
			
		}
		
		/**
		 *	Draws the options when you select new game on the main menu
		 */
		public class newGameScreen extends BorderPane {
			public newGameScreen(int width, int height) {
				GridPane gPane = new GridPane();
				
				Label newGameSettings = new Label("Map Selection");
				newGameSettings.setPrefSize(300, 100);
				newGameSettings.setFont(new Font("Ariel", 30));
				newGameSettings.setTextFill(textColor);
				
				Canvas mapPreview = new Canvas(300,300);
				GraphicsContext mapPreviewGC = mapPreview.getGraphicsContext2D();
				Button start = new Button("Start");
				Button back = new Button("Back");
				
				RadioButton map1 = new RadioButton("Map 1");
				RadioButton map2 = new RadioButton("Map 2");
				RadioButton map3 = new RadioButton("Map 3");

				map1.setPrefSize(100, 100);
				map2.setPrefSize(100, 100);
				map3.setPrefSize(100, 100);
				start.setPrefSize(150, 30);
				back.setPrefSize(100,30);
				
				map1.setFont(new Font("Ariel", 20));
				map2.setFont(new Font("Ariel", 20));
				map3.setFont(new Font("Ariel", 20));
				start.setFont(new Font("Ariel", 20));
				back.setFont(new Font("Ariel", 20));

				map1.setTextFill(textColor);
				map2.setTextFill(textColor);
				map3.setTextFill(textColor);
				
				ToggleGroup mapGroup = new ToggleGroup();
				map1.setToggleGroup(mapGroup);
				map2.setToggleGroup(mapGroup);
				map3.setToggleGroup(mapGroup);
			
				map1.setSelected(true);
				
				
				map = new LevelOne(previewScaleSize);
				map.draw(mapPreviewGC, map1Image);
			
				//radial button event handlers
				map1.setOnAction(event -> {map = new LevelOne(previewScaleSize);map.draw(mapPreviewGC, map1Image);mapNumb = 1;});
				map2.setOnAction(event -> {map = new LevelTwo(previewScaleSize);map.draw(mapPreviewGC, map2Image);mapNumb = 2;});
				map3.setOnAction(event -> {map = new LevelThree(previewScaleSize);map.draw(mapPreviewGC, map3Image);mapNumb = 3;});
			
				gPane.add(newGameSettings,0,0);
				gPane.add(map1, 0, 1);
				gPane.add(map2, 0, 2);
				gPane.add(map3, 0, 3);
				
				setCenter(gPane);
				setMargin(gPane, new Insets(0,0,0,30));
				setRight(mapPreview);
				setMargin(mapPreview, new Insets(100,0,0,00));
				
				GridPane buttonPane = new GridPane();
				buttonPane.add(back, 0, 0);
				buttonPane.add(start, 2, 0);
				buttonPane.setHgap(50);
				setBottom(buttonPane);
				setMargin(buttonPane, new Insets(0,0,60,0));
				
				//button event handlers
				back.setOnAction(event -> {shrinkMenu();});
				start.setOnAction(event -> {
					if(mapNumb == 1)
						map = new LevelOne(scaleSize);
					else if(mapNumb == 2)
						map = new LevelTwo(scaleSize);
					else	
						map = new LevelThree(scaleSize);
					startSinglePlayerGUI();
				});
			}
		}//end of newGameScreen
		
		private class singlePlayerRules extends BorderPane{
			GridPane gpane;
			Button back;
			public singlePlayerRules() {
				gpane = new GridPane();
				gpane.setVgap(30);
				back = new Button("back");
				Label rulesTitle = new Label("Rules");
				Label rulesText = new Label(rules);
			
				rulesTitle.setFont(new Font("Ariel", 30));
				rulesText.setFont(new Font("Ariel", 20));
				
				rulesTitle.setTextFill(textColor);
				rulesText.setTextFill(textColor);
				
				gpane.setPrefSize(450, 400);
				
				gpane.add(rulesTitle,0,0);
				gpane.add(rulesText,0,1);
				gpane.add(back, 0, 2);
				back.setPrefWidth(50);
				setAlignment(gpane, Pos.TOP_CENTER);
				
				
				setTop(gpane);
				
				back.setOnAction(event -> {shrinkMenu();});
			}
		}


		/**
		 *	Draws the options when you select multiplayer on the main menu
		 */
		public class multiplayerLobbyScreen extends BorderPane {
			private int width;
			private int height;

			private Font defaultFont = new Font("Ariel", 30);
						
			public multiplayerLobbyScreen(int width, int height) {
				this.width = width;
				this.height = height;
				multiplayerStartup();
			}
			
			private void multiplayerStartup() {
		
				GridPane gPane = new GridPane();
				GridPane buttonPane = new GridPane();
				
				Label multiplayerTitle = new Label("Multiplayer");
				multiplayerTitle.setPrefSize(300, 100);
				multiplayerTitle.setFont(new Font("Ariel", 30));
				multiplayerTitle.setTextFill(textColor);
				//initially tries to connect if the user is not already connected
				if(!client.getConnectionStatus())
					client.reconnect();
				
				if(client.getConnectionStatus() && loggedin)
					multiplayerLobby();
				else {
				if(client.getConnectionStatus() && !loggedin) {
					Label loginLabel = new Label("Enter your Username and Password");
					Label usernameLabel = new Label("Username: ");
					Label passwordLabel = new Label("Password: ");
					TextField usernameField = new TextField();
					TextField passwordField = new TextField();
					oldPasswordSize = 0;
					passwordText = "";
					
					loginLabel.setFont(defaultFont);
					usernameLabel.setFont(defaultFont);
					passwordLabel.setFont(defaultFont);
					loginLabel.setTextFill(textColor);
					usernameLabel.setTextFill(textColor);
					passwordLabel.setTextFill(textColor);
					
					loginLabel.setMaxSize(600, 50);
					usernameField.setMaxSize(400, 50);
					usernameField.setFont(defaultFont);
					passwordField.setMaxSize(400, 50);
					passwordField.setFont(defaultFont);
					
					gPane.add(loginLabel, 1, 0);
					gPane.add(usernameLabel, 0, 1);
					gPane.add(usernameField, 1, 1);
					gPane.add(passwordLabel, 0, 2);
					gPane.add(passwordField, 1, 2);
				
					gPane.setVgap(30);
					
					passwordField.setOnKeyPressed(event -> {
						if(event.getCode() == KeyCode.BACK_SPACE && oldPasswordSize != 0) {
							passwordText = passwordText.substring(0, passwordField.getText().length()-1);
							oldPasswordSize--;
						}
						else if (event.getCode() != KeyCode.BACK_SPACE && event.getCode() != KeyCode.ENTER){
							if(event.getCode().toString().length() > 1 )
								return;
							char key = event.getCode().toString().charAt(0);
							if(key >= 32 && key < 127) {
								passwordText += key;
								oldPasswordSize++;
							}
						}	
						String insertedString = "";
						for(int i = 0; i < oldPasswordSize; i++)
							insertedString += "*";
						passwordField.setText(insertedString);
					});
					
					Button login = new Button ("Login");
					Button newUser = new Button("New User");
					
					login.setPrefSize(150, 30);
					login.setFont(new Font("Ariel", 20));
					newUser.setPrefSize(150, 30);
					newUser.setFont(new Font("Ariel", 20));
					
					buttonPane.add(login, 1, 0);
					buttonPane.add(newUser, 2, 0);		
					
					login.setOnAction(event -> {
						if(client.getConnectionStatus()) {
								client.login(usernameField.getText(), passwordText);
								//runs till the server returns a response
								while(client.getLoginConfirmation() == 0) {
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {}
								}
								if(client.getLoginConfirmation() == 1) 
									multiplayerLobby();
								else {
									loginLabel.setText("Reenter your Username and Password");
									passwordField.setText("");
									passwordText = "";
									oldPasswordSize = 0;
								}
						}
					});

					newUser.setOnAction(event -> {
						if(client.getConnectionStatus()) {
								client.newUserLogin(usernameField.getText(), passwordText);
								//runs till the server returns a response
								while(client.getLoginConfirmation() == 0) {System.out.println("getloginconfirmation = 0");
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {}
								}
								if(client.getLoginConfirmation() == 1) 
									multiplayerLobby();
								else {
									loginLabel.setText("Reenter your Username and Password");
									passwordField.setText("");
									passwordText = "";
									oldPasswordSize = 0;
								}
						}
					});
					
					setCenter(gPane);
					}
				else {
					Label couldNotConnect = new Label("Could not connect to server");
					couldNotConnect.setFont(defaultFont);
					couldNotConnect.setTextFill(textColor);
					gPane.add(couldNotConnect, 0, 0);
					setCenter(gPane);
				}
				
				
				Button back = new Button("Back");
			
				back.setPrefSize(100,30);	
				back.setFont(new Font("Ariel", 20));
				
				setTop(multiplayerTitle);
				setAlignment(multiplayerTitle, Pos.TOP_CENTER);
				
				buttonPane.add(back, 0, 0);
				buttonPane.setHgap(50);
				setBottom(buttonPane);
				setMargin(buttonPane, new Insets(0,0,60,0));
				
				//button event handlers
				back.setOnAction(event -> {shrinkMenu();});
				}
			}//end of multiplayerStartup
			
			/**
			 * displays the list of people in game lobby
			 */
			public void multiplayerLobby() {
				setCenter(null);
				setBottom(null);
				setTop(null);
				
				loggedin = true;
				GridPane buttonPane = new GridPane();
				Font buttonFont = new Font("Ariel", 20);
				
				Button back = new Button("Back");
				Button newLobby = new Button("New Lobby");
				Button join = new Button("Join");
				
				back.setPrefSize(100,30);	
				back.setFont(buttonFont);
				newLobby.setPrefSize(150, 30);
				newLobby.setFont(buttonFont);
				join.setPrefSize(150, 30);
				join.setFont(buttonFont);
				
				buttonPane.add(back, 0, 0);
				buttonPane.add(newLobby, 1, 0);
				buttonPane.add(join, 2, 0);
				buttonPane.setHgap(50);
				setBottom(buttonPane);
				setMargin(buttonPane, new Insets(0,0,60,0));
				
				TableView<UserData> table = new TableView<UserData>();
				
				TableColumn<UserData, String> usersColumn = new TableColumn<>("User");
				usersColumn.setCellValueFactory(new PropertyValueFactory<UserData, String>("UserName"));
				
				TableColumn<UserData, Integer> mapColumn = new TableColumn<>("Map");
				mapColumn.setCellValueFactory(new PropertyValueFactory<UserData, Integer>("Map"));
				
				TableColumn<UserData, Integer> winsColumn = new TableColumn<>("Wins");
				winsColumn.setCellValueFactory(new PropertyValueFactory<UserData, Integer>("Wins"));
				
				TableColumn<UserData, Integer> losesColumn = new TableColumn<>("Loses");
				losesColumn.setCellValueFactory(new PropertyValueFactory<UserData, Integer>("Loses"));
				
				table.getColumns().addAll(usersColumn, mapColumn, winsColumn, losesColumn);
				
				table.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
					changeInfoPanel(oldValue, newValue);
				});
				
				client.setupLobbyList(table);
				setRight(table);
				
				client.sendPacketHeader(PacketHeader.LOBBY);
				
				back.setOnAction(event -> {shrinkMenu();});
				newLobby.setOnAction(event -> {lobbyCreation();});
				join.setOnAction(event -> {
					client.connectToUser(table.getSelectionModel().getSelectedItem().getUserName());
					if(table.getSelectionModel().getSelectedItem().getMap() == 1)
						map = new LevelOne(scaleSize);
					else if(table.getSelectionModel().getSelectedItem().getMap() == 2)
						map = new LevelTwo(scaleSize);
					else
						map = new LevelThree(scaleSize);
					lobbyWaitScreen(false, table.getSelectionModel().getSelectedItem().getUserName());
				});
			}
			
			private void changeInfoPanel(UserData oldValue, UserData newValue) {
				GridPane infoPane = new GridPane();
				
				double winRatio;
				if(newValue.getLoses()!= 0)
					winRatio =  ((double)newValue.getWins())/newValue.getLoses();
				else 
					winRatio = 0;
				Label info = new Label("Lobby Info");
				Label playerInfo = new Label("Player Name: " + newValue.getUserName() + "\n\n" +
											 "Slected Map: Map " + newValue.getMap() + "\n\n" +
											 "Wins:  " + newValue.getWins() + "\n\n"+
											 "Loses: " + newValue.getLoses() + "\n\n" +
											 "Win Ratio: " + winRatio + "\n\n");
				
				info.setFont(new Font("Ariel", 30));
				playerInfo.setFont(new Font("Ariel", 15));
				
				infoPane.add(info, 0, 0);
				infoPane.add(playerInfo, 0, 1);
				
				info.setTextFill(textColor);
				playerInfo.setTextFill(textColor);
				
				setLeft(null);
				setLeft(infoPane);
			}


			/**
			 *	Draws the options when you select new game on the main menu
			 */
			public void lobbyCreation() {
				setLeft(null);
				setCenter(null);
				setBottom(null);
				setTop(null);
					GridPane gPane = new GridPane();
					
					Label newGameSettings = new Label("Map Selection");
					newGameSettings.setPrefSize(300, 100);
					newGameSettings.setFont(new Font("Ariel", 30));
					newGameSettings.setTextFill(textColor);
					
					Canvas mapPreview = new Canvas(300,300);
					GraphicsContext mapPreviewGC = mapPreview.getGraphicsContext2D();
					Button start = new Button("Start");
					Button back = new Button("Back");
					
					RadioButton map1 = new RadioButton("Map 1");
					RadioButton map2 = new RadioButton("Map 2");
					RadioButton map3 = new RadioButton("Map 3");

					map1.setPrefSize(100, 100);
					map2.setPrefSize(100, 100);
					map3.setPrefSize(100, 100);
					start.setPrefSize(150, 30);
					back.setPrefSize(100,30);
					
					map1.setFont(new Font("Ariel", 20));
					map2.setFont(new Font("Ariel", 20));
					map3.setFont(new Font("Ariel", 20));
					start.setFont(new Font("Ariel", 20));
					back.setFont(new Font("Ariel", 20));

					map1.setTextFill(textColor);
					map2.setTextFill(textColor);
					map3.setTextFill(textColor);
					
					ToggleGroup mapGroup = new ToggleGroup();
					map1.setToggleGroup(mapGroup);
					map2.setToggleGroup(mapGroup);
					map3.setToggleGroup(mapGroup);
				
					map1.setSelected(true);
					
					
					map = new LevelOne(previewScaleSize);
					map.draw(mapPreviewGC, map1Image);
				
					//radial button event handlers
					map1.setOnAction(event -> {map = new LevelOne(previewScaleSize);map.draw(mapPreviewGC, map1Image);mapNumb = 1;});
					map2.setOnAction(event -> {map = new LevelTwo(previewScaleSize);map.draw(mapPreviewGC, map2Image);mapNumb = 2;});
					map3.setOnAction(event -> {map = new LevelThree(previewScaleSize);map.draw(mapPreviewGC, map3Image);mapNumb = 3;});
				
					gPane.add(newGameSettings,0,0);
					gPane.add(map1, 0, 1);
					gPane.add(map2, 0, 2);
					gPane.add(map3, 0, 3);
					
					setCenter(gPane);
					setMargin(gPane, new Insets(0,0,0,30));
					setRight(mapPreview);
					setMargin(mapPreview, new Insets(100,0,0,00));
					
					GridPane buttonPane = new GridPane();
					buttonPane.add(back, 0, 0);
					buttonPane.add(start, 1, 0);
					buttonPane.setHgap(50);
					setBottom(buttonPane);
					setMargin(buttonPane, new Insets(0,0,60,0));
					
					//button event handlers
					back.setOnAction(event -> {shrinkMenu();});
					start.setOnAction(event -> {
						if(mapNumb == 1)
							map = new LevelOne(scaleSize);
						else if(mapNumb == 2)
							map = new LevelTwo(scaleSize);
						else	
							map = new LevelThree(scaleSize);
						lobbyWaitScreen(true, "Waiting for player");
					});
									
			}//end of lobbyCreation
			
			/**
			 * creates a wait screen for both players to ready up
			 * @param madeLobby determines if the user made the lobby or not to determine whether to call createLobby in client
			 */
			private void lobbyWaitScreen(boolean madeLobby, String secondUser) {
				setTop(null);
				setCenter(null);
				setLeft(null);
				setRight(null);
				setBottom(null);
				secondUsername = secondUser;
				
				GridPane gPane = new GridPane();
				GridPane buttonPane = new GridPane();

				Label multiplayerTitle = new Label("Multiplayer Lobby");
				multiplayerTitle.setPrefSize(300, 100);
				multiplayerTitle.setFont(new Font("Ariel", 30));
				multiplayerTitle.setTextFill(textColor);
			
				gPane.setHgap(100);
				
				Label playerLabel = new Label("Player");
				Label readyLabel = new Label("Ready");
				
				Label playerOne = new Label(client.getUserName());
				Label playerOneReady = new Label("X");
				
				Label playerTwo = new Label(secondUser);
				playerTwoReady = new Label("X");
				
				playerLabel.setFont(new Font("Ariel", 30));
				playerLabel.setTextFill(textColor);
				readyLabel.setFont(new Font("Ariel", 30));
				readyLabel.setTextFill(textColor);
				playerOne.setFont(new Font("Ariel", 30));
				playerOne.setTextFill(textColor);
				playerOneReady.setFont(new Font("Ariel", 30));
				playerOneReady.setTextFill(textColor);
				playerTwo.setFont(new Font("Ariel", 30));
				playerTwo.setTextFill(textColor);
				playerTwoReady.setFont(new Font("Ariel", 30));
				playerTwoReady.setTextFill(textColor);
			
				playerTwo.setPrefWidth(300);
				
				gPane.add(playerLabel, 0, 0);
				gPane.add(readyLabel, 1, 0);
				gPane.add(playerOne, 0, 1);
				gPane.add(playerOneReady, 1,1);
				gPane.add(playerTwo, 0, 2);
				gPane.add(playerTwoReady, 1,2);
				setCenter(gPane);
				
				Button ready = new Button ("Ready");
					
				ready.setPrefSize(150, 30);
				ready.setFont(new Font("Ariel", 20));
				buttonPane.add(ready, 1, 0);	
				
				Button back = new Button("Back");			
				back.setPrefSize(100,30);	
				back.setFont(new Font("Ariel", 20));
				
				setTop(multiplayerTitle);
				setAlignment(multiplayerTitle, Pos.TOP_CENTER);
				
				buttonPane.add(back, 0, 0);
				buttonPane.setHgap(50);
				
				if(madeLobby) {
					Button start = new Button("Start");
					start.setPrefSize(100, 30);
					start.setFont(new Font("Ariel", 20));
					buttonPane.add(start, 2, 0);
					start.setOnAction(event ->{
						if(playerOneReady.getText().equals("✔") && playerTwoReady.getText().equals("✔")) {
							client.startGame();
							startMultiplayerGUI();
						}
						
					});
				}
				
				setBottom(buttonPane);
				setMargin(buttonPane, new Insets(0,0,60,0));
				
				//button event handlers
				back.setOnAction(event->{multiplayerLobby();client.lobbyDisconnect();});
				ready.setOnAction(event ->{
					if(playerOneReady.getText().equals("X")) {
						playerOneReady.setText("✔");
						client.updateReadyStatus(true);
					}
					else {
						playerOneReady.setText("X");
						client.updateReadyStatus(false);
					}
				});
				
				

				if(madeLobby) {
					ObservableList<String> userList = FXCollections.observableArrayList();
					userList.addListener(new ListChangeListener<String>() {
			        	@Override
			        	public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
			        		Platform.runLater(new connectionListener(userList, playerOne, playerTwo));
			        	}
			        
			    	});
					client.createLobby(mapNumb, userList);
				}
			}
			
			
			/**
			 * 
			 */
			private class connectionListener extends Task<Object> {
				ObservableList<String> userList;
				Label playerOne, playerTwo;
				public connectionListener(ObservableList<String> userList, Label playerOne, Label playerTwo) {
					this.userList = userList;
					this.playerOne = playerOne;
					this.playerTwo = playerTwo;
				}

				@Override
				public void run() {
					if(userList.size() == 1)
	        			playerOne.setText(userList.get(0));
	        		if(userList.size() == 2) {
	        			playerTwo.setText(userList.get(1));
	        			secondUsername = userList.get(1);
	        		}
	        	
				}

				@Override
				protected Object call() throws Exception {
					return null;
				}
			}//end of connectionListener

			
		}	
	}
		
	/**
	 * @param ready ready status of the other player
	 * @param message the message displayed if ready == false
	 */
	public void playerTwoReady(boolean ready, String message) {
		Platform.runLater(new readyListener(ready, message));
	}
	
	/**
	 * Updates the gui with a check or an X if the other player is ready
	 */
	private class readyListener extends Task<Object> {
		boolean readyState;
		String message;
		public readyListener(boolean ready, String message) {
			readyState = ready;
			this.message = message;
		}

		@Override
		public void run() {
			if(readyState)
				playerTwoReady.setText("✔");
			else
				playerTwoReady.setText(message);
			}

		@Override
		protected Object call() throws Exception {
			return null;
		}
	}//end of readyListener

	/**
	 * lets the client tell the gui to start the multiplayer gui
	 */
	public void startMultiplayer() {
		Platform.runLater(new multiplayerStart());
	}
	
	/**
	 * Starts the multiplayer game
	 */
	private class multiplayerStart extends Task<Object> {

		@Override
		public void run() {
			startMultiplayerGUI();
		}

		@Override
		protected Object call() throws Exception {
			return null;
		}
	}//end of readyListener

	
	/**
	 * Starts the single player GUI for the Tower Defense Game
	 */
	private void startSinglePlayerGUI(){
		multiplayer = false;
		multiplayerSideShift = 0;
		
		window = new StackPane();
		Scene scene = new Scene(window, (scaleSize*11)+250, (scaleSize*11));
		Canvas canvas = new Canvas(scaleSize*11, scaleSize*11);
		gc = canvas.getGraphicsContext2D();
		Font defFault = new Font("Verdana", 18);
		player = new Player();
		player.setMapNumber(map.getMapInt());

		controller = new GameBoy(scaleSize, map, player, gc);
		
		if(loadedGame){
			player = PersistenceObject.readPlayerFromFile();
			controller.setWaveNumber(player.getWaveNumber());
			if(player.getMapNumber() == 1)
				map = new LevelOne(scaleSize);
			else if(player.getMapNumber() == 2)
				map = new LevelTwo(scaleSize);
			else
				map = new LevelThree(scaleSize);
			controller.setEnemies(PersistenceObject.readEnemiesFromFile());
			controller.setTowers(PersistenceObject.readTowersFromFile());
			controller.setMap(map);
			controller.setWaveNumber(player.getWaveNumber());
		}

		if(map.getClass() == LevelOne.class)
			map.draw(gc, map1Image);
		else if(map.getClass() == LevelTwo.class)
			map.draw(gc, map2Image);
		else if(map.getClass() == LevelThree.class)
			map.draw(gc, map3Image);
		controller.drawObjects();
		
		//START TOP ROW
		topPane = new GridPane();
		topPane.setVgap(10);
		topPane.setHgap(0);
		start = new Button("II");
		start.setOnAction(new StartTimerButtonListener());
		start.setPrefSize(60, 40);
		topPane.add(start, 0, 1);
		ImageView moneyPic = new ImageView("file:images/gold.png");
		moneyPic.setFitWidth(70);
		moneyPic.setFitHeight(50);
		moneyLabel = new Label("" + player.getMoneys());
		moneyLabel.setFont(defFault);
		waveLabel = new Label("Wave: " + controller.getWaveNumber());
		waveLabel.setFont(defFault);
		//enemyCountLabel = new Label("Enemies to spawn: 0");
		healthLabel = new Label("Health: " + player.getHealth());
		healthLabel.setFont(defFault);
		healthBar = new Line(0, 0, player.getHealth(), 0);
		healthBar.setStrokeWidth(20);
		healthBar.setStroke(Color.LIME);
		//healthLabel.setPrefSize(75, 25);
		topPane.add(moneyPic, 0, 2);
		topPane.add(moneyLabel, 1, 2);
		topPane.add(waveLabel, 1, 1);
		topPane.add(healthLabel, 0, 5);
		//topPane.add(healthBar, 0, 6);
		window.setAlignment(topPane, Pos.TOP_RIGHT);
		window.setMargin(topPane, new Insets(30,0,0, scaleSize*11 + 40)); //positioning of player info
		window.setAlignment(healthBar, Pos.TOP_LEFT);
		window.setMargin(healthBar, new Insets(200, 0, 0, scaleSize*11 + 40));
		
		window.getChildren().add(topPane);
		window.getChildren().add(healthBar);
		//END TOP ROW
		
		window.setAlignment(canvas, Pos.CENTER_LEFT);
		window.setMargin(canvas, new Insets(0,0,0,0));
		window.getChildren().add(canvas);
		scene.setOnKeyPressed(new KeyListener());
		
		//SHOP
		mouseLoc = new Point2D.Double();
		canvas.setOnMouseMoved(new mouseMovement());
		canvas.setOnMouseClicked(new mapMouseClick());
		shop = new Shop(scaleSize);
		window.setAlignment(shop, Pos.BOTTOM_RIGHT);
		window.setMargin(shop, new Insets(300,0,0,scaleSize*11));
		window.getChildren().add(shop);
		
		primaryStage.setTitle("Tower Defense");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		gameStart = true;
		
		timeline = new Timeline(new KeyFrame(Duration.millis(50), new GameStarter()));
	    timeline.setCycleCount(Animation.INDEFINITE);
	    paused = true;
	    escPaused = false;
		
	} 
	
	/**
	 * Handler for the GameController
	 */
	private class GameStarter implements EventHandler<ActionEvent> {
	    int tic = 0;
	    int fireworkCount = 0;
	    int fireworkLimit = 14;
	    @Override
	    public void handle(ActionEvent event) {
	    	if(shop.getSelectedTower() != null) {
	    		togglePause();
	    		return;
		    }
	    	if (controller.playerWon()) {
	    		if (fireworkCount < fireworkLimit) {
	    			if (tic % 5 == 0) {
	    				controller.fireWorks();
	    				fireworkCount++;
	    			}
	    		}
	    	} 
	    	else {
	    		tic++;
	    		if(map.getClass() == LevelOne.class)
					map.draw(gc, map1Image);
				else if(map.getClass() == LevelTwo.class)
					map.draw(gc, map2Image);
				else if(map.getClass() == LevelThree.class)
					map.draw(gc, map3Image);
		      
		      controller.singleTic(); //single tic of game
		      if(tic%5 == 0) {
		    	  	if(multiplayer) {
		    	  		client.sendEnemies(controller.getEnemies());
		    	  	}
		      }
		      if (controller.canGenWave()) {
		    	  	controller.generateWave(); //starts a new wave if possible
		    	}
		      updateGUI();
		      shop.redrawShopImages();
	    	}
	      updateGUI();
	      shop.redrawShopImages();
	      
	      if(tic > lastTic)
			lastTic = tic;
			tic++;
			if (controller.gameOver() && fireworkCount >= fireworkLimit) {
				if(multiplayer)
					client.sendGameOver(controller.playerWon(), player, controller.getEnemiesKilled(),controller.getTowersBought());
				gameOverScreen = new gameOverScreen();
				gameOverScreen.setMaxSize(400, 400);
				window.getChildren().add(gameOverScreen);
				timeline.stop();
			  }
		}
}

    /**
     * White the game is going, this updates the player info that is in the top right of the screen (wave number, health, etc..)
     */
    private void updateGUI() {
    	moneyLabel.setText("" + player.getMoneys());
    	if (controller.getWaveNumber() > controller.getBossWaveNumber())
    		waveLabel.setText("BOSS WAVE");
    	else
    		waveLabel.setText("Wave: " + controller.getWaveNumber());
    	int playerHealth;
    	if(multiplayer) {
    		playerHealth = Math.min(player.getHealth(), miniMapPlayer.getHealth());
    		miniMapPlayer.takeDamage(miniMapPlayer.getHealth()-playerHealth);
    		player.takeDamage(player.getHealth()-playerHealth);
    	}
    	else
    		playerHealth = player.getHealth();
    	healthLabel.setText("Health: " + player.getHealth());
    	window.getChildren().remove(healthBar);
    	healthBar = new Line(0, 0, player.getHealth(), 0);
    	healthBar.setStrokeWidth(20);
    	if(player.getHealth() > 76)
    		healthBar.setStroke(Color.LIME);
    	else if (player.getHealth() > 26)
    		healthBar.setStroke(Color.GOLD);
    	else
    		healthBar.setStroke(Color.RED);
    	window.setAlignment(healthBar, Pos.TOP_LEFT);
		window.setMargin(healthBar, new Insets(200, 0, 0, scaleSize*11 + 40+multiplayerSideShift));
    	window.getChildren().add(healthBar);
    }
	    

	/**
	 * Start Button for Iteration 1
	 */
	private class StartTimerButtonListener implements EventHandler<ActionEvent> {
	    @Override
	    public void handle(ActionEvent event) {
	    	if (controller.gameOver())
	    		return;
	    	gameStart = false;
			if(paused) {
				timeline.setRate(1.0);
				timeline.play();
				start.setText(">");
				paused = false;
				if(multiplayer)
					client.sendGameState(PacketHeader.GAMESPEED, 1);
			}
			else if(start.getText().equals(">")) {
				timeline.setRate(2.0);
				start.setText(">>");
				if(multiplayer)
					client.sendGameState(PacketHeader.GAMESPEED, 2);
			}
			else {
				timeline.pause();
				start.setText("II");
				paused = true;
				if(multiplayer)
					client.sendGameState(PacketHeader.GAMESPEED, 0);
			}}
	  }
	

	/**
	 * Handles the key inputs from the user
	 */
	private class KeyListener implements EventHandler<KeyEvent>{
		
		public void handle(KeyEvent event) {
			if (controller.gameOver())
	    		return;
			switch(event.getCode()) {
				case ESCAPE: 
					toggleEscape();
					break;
				case SPACE:
					togglePause();
					break;
				case T: 
					if(escPaused)
						break;
					if(!gameStart &&paused == false)
							togglePause();
					if(paused == true) {
						if(!(shop.getSelectedTower() instanceof LaserTower)) 
							shop.setSelectedTower(new LaserTower(0, 0, scaleSize));
						else
							shop.setSelectedTower(null);
						drawMouseSelection();
					}
					break;
				case P: 
					if(escPaused)
						break;
					if(!gameStart && paused == false)
						togglePause();
					if(paused == true) {
						if(!(shop.getSelectedTower() instanceof PoisonTower)) 
							shop.setSelectedTower(new PoisonTower(0, 0, scaleSize));
						else
							shop.setSelectedTower(null);
						drawMouseSelection();
					}
					break;
			
			}
		}
	}
	
	/**
	 * Toggles between the game being paused and the game running
	 */
	protected void togglePause() {
		gameStart = false;
		if(paused) {
			timeline.play();
			start.setText(">");
			paused = false;
			if(multiplayer)
				client.sendGameState(PacketHeader.GAMESPEED, 1);
		}
		else {
			timeline.pause();
			start.setText("II");
			paused = true;
			if(multiplayer)
				client.sendGameState(PacketHeader.GAMESPEED, 0);
		}
	}
	
	/**
	 * toggles between the Esc Menu being displayed and not
	 */
	private void toggleEscape() {
		if(controller.gameOver())
			return;
		window.getChildren().remove(escScreen);
		if(!gameStart)
			if(!escPaused) {
				if(!paused)
					togglePause();
			}
			else
				togglePause();
		if(escPaused) {
			escPaused= false;
			return;
		}
		escPaused = true;
		escScreen = new escScreen();
		window.setMargin(escScreen, new Insets(0, 150, 0, multiplayerSideShift));
		window.getChildren().add(escScreen);
	}
	
	/**
	 * Draws the preview of the tower selected from the shop on the current location of the mouse
	 * Also draws the preview range of the the tower
	 */
	private void drawMouseSelection() {
		if(shop.getSelectedTower() == null) 
			return;
		else {
			if(map.getClass() == LevelOne.class)
				map.draw(gc, map1Image);
			else if(map.getClass() == LevelTwo.class)
				map.draw(gc, map2Image);
			else if(map.getClass() == LevelThree.class)
				map.draw(gc, map3Image);
			controller.drawObjects();
			Color validTowerLocationFill = Color.rgb(0,140,0,0.5);
			Color invalidTowerLocationFill = Color.rgb(140, 0, 0, 0.5);
			int rangeRadius = scaleSize*(shop.getSelectedTower().getRange()+1);
			if(controller.validTowerLocation((int) (mouseLoc.getX()-multiplayerSideShift)/scaleSize, (int) mouseLoc.getY()/scaleSize) 
													&& player.getMoneys() >= shop.getSelectedTower().getTowerCost())
				gc.setFill(validTowerLocationFill);
			else
				gc.setFill(invalidTowerLocationFill);

			Tower tower = shop.getSelectedTower();
			
			gc.fillOval(mouseLoc.getX()-rangeRadius-multiplayerSideShift, mouseLoc.getY()-rangeRadius, (double) rangeRadius*2, (double) rangeRadius*2);
			//Tower tower = shop.getSelectedTower();
			if(tower.getClass() == LaserTower.class && tower.getTowerLvl() == 1)
				gc.drawImage(laserTower1, mouseLoc.getX()-scaleSize/2-multiplayerSideShift, mouseLoc.getY()-scaleSize*0.5, (double) scaleSize, (double) scaleSize);
			else if(tower.getClass() == LaserTower.class && tower.getTowerLvl() == 2)
				gc.drawImage(laserTower2, mouseLoc.getX()-scaleSize/2-multiplayerSideShift, mouseLoc.getY()-scaleSize*0.5, (double) scaleSize, (double) scaleSize);
			else if(tower.getClass() == PoisonTower.class && tower.getTowerLvl() == 1)
				gc.drawImage(poisonTower1, mouseLoc.getX()-scaleSize/2-multiplayerSideShift, mouseLoc.getY()-scaleSize*0.5, (double) scaleSize, (double) scaleSize);
			else if(tower.getClass() == PoisonTower.class && tower.getTowerLvl() == 2)
				gc.drawImage(poisonTower2, mouseLoc.getX()-scaleSize/2-multiplayerSideShift, mouseLoc.getY()-scaleSize*0.5, (double) scaleSize, (double) scaleSize);
			else if(tower.getClass() == TeleporterTower.class && tower.getTowerLvl() == 1)
				gc.drawImage(teleportTower1, mouseLoc.getX()-scaleSize/2-multiplayerSideShift, mouseLoc.getY()-scaleSize*0.5, (double) scaleSize, (double) scaleSize);
			else if(tower.getClass() == TeleporterTower.class && tower.getTowerLvl() == 2)
				gc.drawImage(teleportTower2, mouseLoc.getX()-scaleSize/2-multiplayerSideShift, mouseLoc.getY()-scaleSize*0.5, (double) scaleSize, (double) scaleSize);		
			}
	}
	
	/**
	 * When the mouse is moved when placing a tower, it draws a preview circle (tower range) and the the tower
	 */
	private class mouseMovement implements EventHandler<MouseEvent>{
		@Override
		public void handle(MouseEvent event) {
			if(paused);
			mouseLoc.setLocation(event.getSceneX(), event.getSceneY());
			drawMouseSelection();
		}

	}
	
	/**
	 * places the selected tower when the mouse is clicked over a empty square on the map and updates the money gui 
	 * if the user has clicked on a tower or an enemy this gives a popup to the user showing the information of that object
	 */
	private class mapMouseClick implements EventHandler<MouseEvent>{
		@Override
		public void handle(MouseEvent event) {
			int xClicked = (int) ((event.getSceneX()-multiplayerSideShift)/scaleSize);
			int yClicked = (int) (event.getSceneY()/scaleSize);
			window.getChildren().remove(upgradeScreen);
			window.getChildren().remove(enemyScreen);
			if(controller.gameOver())
				return;
			if(shop.getSelectedTower() == null) {
				if(controller.getTower(xClicked, yClicked) != null) {
					upgradeScreen = new upgradeScreenGUI(controller.getTower(xClicked, yClicked));
					upgradeScreen.setMaxSize(130, 120);
					window.setAlignment(upgradeScreen, Pos.TOP_LEFT);
					window.setMargin(upgradeScreen, new Insets(event.getSceneY(), 0,0,event.getSceneX()));
					window.getChildren().add(upgradeScreen);
				
				}
				else if(controller.getEnemy(xClicked, yClicked) != null) {
					enemyScreen = new enemyScreenGUI(controller.getEnemy(xClicked, yClicked));
					enemyScreen.setMaxSize(130,120);
					window.setAlignment(enemyScreen, Pos.TOP_LEFT);
					window.setMargin(enemyScreen, new Insets(event.getSceneY(), 0,0,event.getSceneX()));
					window.getChildren().add(enemyScreen);
				}
				return;
			}
			
			System.out.println("double: " + event.getSceneX() + "  int : " + xClicked);
			System.out.println("double: " + event.getSceneY() + "  int : " + yClicked);
			Tower newTower;
			if(shop.getSelectedTower() instanceof LaserTower)
				newTower = new LaserTower(xClicked, yClicked, scaleSize);
			else if(shop.getSelectedTower() instanceof PoisonTower)
				newTower = new PoisonTower(xClicked, yClicked, scaleSize);
			else 
				newTower = new TeleporterTower(xClicked, yClicked, scaleSize);
				
			controller.addTower(newTower);
	    	moneyLabel.setText("Moneys: " + player.getMoneys());
			shop.redrawShopImages();
			
			shop.setSelectedTower(null);
			client.sendTowers(controller.getTowers());
		}

	}
	
	/**
	 * Sets up and draws the Shop GUI and functionality
	 * @author Derian Davila Acuna
	 */
	private class Shop extends GridPane {

		public Tower selectedTower;
		
		private int pixelsPerMapSquare;
		
		private GraphicsContext beamTgc;
		private GraphicsContext poisonTgc;
		private GraphicsContext teleTgc;
		private Canvas beamTCanvas;
		private Canvas poisonTCanvas;
		private Canvas teleTCanvas;
		
		private LaserTower beamT;
		private PoisonTower poisonT;
		private TeleporterTower teleT;

		/**
		 * 
		 * @param pixelsPerMapSquare how many pixels long is a single map square
		 */
		public Shop(int pixelsPerMapSquare) {
			this.pixelsPerMapSquare = pixelsPerMapSquare;
		
			beamTCanvas = new Canvas(250, pixelsPerMapSquare+25);
			poisonTCanvas = new Canvas(250, pixelsPerMapSquare+25);
			teleTCanvas = new Canvas(250, pixelsPerMapSquare+25);
			beamTgc = beamTCanvas.getGraphicsContext2D();
			poisonTgc = poisonTCanvas.getGraphicsContext2D();
			teleTgc = teleTCanvas.getGraphicsContext2D();

			beamT = new LaserTower(0,0, scaleSize);
			poisonT = new PoisonTower(0,0, scaleSize);
			teleT = new TeleporterTower(0,0, scaleSize);

			Tooltip beamTip = new Tooltip("Name:   " + beamT.getName() + "\n" + 
										  "Cost:   " + beamT.getTowerCost() + "\n"+
										  "Damage: " + beamT.getDamage() + "\n" +
										  "Range:  " + beamT.getRange() + "\n");
			Tooltip poisonTip = new Tooltip("Name:   " + poisonT.getName() + "\n" +
											"Cost:   " + poisonT.getTowerCost() + "\n"+
											"Damage: " + poisonT.getDamage() + "\n" +
											"Range:  " + poisonT.getRange() + "\n" +
											"Desc:   " + "poisons the enemy for 10 damage each step");
			Tooltip teleTip = new Tooltip("Name:   " + teleT.getName() + "\n" +
										  "Cost:   " + teleT.getTowerCost() + "\n"+
										  "Damage: " + teleT.getDamage() + "\n" +
										  "Range:  " + teleT.getRange() + "\n");
			
			
			Tooltip.install(beamTCanvas, beamTip);
			Tooltip.install(poisonTCanvas, poisonTip);
			Tooltip.install(teleTCanvas, teleTip);
			
			this.add(beamTCanvas, 0 , 0);
			this.add(poisonTCanvas, 0 , 1);
			this.add(teleTCanvas, 0 , 2);
		
			redrawShopImages();
		
			beamTCanvas.setOnMouseClicked(event -> {setSelectedTower(new LaserTower(0,0,scaleSize));});
			poisonTCanvas.setOnMouseClicked(event -> {setSelectedTower(new PoisonTower(0,0,scaleSize));});
			teleTCanvas.setOnMouseClicked(event -> {setSelectedTower(new TeleporterTower(0,0,scaleSize));});
		}
		
		/**
		 * sets the selected tower to newTower unless they are the same type then it sets the selctedTower to null
		 * in the shop gui the tower is highlighted with a blue border
		 * @param newTower - tower to replace the currently selectedTower
		 */
		public void setSelectedTower(Tower newTower) {
			if(controller.gameOver())
				return;
			redrawShopImages();
			if(newTower == null) {
				selectedTower = null;
				timeline.setRate(1.0);
				timeline.play();
				start.setText(">");
				paused = false;
				return;
			}
			if(selectedTower != null && 
					selectedTower.getClass().equals(newTower.getClass())) { 
				timeline.setRate(1.0);
				timeline.play();
				start.setText(">");
				paused = false;
				selectedTower = null;
				return;
			}
			else {
				start.setText("II");
				selectedTower = newTower;
			}
			
			if(selectedTower instanceof LaserTower) {
				beamTgc.setFill(Color.BLUE);
				beamTgc.strokeRect(0, 0, beamTCanvas.getWidth(), beamTCanvas.getHeight());
				return;
			}
			if(selectedTower instanceof PoisonTower) {
				poisonTgc.setFill(Color.BLUE);
				poisonTgc.strokeRect(0, 0, poisonTCanvas.getWidth(), poisonTCanvas.getHeight());
				return;
			}
			teleTgc.setFill(Color.BLUE);
			teleTgc.strokeRect(0, 0, teleTCanvas.getWidth(), teleTCanvas.getHeight());
			return;
		}
		
		
		/**
		 * @return returns the selected tower
		 */
		public Tower getSelectedTower() {
			return selectedTower;
		}
		
		/**
		 * redraws all the images in the shop
		 * changes background of the item if the player can buy the turret
		 */
		public void redrawShopImages() {
			Paint canBuy = Color.LIGHTGREY;
			Paint canNotBuy = Color.rgb(140,0,0,0.7);
			int marginLeft = 70;
			int marginTop = 30;
			beamTgc.setFill(canBuy);
			beamTgc.fillRect(0, 0, beamTCanvas.getWidth(), beamTCanvas.getHeight());
			if(player.getMoneys() < beamT.getTowerCost()) {
				beamTgc.setFill(canNotBuy);
				beamTgc.fillRect(0, 0, beamTCanvas.getWidth(), beamTCanvas.getHeight());
			}
			beamTgc.setFill(Color.BLACK);
			beamTgc.setFont(Font.font("Arial", 15));
			beamTgc.fillText(beamT.getName(), marginLeft-5, marginTop);
			
			if(beamT.getTowerLvl() == 1)
				beamTgc.drawImage(laserTower1,marginLeft, 25, (double) pixelsPerMapSquare, (double) pixelsPerMapSquare);
			else
				beamTgc.drawImage(laserTower2,marginLeft, 25, (double) pixelsPerMapSquare, (double) pixelsPerMapSquare);
			
			poisonTgc.setFill(canBuy);
			poisonTgc.fillRect(0, 0, poisonTCanvas.getWidth(), poisonTCanvas.getHeight());
			if(player.getMoneys() < poisonT.getTowerCost()) {
				poisonTgc.setFill(canNotBuy);
				poisonTgc.fillRect(0, 0, poisonTCanvas.getWidth(), poisonTCanvas.getHeight());
			}
			if(poisonT.getTowerLvl() == 1)
				poisonTgc.drawImage(poisonTower1,marginLeft, 25, (double) pixelsPerMapSquare, (double) pixelsPerMapSquare);
			else
				poisonTgc.drawImage(poisonTower2,marginLeft, 25, (double) pixelsPerMapSquare, (double) pixelsPerMapSquare);
			poisonTgc.setFill(Color.BLACK);
			poisonTgc.setFont(Font.font("Arial", 15));
			poisonTgc.fillText(poisonT.getName(), marginLeft, marginTop);

			teleTgc.setFill(canBuy);
			teleTgc.fillRect(0, 0, teleTCanvas.getWidth(), teleTCanvas.getHeight());
			if(player.getMoneys() < teleT.getTowerCost()) {
				teleTgc.setFill(canNotBuy);
				teleTgc.fillRect(0, 0, teleTCanvas.getWidth(), teleTCanvas.getHeight());
			}			
			teleTgc.setFill(Color.BLACK);
			if(teleT.getTowerLvl() == 1)
				teleTgc.drawImage(teleportTower1,marginLeft, 25, (double) pixelsPerMapSquare, (double) pixelsPerMapSquare);
			else
				teleTgc.drawImage(teleportTower2,marginLeft, 25, (double) pixelsPerMapSquare, (double) pixelsPerMapSquare);
			teleTgc.setFont(Font.font("Arial", 15));
			teleTgc.fillText(teleT.getName(), marginLeft-10, marginTop);
		}

	}
	
	/**
	 * Screen that shows up with options when the esc key is pressed in game
	 *
	 */
	private class escScreen extends StackPane{
		public escScreen() {
			int height;
			if(multiplayer)
				height = 270;
			else
				height = 370;
			
			Canvas canvas = new Canvas(200, height);
			
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.setFill(new Color(0.89,0.89,0.89,1));
			gc.fillRect(0, 0, 200, 370);
			
			gc.setFill(Color.BLACK);
			gc.strokeRect(0, 0, 200, 400);
			
			this.getChildren().add(canvas);
			
			GridPane gPane = new GridPane();
			Button qGame = new Button("Quit Game");
			Button qmGame = new Button("Quit to Menu");
			Button resume = new Button("Resume");
			Button save = new Button("Save");
			
			qGame.setPrefSize(150, 60);
			qmGame.setPrefSize(150, 60);
			resume.setPrefSize(150, 60);
			save.setPrefSize(150, 60);
			
			qGame.setOnAction(event -> {
				if(multiplayer)
					client.gameDisconnect();
				primaryStage.close();
				});
			qmGame.setOnAction(event -> {
				window.getChildren().remove(this);
				if(multiplayer)
					client.gameDisconnect();
				timeline.stop();
				primaryStage.setScene(new Scene(new mainMenuGUI(),(scaleSize*11)+150, (scaleSize*11) + 25));
				primaryStage.show();
			});
			resume.setOnAction(event -> {
				toggleEscape();
			});
			
			save.setOnAction(event -> {
				PersistenceObject.writeEnemiesToFile(controller.getEnemies());
				PersistenceObject.writeTowersToFile(controller.getTowers());
				PersistenceObject.writePlayerFromFile(player);
				save.setText("Saved");
			});
			
			gPane.add(resume, 0, 0);
			if(multiplayer) {
				gPane.add(qmGame, 0, 1);
				gPane.add(qGame, 0, 2);	
			}
			else {
				gPane.add(save, 0, 1);
				gPane.add(qmGame, 0, 2);
				gPane.add(qGame, 0, 3);
			}
			gPane.setVgap(25);
			gPane.setMaxSize(150, 340);
			this.setAlignment(gPane, Pos.CENTER);

			this.setMargin(gPane, new Insets(35,0,0,0));

			if(multiplayer)
				this.setMargin(gPane, new Insets(100,0,0,0));
			else
				this.setMargin(gPane, new Insets(40,0,0,0));
			this.getChildren().add(gPane);
		}
	}

	/**
	 * When the user clicks on a tower this class will be created and a popup info square will be drawn right next to the tower
	 * This will display information of the tower like the name,damage,range, and rate of fire
	 * This also contains a button that allows the user to upgrade the tower
	 * @author Derian Davila Acuna
	 */
	private class upgradeScreenGUI extends StackPane{
		public upgradeScreenGUI(Tower tower) {
			Canvas canvas = new Canvas(130, 130);
			GraphicsContext gc = canvas.getGraphicsContext2D();
			
			gc.setFill(new Color(0.3,0.3,0.3,0.7));
			gc.fillRect(0, 0, 130, 130);
			
			String info = tower.getName() +"\n" + 
						  "Damage: " + tower.getDamage() + "\n" + 
						  "Range: " + tower.getRange() + "\n" + 
						  "Fire Rate: " + (int)(1000/(double)tower.getDelay()/50)*60 + "/minute";
			if(tower.getTowerLvl() == 1)
						  info += "\nUpgrade Cost: " + tower.getUpgradeCost();

			
			gc.setFont(new Font("Ariel", 12));
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.HONEYDEW);
			gc.fillText(info, 5, 10);

			getChildren().add(canvas);
			
			if(tower.getTowerLvl() == 1) {
				Button upgrade = new Button("Upgrade");
				upgrade.setOnAction(event ->{
					if(player.getMoneys() >= tower.getUpgradeCost()) {
						player.subtractCost(tower.getUpgradeCost());
						tower.upgradeTower();
						window.getChildren().remove(this);
					}
				});

				setAlignment(upgrade, Pos.BOTTOM_CENTER);
				getChildren().add(upgrade);		
			}
		
		}
	}
	
	/**
	 * When the user clicks on a enemy this class will be created and a popup info square will be drawn right next to the enemy
	 * This will display information of the enemy like the name,damage,and health
	 * @author Derian Davila Acuna
	 */
	private class enemyScreenGUI extends StackPane{
		public enemyScreenGUI(Enemy enemy) {
			Canvas canvas = new Canvas(130, 130);
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.setFill(new Color(0.3,0.3,0.3,0.7));
			gc.fillRect(0, 0, 130, 130);
			
			String info = enemy.getName() +"\n" + 
						  "Damage: " + enemy.getDamage() + "\n" + 
						  "Health: " + enemy.getHealth() + "\n" + 
						  "Armor: " + enemy.getArmor();

			
			gc.setFont(new Font("Ariel", 12));
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.HONEYDEW);
			gc.fillText(info, 5, 10);
			
			getChildren().add(canvas);
		}
	}
	
	/**
	 * Draws in the Game Over pop up screen
	 */
	private class gameOverScreen extends StackPane {
		public gameOverScreen() {
			Canvas canvas = new Canvas(400, 400);
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.setFill(new Color(0.89,0.89,0.89,1));
			gc.fillRect(0, 0, 400, 400);

			gc.setFill(Color.BLACK);
			gc.strokeRect(0, 0, 400, 400);

			gc.setFont(new Font("Ariel", 30));

			String stats = "";
			
			if(multiplayer) {
				if(controller.playerDead() || (controller.gameOver() && playerTwoGameover)) {
					if(controller.playerDead() || !playerTwoWon || miniMapPlayer.isDead()) {
						client.sendServerGameOver(PacketHeader.GAMELOST);
						gc.strokeText("You Lose", 125, 50);
					}
					else {
						client.sendServerGameOver(PacketHeader.GAMEWON);
						gc.strokeText("You Win", 125, 50);
					}
					
					int totalSeconds = lastTic/20;
					stats =
							"Your Total Time: " + totalSeconds/60 + " minutes " + totalSeconds%60 + " seconds" + "\n\n" + 
									"Enemies Killed: " + (controller.getEnemiesKilled()+playerTwoEnemyKills) + "\n\n" + 
									"Towers Bought: " + (controller.getTowersBought()+playerTwoTowersBought) + "\n\n" +
									"Total Moneys: " + (player.getTotalMoneys()+miniMapPlayer.getTotalMoneys());
				
				}
				else if(controller.isPlayerDC())
					gc.strokeText("Other player has DC'ed", 50, 50);
				else
					gc.strokeText("Waiting for other player", 50, 50);
			}
			else {
				if(controller.playerDead())
					gc.strokeText("You Lose", 125, 50);
				else
					gc.strokeText("You Win", 125, 50);
				int totalSeconds = lastTic/20;
				stats =
						"Total Time: " + totalSeconds/60 + " minutes " + totalSeconds%60 + " seconds" + "\n\n" + 
								"Enemies Killed: " + controller.getEnemiesKilled() + "\n\n" + 
								"Towers Bought: " + controller.getTowersBought() + "\n\n" +
								"Total Moneys: " + player.getTotalMoneys();
			
			}			

			setMargin(canvas, new Insets(0,150,0,multiplayerSideShift));
			getChildren().add(canvas);


			Label statLabel = new Label(stats);
			statLabel.setFont(new Font("Ariel", 15));

			GridPane gPane = new GridPane();
			Button qGame = new Button("Quit Game");
			Button qmGame = new Button("Quit to Menu");
			Button restart = new Button("Restart");

			qGame.setPrefSize(100, 40);
			qmGame.setPrefSize(100, 40);
			restart.setPrefSize(100, 40);


			qGame.setOnAction(event -> {primaryStage.close();});
			qmGame.setOnAction(event -> {
				if(multiplayer)
					client.gameDisconnect();
				window.getChildren().remove(gPane);
				window.getChildren().remove(qGame);
				window.getChildren().remove(qmGame);
				window.getChildren().remove(restart);
				window.getChildren().remove(this);
				primaryStage.setScene(new Scene(new mainMenuGUI(),(scaleSize*11)+150, (scaleSize*11) + 25));
				primaryStage.show();
			});
			restart.setOnAction(event -> {
				if(multiplayer)
					client.gameDisconnect();
				window.getChildren().remove(gPane);
				window.getChildren().remove(qGame);
				window.getChildren().remove(qmGame);
				window.getChildren().remove(restart);
				if(map instanceof LevelOne)
					map = new LevelOne(scaleSize);
				else if(map instanceof LevelTwo)
					map = new LevelTwo(scaleSize);
				else
					map = new LevelThree(scaleSize);
				startSinglePlayerGUI();
			});

			if(!multiplayer) {
				gPane.add(restart, 0, 0);
				gPane.add(qmGame, 1, 0);
				gPane.add(qGame, 2, 0);
			}
			else {
				gPane.add(qmGame, 0, 0);
				gPane.add(qGame, 1, 0);
			}
				
			gPane.setHgap(25);
			gPane.setMaxSize(375, 40);

			setMargin(statLabel, new Insets(0,150,100,multiplayerSideShift));
			setAlignment(gPane, Pos.BOTTOM_LEFT);
			setMargin(gPane, new Insets(0, 0, 15, 25+multiplayerSideShift));

			getChildren().add(statLabel);
			getChildren().add(gPane);
		}
	}
	
	
	/**
	 * Starts the single player GUI for the Tower Defense Game
	 */
	private void startMultiplayerGUI(){
		multiplayer = true;
		multiplayerSideShift = 200;
		window = new StackPane();
		Scene scene = new Scene(window, (scaleSize*11)+250+multiplayerSideShift, (scaleSize*11));
		Canvas canvas = new Canvas(scaleSize*11, scaleSize*11);
		
		gc = canvas.getGraphicsContext2D();
		Font defFault = new Font("Arial", 18);
		player = new Player();

		controller = new GameBoy(scaleSize, map, player, gc);

		if(map.getClass() == LevelOne.class)
			map.draw(gc, map1Image);
		else if(map.getClass() == LevelTwo.class)
			map.draw(gc, map2Image);
		else if(map.getClass() == LevelThree.class)
			map.draw(gc, map3Image);
		
		//START TOP ROW
		topPane = new GridPane();
		topPane.setVgap(10);
		topPane.setHgap(0);
		start = new Button("II");
		start.setOnAction(new StartTimerButtonListener());
		start.setPrefSize(60, 40);
		topPane.add(start, 0, 1);
		ImageView moneyPic = new ImageView("file:images/gold.png");
		moneyPic.setFitWidth(50);
		moneyPic.setFitHeight(50);
		moneyLabel = new Label("" + player.getMoneys());
		moneyLabel.setFont(defFault);
		waveLabel = new Label("Wave: 0");
		waveLabel.setFont(defFault);
		//enemyCountLabel = new Label("Enemies to spawn: 0");
		healthLabel = new Label("Health: " + player.getHealth());
		healthLabel.setFont(defFault);
		healthBar = new Line(0, 0, player.getHealth(), 0);
		healthBar.setStrokeWidth(20);
		healthBar.setStroke(Color.LIME);
		//healthLabel.setPrefSize(75, 25);
		topPane.add(moneyPic, 0, 2);
		topPane.add(moneyLabel, 1, 2);
		topPane.add(waveLabel, 1, 1);
		topPane.add(healthLabel, 0, 5);
		window.setAlignment(topPane, Pos.TOP_RIGHT);
		window.setMargin(topPane, new Insets(30,0,0, scaleSize*11 + 40+multiplayerSideShift)); //positioning of player info
		window.setAlignment(healthBar, Pos.TOP_LEFT);
		window.setMargin(healthBar, new Insets(200, 0, 0, scaleSize*11 + 40+multiplayerSideShift));
		
		window.getChildren().add(topPane);
		window.getChildren().add(healthBar);
		//END TOP ROW
		
		window.setAlignment(canvas, Pos.CENTER_LEFT);

		window.setMargin(canvas, new Insets(0,0,0,multiplayerSideShift));

		window.getChildren().add(canvas);
		scene.setOnKeyPressed(new KeyListener());
		
		//Left sidebar
		Label playerName = new Label(secondUsername + "'s View");
		Canvas minimapCan = new Canvas(multiplayerSideShift-5, multiplayerSideShift-5);
		GraphicsContext minimapGc = minimapCan.getGraphicsContext2D();
		Map miniMap;
	
		if(map.getClass() == LevelOne.class) {
			miniMap= new LevelOne(multiplayerSideShift/11);
			miniMap.draw(minimapGc, map1Image);
		}
		else if(map.getClass() == LevelTwo.class) {
			miniMap = new LevelTwo(multiplayerSideShift/11);
			miniMap.draw(minimapGc, map2Image);
		}
		else{
			miniMap = new LevelThree(multiplayerSideShift/11);
			miniMap.draw(minimapGc, map3Image);
		}
		window.setAlignment(minimapCan, Pos.TOP_LEFT);
		window.setMargin(minimapCan, new Insets(25,0,0,0));
		window.getChildren().add(minimapCan);

		playerName.setMaxSize(200, 15);
		window.setAlignment(playerName, Pos.TOP_LEFT);
		window.setMargin(playerName, new Insets(10,0,0,0));
		window.getChildren().add(playerName);
		
		miniMapPlayer = new Player();
	    miniMapController = new GameBoy(multiplayerSideShift/11,miniMap, miniMapPlayer, minimapGc);
	    miniMapTimeline = new Timeline(new KeyFrame(Duration.millis(50), new GameStarterMiniMap(miniMap, minimapGc)));
	    miniMapTimeline.setCycleCount(Animation.INDEFINITE);
	 //   miniMapTimeline.play();
		
		ListView<String> chatList = new ListView<String>();
		TextField chatInput = new TextField();
		client.setupChat(chatList);
		
		chatList.setMaxSize(multiplayerSideShift, 500*heightScale);
		chatInput.setMaxSize(multiplayerSideShift,50);
		window.setAlignment(chatList, Pos.BOTTOM_LEFT);
		window.setMargin(chatList, new Insets(0,0,100,0));
		window.setAlignment(chatInput, Pos.BOTTOM_LEFT);
		window.setMargin(chatInput, new Insets(0,0,50,0));
		window.getChildren().add(chatList);
		window.getChildren().add(chatInput);
		
		chatInput.setOnAction(event -> {
			client.sendMessage(chatInput.getText());
			chatInput.setText("");
		});
		
		//SHOP
		mouseLoc = new Point2D.Double();
		canvas.setOnMouseMoved(new mouseMovement());
		canvas.setOnMouseClicked(new mapMouseClick());
		shop = new Shop(scaleSize);

		window.setAlignment(shop, Pos.BOTTOM_RIGHT);
		window.setMargin(shop, new Insets(300,0,0,scaleSize*11+multiplayerSideShift));

		window.getChildren().add(shop);
		
		primaryStage.setTitle("Tower Defense");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		gameStart = true;
		
		timeline = new Timeline(new KeyFrame(Duration.millis(50), new GameStarter()));
	    timeline.setCycleCount(Animation.INDEFINITE);
	    paused = true;
	    escPaused = false;
		
	} 
	
	/**
	 * Minimap GUI that client sees in top left hand corner of screen during multiplayer
	 *
	 */
	private class GameStarterMiniMap implements EventHandler<ActionEvent> {
	    int tic = 0;
	    Map miniMap;
	    GraphicsContext miniMapGc;
	    
	    public GameStarterMiniMap(Map miniMap, GraphicsContext miniMapGc) {
	    	this.miniMap = miniMap;
	    	this.miniMapGc = miniMapGc;
	    }
	    
	    @Override
	    public void handle(ActionEvent event) {
	      tic++;
	      if(miniMap.getClass() == LevelOne.class)
				miniMap.draw(miniMapGc, map1Image);
			else if(miniMap.getClass() == LevelTwo.class)
				miniMap.draw(miniMapGc, map2Image);
			else
				miniMap.draw(miniMapGc, map3Image);
	    
	    	miniMapController.singleTic(); //single tic of game
	    	if(tic > lastTic)
	    		lastTic = tic;
	    	updateGUI();
	    	
	    	if (miniMapController.playerDead()) {
	    		if(window.getChildren().contains(gameOverScreen))
	    			window.getChildren().remove(gameOverScreen);
	    		gameOverScreen = new gameOverScreen();
	    		gameOverScreen.setMaxSize(400, 400);
	    		window.getChildren().add(gameOverScreen);
	    		miniMapTimeline.stop();
	    		timeline.stop();
	    		return;
	    	}
	    }
	    

	    private void updateGUI() {
	    	int playerHealth;
	    	if(multiplayer) {
	    		playerHealth = Math.min(player.getHealth(), miniMapPlayer.getHealth());
	    		miniMapPlayer.takeDamage(miniMapPlayer.getHealth()-playerHealth);
	    		player.takeDamage(player.getHealth()-playerHealth);
	    	}
	    	else
	    		playerHealth = player.getHealth();
	    	healthLabel.setText("Health: " + player.getHealth());
	    	window.getChildren().remove(healthBar);
	    	healthBar = new Line(0, 0, player.getHealth(), 0);
	    	healthBar.setStrokeWidth(20);
	    	if(player.getHealth() > 76)
	    		healthBar.setStroke(Color.LIME);
	    	else if (player.getHealth() > 26)
	    		healthBar.setStroke(Color.GOLD);
	    	else
	    		healthBar.setStroke(Color.RED);
	    	window.setAlignment(healthBar, Pos.TOP_LEFT);
			window.setMargin(healthBar, new Insets(200, 0, 0, scaleSize*11 + 40+multiplayerSideShift));
	    	window.getChildren().add(healthBar);
	    }
		    

	  }
	
	/**
	 * has the game end if the other player has dc'ed
	 */
	public void endGame() {
		Platform.runLater(new endGameRunable());
	}

	/**
	 * Updates the gui with a check or an X if the other player is ready
	 */
	private class endGameRunable extends Task<Object> {

		@Override
		public void run() {
			controller.setGameOver();
		}

		@Override
		protected Object call() throws Exception {
			return null;
		}
	}//end of endGameRunable

	/**
	 * takes the list from the server and changes the enemy list
	 * @param list the list we insert
	 */
	public void setNewEnemiesList(ArrayList<Enemy> list) {
		Platform.runLater(new setNewEnemiesList(list));
	}
	/**
	 * changes the miniMap controller Enemies list
	 */
	private class setNewEnemiesList implements Runnable{
		private ArrayList<Enemy> list;
		public setNewEnemiesList(ArrayList<Enemy> list) {
			for(Enemy enemy : list) {
				if(enemy instanceof BossMonster)
					System.out.println("boss x = " + enemy.getX());
				enemy.changeScale(multiplayerSideShift/11);}
			this.list = list;
		}
		@Override
		public void run() {
			miniMapController.setEnemies(list);
			miniMapController.drawObjects();
			
		}
	}
	
	/**
	 * takes the list from the server and changes the tower list
	 * @param list the list we insert
	 */
	public void setNewTowersList(ArrayList<Tower> list) {
		Platform.runLater(new setNewTowersList(list));
	}
	/**
	 * changes the miniMap controller tower list
	 */
	private class setNewTowersList implements Runnable{
		private ArrayList<Tower> list;
		
		public setNewTowersList(ArrayList<Tower> list) {
			for(Tower tower : list)
				tower.changeScale(multiplayerSideShift/11);
			this.list = list;
		}
		@Override
		public void run() {
			miniMapController.setTowers(list);
			miniMapController.drawObjects();
			miniMapTimeline.setRate(1.0);
			miniMapTimeline.play();
		}
	}
	
	
	/**
	 * takes the speed from the server and changes the minimap speed
	 * @param speed
	 */
	public void changeMiniMapSpeed(int speed) {
		Platform.runLater(new changeMiniMapSpeed(speed));
	}
	
	/**
	 * changes the game speed of the minimap
	 */
	private class changeMiniMapSpeed implements Runnable{
		private int speed;
		public changeMiniMapSpeed(int speed) {
			this.speed = speed;
		}
		@Override
		public void run() {
			if(speed == 0)
				miniMapTimeline.pause();
			else if(speed == 1) {
				miniMapTimeline.setRate(1.0);
				miniMapTimeline.play();	
			}
			else {
				miniMapTimeline.setRate(2.0);
				miniMapTimeline.play();	
			}
		}
	}

	/**
	 * updates the player's money and redraws the shop in case your ability to afford a tower changes 
	 */
	public void updateMoneyGUI() {
		moneyLabel.setText("" + player.getMoneys());
		shop.redrawShopImages();
	}
	
	/**
	 * takes the list from the server and changes the tower list
	 * @param won
	 * @param player2 - replaces miniMapPlayer 
	 * @param towersBought - towers bought by player 2
	 * @param enemiesKilled - amount of enemies killed by player 2
	 */
	public void setPlayerTwoGameover(boolean won, Player player2, int enemiesKilled, int towersBought) {
		Platform.runLater(new setPlayerTwoGameover(won, player2, enemiesKilled, towersBought));
	}
	/**
	 * changes the miniMap controller tower list
	 */
	private class setPlayerTwoGameover implements Runnable{
		private boolean won;
		private Player player2;
		private int enemiesKilled, towersBought;
		
		public setPlayerTwoGameover(boolean won, Player player2, int enemiesKilled, int towersBought) {
			this.won = won;
			this.player2 = player2;
			this.enemiesKilled = enemiesKilled;
			this.towersBought = towersBought;
		}
		@Override
		public void run() {
			playerTwoGameover = true;
			playerTwoWon = true;
			miniMapTimeline.stop();
			miniMapPlayer = player2;
			playerTwoEnemyKills = enemiesKilled;
			playerTwoTowersBought = towersBought;
			if(controller.gameOver()) {
	    		window.getChildren().remove(gameOverScreen);
	    		gameOverScreen = new gameOverScreen();
				gameOverScreen.setMaxSize(400, 400);
	    		window.getChildren().add(gameOverScreen);
	    		timeline.stop();
	    		return;
	    	}
			miniMapTimeline.stop();
		}
	}
}
