package org.us._42.laphicet.gomoku.visualizer;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.GameStateReporter;
import org.us._42.laphicet.gomoku.PlayerController;

import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Visualizer - Object that rendered a 2D space for the board game
 * Gomoku, a Go variant
 * 
 * @author mlu & apuel
 */
public class Visualizer implements PlayerController, GameStateReporter {
	private static final int BOARD_OFFSET = 300;
	private static final byte BOARD_SIZE = 19;
	private static final byte BOARD_SPACE = 50;
	private static final int BOARD_WIDTH = (BOARD_SIZE + 1) * BOARD_SPACE;
	private static final int PIECE_OFFSET = 150;
	private static final int STATBOX_OFFSET = 100;
	private static final int MIDDLE_OFFSET = 250;
	private static final int REPORTBOX_OFFSET = 50;
	private static final int TEXTURE_OFFSET = (128 / 5);
	private static final int BG_OFFSET_X = (BOARD_WIDTH - (BOARD_SPACE * 2)) / 2;
	private static final int BG_OFFSET_Y = ((BOARD_WIDTH + BOARD_OFFSET) - (BOARD_SPACE * 2)) / 2;
	private static final int CHAR_COUNT = 8;
	
	private Set<Piece> pieces = new HashSet<Piece>();
	private List<Entry<Float[],String>> report = new ArrayList<Entry<Float[],String>>();
	private List<Entry<Float[],String>> debug = new ArrayList<Entry<Float[],String>>();
	private int[] textures = new int[CHAR_COUNT];
	private int backgroundTexture;
	private BufferedImage bgBuffer = null;
	private BufferedImage[] images = new BufferedImage[CHAR_COUNT];
	private int[] playerPiece = new int[2];
	
	private int currentPlayerPickingChar = 0;
	private boolean[] availableChar = new boolean[8];
	private static final String[] PIECENAME = new String[] {"Victini", "Claydol", "Slowpoke", "Cyndaquil", "Flareon", "Porygon2", "Paras", "Charmander"};
	
	private boolean toggleDebug;
	private boolean debugPressed;
	
	private DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
	private DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
	
	private TextUtil textutil;
	private GLFWKeyCallback keyCallback;
	private long window;
	private long console;
	private boolean mousePressed = false;
	
	private String[] playerNames= new String[2];
	
	/**
	 * Initializes the visualizer window.
	 */
	private void init() {
		if (!glfwInit()) {
			throw new RuntimeException("Unable to initialize visualizer");
		}
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); 
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		this.window = glfwCreateWindow(BOARD_WIDTH, BOARD_WIDTH + BOARD_OFFSET, "Gomoku", NULL, NULL);
		this.console = glfwCreateWindow(BOARD_WIDTH, BOARD_WIDTH, "Debug Console", NULL, NULL);
		if (this.window == NULL || this.console == NULL) {
			throw new RuntimeException("Failed to create the visualizer window");
		}
		
		this.keyCallback = new KeyCallBack();
		glfwSetKeyCallback(window, this.keyCallback);
		
