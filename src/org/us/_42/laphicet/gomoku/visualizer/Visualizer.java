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
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
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
	
	private Set<Piece> pieces = new HashSet<Piece>();
	private List<Entry<Float[],String>> report = new ArrayList<Entry<Float[],String>>();
	private List<Entry<Float[],String>> debug = new ArrayList<Entry<Float[],String>>();
	private int[] textures = new int[8];
	private int backgroundTexture;
	private BufferedImage bgBuffer = null;
	private BufferedImage[] images = new BufferedImage[8];
	private int[] playerPiece = new int[2];
	
	private int currentPlayerPickingChar = 0;
	private boolean[] availableChar = new boolean[]{false, false, false, false, false, false, false, false};
	private static final String[] pieceName = new String[] {"Victini", "Claydol", "Slowpoke", "Cyndaquil", "Flareon", "Porygon2", "Paras", "Charmander"};
	
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
		
//		textutil.init("./img/font.png", 32, 3);
		
		try {
			this.textutil = new TextUtil("./img/font.png", 32, 3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Displays the visualizer window.
	 */
	private void displayWindow(long window) {
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}
	
	private void pickChar(double x, double y) {
		for (int i = 0, x1 = 200, x2 = 240; i < 8; i++, x1+= 80, x2 += 80) {
			if (x >= x1 && x <= x2 && y >= 630 && y < 670 && !availableChar[i]) {
				this.playerPiece[currentPlayerPickingChar] = this.textures[i];
				this.playerNames[currentPlayerPickingChar] = this.pieceName[i];
				availableChar[i] = true;
				currentPlayerPickingChar++;
				System.out.println("Selected " + this.pieceName[i]); 
			}
		}
	}
	/**
	 * Scans for key changes on the GL window.
	 * 
	 * @param coords The output buffer for game-board coordinates.
	 */
    private void scan(int[] coords){
    	if (KeyCallBack.isKeyDown(GLFW_KEY_ESCAPE)) {
    		glfwSetWindowShouldClose(this.window, true);
    	}
    	if (currentPlayerPickingChar > 1) {
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
    	else {
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
    
    /**
     * Draws a line from one pair of coordinates to another.
     * 
     * @param x1 The x coordinate for the first pair.
     * @param y1 The y coordinate for the first pair.
     * @param x2 The x coordinate for the second pair.
     * @param y2 The y coordinate for the second pair.
     */
    private void drawLine(float x1, float y1, float x2, float y2) {
    	glColor3f(0.0f, 0.0f, 0.0f);
    	glBegin(GL_LINES);
    	{
    		for (float i = -1.337f; i < 1.337f; i += 0.01f) {
    			glVertex2f(x1 + i, y1 + i);
    			glVertex2f(x2 + i, y2 + i);
    		}
    	}
    	glEnd();
    	glColor3f(1.0f, 1.0f, 1.0f);
    }
    
    /**
     * Renders a blank board on the visualizer window.
     */
    private void renderBoard() {
    	drawLine(BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET, BOARD_WIDTH - BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET);
		drawLine(BOARD_SPACE, BOARD_WIDTH + STATBOX_OFFSET , BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET);
    	drawLine(BOARD_SPACE, BOARD_WIDTH + STATBOX_OFFSET , BOARD_WIDTH - BOARD_SPACE, BOARD_WIDTH + STATBOX_OFFSET );
		drawLine(BOARD_WIDTH - BOARD_SPACE, BOARD_WIDTH + STATBOX_OFFSET , BOARD_WIDTH - BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET);
		
    	for (float i = 1; i <= BOARD_SIZE; i++) {
    		drawLine(BOARD_SPACE, (i * BOARD_SPACE) + PIECE_OFFSET, BOARD_SIZE * BOARD_SPACE, (i * BOARD_SPACE) + PIECE_OFFSET);
    		drawLine(i * BOARD_SPACE, BOARD_SPACE + PIECE_OFFSET, i * BOARD_SPACE, (BOARD_SIZE * BOARD_SPACE) + PIECE_OFFSET);
    	}
    	
    	drawLine(BOARD_SPACE, MIDDLE_OFFSET, BOARD_WIDTH - BOARD_SPACE, MIDDLE_OFFSET);
		drawLine(BOARD_SPACE, REPORTBOX_OFFSET , BOARD_SPACE, BOARD_WIDTH + MIDDLE_OFFSET);
    	drawLine(BOARD_SPACE, REPORTBOX_OFFSET , BOARD_WIDTH - BOARD_SPACE, REPORTBOX_OFFSET );
		drawLine(BOARD_WIDTH - BOARD_SPACE, REPORTBOX_OFFSET , BOARD_WIDTH - BOARD_SPACE, MIDDLE_OFFSET);
    }
    
    /**
     * Renders the currently placed pieces onto the visualizer.
     */
    private void renderPieces() {
    	for (Piece piece : this.pieces) {
    		Tools.renderTexture(this.playerPiece[piece.player], piece.x * BOARD_SPACE, BOARD_WIDTH - (piece.y * BOARD_SPACE) + PIECE_OFFSET, TEXTURE_OFFSET, TEXTURE_OFFSET);
    	}
    }
    
    private void setupGL(long screen, int x, int y) {
    	glfwMakeContextCurrent(screen);
    	GL.createCapabilities();
		glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, x, 0, y, -1, 1);
        glMatrixMode(GL_MODELVIEW); 
        textutil.initAlphabet();
    }
    
    /**
     * Draws the initial visualizer state and initializes token textures.
     */
	private void loadCharacters() {
		for (int i = 0; i < 8; i++) {
			this.textures[i] = Tools.initTexture(this.images[i], 0, 0, this.images[i].getWidth(), this.images[i].getHeight(), false);
		}
		this.backgroundTexture = Tools.initTexture(this.bgBuffer, 0, 0, this.bgBuffer.getWidth(), this.bgBuffer.getHeight(), false);
	}
	
	private void charSelect() {
		while (currentPlayerPickingChar < 2) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			if (currentPlayerPickingChar == 0) {
				this.textutil.drawString("Select Player 1", BOARD_WIDTH / 2 - 180, BOARD_WIDTH - 150, 3, new Float[]{ 0.0f, 1.0f, 0.0f});
				for (int i = 0, x = 220, y = 650; i < 8; i++, x+= 80) {
					Tools.renderTexture(this.textures[i], x, y, TEXTURE_OFFSET, TEXTURE_OFFSET);
				}
			}
			else {
				this.textutil.drawString("Select Player 2", BOARD_WIDTH / 2 - 180, BOARD_WIDTH - 150, 3, new Float[]{ 0.0f, 1.0f, 0.0f});
				for (int i = 0, x = 220, y = 650; i < 8; i++, x+= 80) {
					Tools.renderTexture(this.textures[i], x, y, TEXTURE_OFFSET, TEXTURE_OFFSET);
				}
			}
        	glfwSwapBuffers(this.window);
        	glfwPollEvents();
			scan(new int[]{});
		}
	}
	/**
	 * Initialize and display the visualizer window and its initial states.
	 */
	public void start() {
		this.init();
		try {
			this.images[0] = Tools.getBufferedImage("./img/victini.png");
			this.images[1] = Tools.getBufferedImage("./img/claydol.png");
			this.images[2] = Tools.getBufferedImage("./img/slowpoke.png");
			this.images[3] = Tools.getBufferedImage("./img/cyndaquil.png");
			this.images[4] = Tools.getBufferedImage("./img/flareon.png");
			this.images[5] = Tools.getBufferedImage("./img/porygon2.png");
			this.images[6] = Tools.getBufferedImage("./img/paras.png");
			this.images[7] = Tools.getBufferedImage("./img/charmander.png");
			this.bgBuffer = Tools.getBufferedImage("./img/field.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Test");
		this.displayWindow(this.window);
		this.displayWindow(this.console);
		this.setupGL(console, BOARD_WIDTH, BOARD_WIDTH);
		this.setupGL(window, BOARD_WIDTH, BOARD_WIDTH + BOARD_OFFSET);
		
		this.loadCharacters();
		this.charSelect();
		
//		beginVisualize();
		updateBoard();
	}
	
	/**
	 * Destroy the visualizer window and free respective objects.
	 */
	public void end() {
		glfwFreeCallbacks(this.window);
		glfwDestroyWindow(this.window);
		glfwTerminate();
	}
	
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
	
	private void updateConsole() {
		glfwMakeContextCurrent(console);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		this.renderDebug();
        glfwSwapBuffers(this.console);
	}
	
	private void updateBoard() {
		glfwMakeContextCurrent(window);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Tools.renderTexture(this.backgroundTexture, BG_OFFSET_X + BOARD_SPACE, BG_OFFSET_Y + BOARD_SPACE, BG_OFFSET_X, BG_OFFSET_Y);
		this.renderBoard();
		this.renderPieces();
		this.renderReports();
		this.renderStats();
		glfwSwapBuffers(this.window);
	}
	
	private void renderStats() {
		textutil.drawString("TURN", 440, 1220, 3, new Float[]{0.0f, 0.0f, 0.0f});
		textutil.drawString(playerNames[0], 70, 1220, 2, new Float[]{0.0f, 0.0f, 0.0f});
		textutil.drawString(playerNames[1], (int)(960 - (playerNames[1].length() * (textutil.width * 2 / textutil.SCALE))), 1220, 2, new Float[]{0.0f, 0.0f, 0.0f});
	}
	
	@Override
	public void logTurn(Gomoku game, int x, int y, byte value, Collection<String> logs) {
		for (String log : logs) {
			this.report.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 1.0f, 1.0f}, log));
			this.debug.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 1.0f, 1.0f}, log));
		}
		this.updateBoard();
		this.updateConsole();
	}
	
	@Override
	public String name(byte value) {
		if (value == 1) {
			return (playerNames[0]);
		}
		else {
			return (playerNames[1]);
		}
	}
	
	@Override
	public void report(String message) {
		// TODO Auto-generated method stub
		this.report.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 0.0f, 0.0f}, message));
		this.debug.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 0.0f, 0.0f}, message));
		this.updateBoard();
		this.updateConsole();
	}
	
	@Override
	public void informMove(int x, int y, byte value) {
		if (value != 0) {
			this.pieces.add(new Piece(x + 1, y + 1, value - 1));
		}
		else {
			this.pieces.remove(new Piece(x + 1, y + 1, 0));
		}
	}
	
	@Override
	public boolean getMove(Gomoku game, byte piece, int[] coords) {
		while (coords[0] == -1 && !glfwWindowShouldClose(this.window)) {
			glfwPollEvents();
			scan(coords);		
		}
		if (glfwWindowShouldClose(this.window)) {
			game.abort();
		}
		return (true);
	}
}