		try {
			this.textutil = new TextUtil("./img/font.png", 32, 3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Setups GL environment 
	 * 
	 * @param screen The window info that is being passed in
	 * @param x The width of the screen
	 * @param y The height of the screen
	 */
    private void setupGL(long screen, int x, int y) {
    	glfwMakeContextCurrent(screen);
    	GL.createCapabilities();
		glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, x, 0, y, -1, 1);
        glMatrixMode(GL_MODELVIEW); 
        textutil.initAlphabet();
    }
   
    //=============================================================================================================
    //
    // Visualizer Render Methods
    //
    //=============================================================================================================
    /**
     * Renders a blank board on the visualizer window.
     */
    private void renderBoard() {
    	Renderer.drawLine(BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET, BOARD_WIDTH - BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET, 1.4f);
    	Renderer.drawLine(BOARD_SPACE, BOARD_WIDTH + STATBOX_OFFSET , BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET, 1.4f);
    	Renderer.drawLine(BOARD_SPACE, BOARD_WIDTH + STATBOX_OFFSET , BOARD_WIDTH - BOARD_SPACE, BOARD_WIDTH + STATBOX_OFFSET, 1.4f);
    	Renderer.drawLine(BOARD_WIDTH - BOARD_SPACE, BOARD_WIDTH + STATBOX_OFFSET , BOARD_WIDTH - BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET, 1.4f);
		
    	for (float i = 1; i <= BOARD_SIZE; i++) {
    		Renderer.drawLine(BOARD_SPACE, (i * BOARD_SPACE) + PIECE_OFFSET, BOARD_SIZE * BOARD_SPACE, (i * BOARD_SPACE) + PIECE_OFFSET, 1.4f);
    		Renderer.drawLine(i * BOARD_SPACE, BOARD_SPACE + PIECE_OFFSET, i * BOARD_SPACE, (BOARD_SIZE * BOARD_SPACE) + PIECE_OFFSET, 1.4f);
    	}
    	
    	Renderer.drawLine(BOARD_SPACE, MIDDLE_OFFSET, BOARD_WIDTH - BOARD_SPACE, MIDDLE_OFFSET, 1.4f);
    	Renderer.drawLine(BOARD_SPACE, REPORTBOX_OFFSET , BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET, 1.4f);
    	Renderer.drawLine(BOARD_SPACE, REPORTBOX_OFFSET , BOARD_WIDTH - BOARD_SPACE, REPORTBOX_OFFSET, 1.4f);
    	Renderer.drawLine(BOARD_WIDTH - BOARD_SPACE, REPORTBOX_OFFSET , BOARD_WIDTH - BOARD_SPACE, MIDDLE_OFFSET, 1.4f);
    }
    
    /**
     * Renders the currently placed pieces onto the visualizer.
     */
    private void renderPieces() {
    	for (Piece piece : this.pieces) {
    		Renderer.renderTexture(this.playerPiece[piece.player], piece.x * BOARD_SPACE, BOARD_WIDTH - (piece.y * BOARD_SPACE) + PIECE_OFFSET, TEXTURE_OFFSET, TEXTURE_OFFSET);
    	}
    }
    
    /**
     * Renders the report onto the visualizer
     */
	private void renderReports() {
		int x = 60;
		int y = 60;
		Entry<Float[],String> msg = null;
		for (int i = 0; i < 10; i++) {
			if (report.size() > i) {
				msg = this.report.get(report.size() - 1 - i);
				this.textutil.drawString(msg.getValue(), x, y, 1, msg.getKey());
				y = y + 14;
			}
		}
	}
	
	/**
	 * Renders info the debug console
	 */
	private void renderDebug() {
		int x = 50;
		int y = 950;
		Entry<Float[],String> msg = null;
		for (int i = 0; i < 65; i++) {
			if (debug.size() > i) {
				msg = this.debug.get(debug.size() - 1 - i);
				this.textutil.drawString(">", x, y, 1, new Float[]{ 0.0f, 1.0f, 0.0f});
				this.textutil.drawString(msg.getValue(), x + 20, y, 1, msg.getKey());
				y = y - 14;
			}
		}
	}
	 
	/**
	 * Renders the stats with info from the game
	 * 
	 * @param turn The current turn count
	 * @param tokenP1 The token placed by player 1
	 * @param tokenP2 The token placed by player 2
	 * @param captureP1 Total captures made by player 1
	 * @param captureP2 Total captures made by player 2
	 */
	private void renderStats(int turn, int tokenP1, int tokenP2, int captureP1, int captureP2) {
		textutil.drawString("TURN", (int)(515 -  (2f * (textutil.width * 3/textutil.SCALE))), 1170, 3, new Float[]{0.0f, 0.0f, 0.0f});
		textutil.drawString(playerNames[0], 70, 1220, 2, new Float[]{0.0f, 0.0f, 0.0f});
		textutil.drawStringBackwards(playerNames[1], 950, 1220, 2, new Float[]{0.0f, 0.0f, 0.0f});
		String gameTurn = Integer.toString(turn);
		textutil.drawString(gameTurn, (int)(515 -  ((gameTurn.length() / 2f) * (textutil.width * 3/textutil.SCALE))), 1130, 3, new Float[]{1.0f, 1.0f, 1.0f});
		textutil.drawString("Tokens Played: " + tokenP1, 70, 1190, 1.5f, new Float[]{1.0f, 1.0f, 1.0f});
		textutil.drawStringBackwards("Tokens Played: " + tokenP2, 950, 1190, 1.5f, new Float[]{1.0f, 1.0f, 1.0f});
		textutil.drawString("Captures Made: " + captureP1, 70, 1160, 1.5f, new Float[]{1.0f, 1.0f, 1.0f});
		textutil.drawStringBackwards("Captures Made: " + captureP2, 950, 1160, 1.5f, new Float[]{1.0f, 1.0f, 1.0f});
		Renderer.renderTexture(this.playerPiece[turn % 2], 500, 1220, TEXTURE_OFFSET + 5, TEXTURE_OFFSET + 5);
	}

    
    //=============================================================================================================
    //
    // Visualizer Character Selection Methods
    //
    //=============================================================================================================
    /**
     * Draws the initial visualizer state and initializes token textures.
     */
	private void loadCharacters() {
		for (int i = 0; i < 8; i++) {
			this.textures[i] = Renderer.initTexture(this.images[i], 0, 0, this.images[i].getWidth(), this.images[i].getHeight(), false);
		}
		this.backgroundTexture = Renderer.initTexture(this.bgBuffer, 0, 0, this.bgBuffer.getWidth(), this.bgBuffer.getHeight(), false);
	}
	
	/**
	 * The loop to allow user to select the character piece they want to use
	 */
	private void charSelect() {
		while (currentPlayerPickingChar < 2) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			if (currentPlayerPickingChar == 0) {
				this.textutil.drawString("Select Player 1", BOARD_WIDTH / 2 - 180, BOARD_WIDTH - 150, 3, new Float[]{ 0.0f, 1.0f, 0.0f});
				for (int i = 0, x = 220, y = 650; i < 8; i++, x+= 80) {
					Renderer.renderTexture(this.textures[i], x, y, TEXTURE_OFFSET, TEXTURE_OFFSET);
				}
			}
			else {
				this.textutil.drawString("Select Player 2", BOARD_WIDTH / 2 - 180, BOARD_WIDTH - 150, 3, new Float[]{ 0.0f, 1.0f, 0.0f});
				for (int i = 0, x = 220, y = 650; i < 8; i++, x+= 80) {
					Renderer.renderTexture(this.textures[i], x, y, TEXTURE_OFFSET, TEXTURE_OFFSET);
				}
			}
        	glfwSwapBuffers(this.window);
        	glfwPollEvents();
			scan(null);
			if (glfwWindowShouldClose(this.window)) {
				System.exit(0);
			}
		}
	}
	
	/**
	 * Allows users to identify what they pick
	 * x and y allows us to identify which piece they picked based off the location
	 * 
	 * @param x The x coord of their selection
	 * @param y The y coord of their selection
	 */
	private void pickChar(double x, double y) {
		for (int i = 0, x1 = 200, x2 = 240; i < 8; i++, x1+= 80, x2 += 80) {
			if (x >= x1 && x <= x2 && y >= 630 && y < 670 && !availableChar[i]) {
				this.playerPiece[currentPlayerPickingChar] = this.textures[i];
				this.playerNames[currentPlayerPickingChar] = PIECENAME[i];
				availableChar[i] = true;
				currentPlayerPickingChar++;
			}
		}
	}
	
	
    //=============================================================================================================
    //
    // Visualizer Runtime Methods
    //
    //=============================================================================================================
	/**
	 * Initialize and display the visualizer window and its initial states.
	 */
	public void start() {
		this.init();
		try {
			this.images[0] = Renderer.getBufferedImage("./img/victini.png");
			this.images[1] = Renderer.getBufferedImage("./img/claydol.png");
			this.images[2] = Renderer.getBufferedImage("./img/slowpoke.png");
			this.images[3] = Renderer.getBufferedImage("./img/cyndaquil.png");
			this.images[4] = Renderer.getBufferedImage("./img/flareon.png");
			this.images[5] = Renderer.getBufferedImage("./img/porygon2.png");
			this.images[6] = Renderer.getBufferedImage("./img/paras.png");
			this.images[7] = Renderer.getBufferedImage("./img/charmander.png");
			this.bgBuffer = Renderer.getBufferedImage("./img/field.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Renderer.displayWindow(this.window);
		this.setupGL(console, BOARD_WIDTH, BOARD_WIDTH);
		this.setupGL(window, BOARD_WIDTH, BOARD_WIDTH + BOARD_OFFSET);
		
		this.loadCharacters();
		this.charSelect();
		
		updateBoard(null);
	}
	
	/**
	 * Destroy the visualizer window and free respective objects.
	 */
	public void end() {
		glfwFreeCallbacks(this.window);
		glfwDestroyWindow(this.window);
		glfwTerminate();
	}
	
	/**
	 * Handles results for the game
	 * 
	 * @param game The game object
	 */
	public void results(Gomoku game) {
		this.updateBoard(game);
		while (!glfwWindowShouldClose(this.window)) {
			glfwPollEvents();
			scan(null);
		}
	}
	
	/**
	 * Updates the debug console and renders it
	 */
	private void updateConsole() {
		glfwMakeContextCurrent(console);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		this.renderDebug();
        glfwSwapBuffers(this.console);
	}
	
	/**
	 * Updates the visualizer and renders it
	 * 
	 * @param game The game object for stat rendering
	 */
	private void updateBoard(Gomoku game) {
		glfwMakeContextCurrent(window);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Renderer.renderTexture(this.backgroundTexture, BG_OFFSET_X + BOARD_SPACE, BG_OFFSET_Y + BOARD_SPACE, BG_OFFSET_X, BG_OFFSET_Y);
		this.renderBoard();
		this.renderPieces();
		this.renderReports();
		if (game != null) {
			this.renderStats(game.getTurn(), game.getTokensPlaced(1), game.getTokensPlaced(2), game.getCaptureCount(1), game.getCaptureCount(2));
		}
		else {
			this.renderStats(0, 0, 0, 0, 0);
		}
		glfwSwapBuffers(this.window);
	}
	
	/**
	 * Scans for key changes on the GL window.
	 * It handles character selection as well as tabulation for debug console
	 * 
	 * @param coords The output buffer for game-board coordinates.
	 */
    private void scan(int[] coords){
    	if (KeyCallBack.isKeyDown(GLFW_KEY_ESCAPE)) {
    		glfwSetWindowShouldClose(this.window, true);
    	}
    	
    	if (!this.debugPressed && KeyCallBack.isKeyDown(GLFW_KEY_TAB)) {
    		if (!this.toggleDebug) {
    			Renderer.displayWindow(this.console);
    			this.updateConsole();
    			glfwMakeContextCurrent(this.window);
    			glfwFocusWindow(this.window);
    			this.toggleDebug = true;
    		}
    		else {
    			glfwHideWindow(this.console);
    			this.toggleDebug = false;
    		}
    		this.debugPressed = true;
    	}
    	else if (debugPressed && !KeyCallBack.isKeyDown(GLFW_KEY_TAB)){
    		this.debugPressed = false;
    	}
    	
    	
    	if (coords != null && currentPlayerPickingChar > 1) {
    		if (!this.mousePressed && glfwGetMouseButton(this.window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
    			glfwGetCursorPos(this.window, this.mouseX, this.mouseY);
    			coords[0] = (int)Math.round(this.mouseX.get(0)/BOARD_SPACE) - 1;
    			coords[1] = (int)Math.round(this.mouseY.get(0)/BOARD_SPACE) - (1 + (PIECE_OFFSET / BOARD_SPACE));
    			this.mousePressed = true;
    		}
    		if (this.mousePressed && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE) {
    			this.mousePressed = false;
    		}
    	}
    	else if (currentPlayerPickingChar < 2) {
    		if (!this.mousePressed && glfwGetMouseButton(this.window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
    			glfwGetCursorPos(this.window, this.mouseX, this.mouseY);
    			this.pickChar(this.mouseX.get(0), this.mouseY.get(0));
    			this.mousePressed = true;
    		}
    		if (this.mousePressed && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE) {
    			this.mousePressed = false;
    		}
    	}
    }
	
    //=============================================================================================================
    //
    // Visualizer Method Overloads
    //
    //=============================================================================================================
	@Override
	public void logTurn(Gomoku game, Collection<String> logs) {
		for (String log : logs) {
			this.report.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 1.0f, 1.0f}, log));
			this.debug.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 1.0f, 1.0f}, log));
		}
		if (toggleDebug) {
			this.updateConsole();
		}
		this.updateBoard(game);
	}
	
	@Override
	public void reportChange(Gomoku game, int x, int y, int value) {
		if (value != 0) {
			this.pieces.add(new Piece(x + 1, y + 1, value - 1));
		}
		else {
			this.pieces.remove(new Piece(x + 1, y + 1, 0));
		}
	}
	
	@Override
	public String name(Gomoku game, int value) {
		if (value == 1) {
			return (playerNames[0]);
		}
		else {
			return (playerNames[1]);
		}
	}
	
	@Override
	public void report(Gomoku game, String message) {
		this.report.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 0.0f, 0.0f}, message));
		this.debug.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 0.0f, 0.0f}, message));
		if (toggleDebug) {
			this.updateConsole();
		}
		this.updateBoard(game);
	}
	
	@Override
	public void informChange(Gomoku game, int x, int y, int value) { }
	
	@Override
	public void informWinner(Gomoku game, int value) { }
	
	@Override
	public boolean getMove(Gomoku game, int piece, long key) {
		int[] coords = new int[] {-1, -1};
		while (coords[0] == -1 && !glfwWindowShouldClose(this.window)) {
			glfwPollEvents();
			scan(coords);
		}
		if (glfwWindowShouldClose(this.window)) {
			game.abort();
			return (false);
		}
		game.submitMove(coords[0], coords[1], key);
		return (true);
	}
	
	@Override
	public void gameStart(Gomoku game, int value) { }
	
	@Override
	public void gameEnd(Gomoku game) { }
}
