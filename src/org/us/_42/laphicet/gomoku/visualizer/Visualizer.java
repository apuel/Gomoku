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
	private int[] textures = new int[2];
	private int backgroundTexture;
	private BufferedImage[] images = new BufferedImage[3];
	
	private DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
	private DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
	
	private TextUtil textutil = new TextUtil();
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
		
		textutil.init("./img/font.png", 32, 3);

		// will be replaced once player selection option is available
		playerNames[0] = "Victini";
		playerNames[1] = "Claydol";
	}
	
	/**
	 * Displays the visualizer window.
	 */
	private void displayWindow(long window) {
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
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
    		Tools.renderTexture(this.textures[piece.player], piece.x * BOARD_SPACE, BOARD_WIDTH - (piece.y * BOARD_SPACE) + PIECE_OFFSET, TEXTURE_OFFSET, TEXTURE_OFFSET);
    	}
    }
    
    /**
     * Draws the initial visualizer state and initializes token textures.
     */
	private void beginVisualize() {
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		
		textutil.initAlphabet();
		
		glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, BOARD_WIDTH, 0, BOARD_WIDTH + BOARD_OFFSET, -1, 1);
        glMatrixMode(GL_MODELVIEW); 
        
		this.textures[0] = Tools.initTexture(this.images[0], 0, 0, this.images[0].getWidth(), this.images[0].getHeight(), false);
		this.textures[1] = Tools.initTexture(this.images[1], 0, 0, this.images[1].getWidth(), this.images[1].getHeight(), false);
		this.backgroundTexture = Tools.initTexture(this.images[2], 0, 0, this.images[2].getWidth(), this.images[2].getHeight(), false);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Tools.renderTexture(this.backgroundTexture, BG_OFFSET_X + BOARD_SPACE, BG_OFFSET_Y + BOARD_SPACE, BG_OFFSET_X, BG_OFFSET_Y);
		renderBoard();
        glfwSwapBuffers(this.window);
	}
	
	/**
	 * Initialize and display the visualizer window and its initial states.
	 */
	public void start() {
		init();
		try {
			this.images[0] = Tools.getBufferedImage("./img/victini.png");
			this.images[1] = Tools.getBufferedImage("./img/claydol.png");
			this.images[2] = Tools.getBufferedImage("./img/field.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		displayWindow(this.window);
		displayWindow(this.console);
		beginVisualize();
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
				msg = report.get(report.size() - 1 - i);
				textutil.drawString(msg.getValue(), x, y, 1, msg.getKey());
				y = y + 14;
			}
		}
	}
	
	private void updateBoard() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Tools.renderTexture(this.backgroundTexture, BG_OFFSET_X + BOARD_SPACE, BG_OFFSET_Y + BOARD_SPACE, BG_OFFSET_X, BG_OFFSET_Y);
		this.renderBoard();
		this.renderPieces();
		this.renderReports();
		this.renderStats();
//		glfwMakeContextCurrent(console);
//		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//		textutil.drawString("Testing", 150, 150, 4, new Float[]{1.0f, 0.0f, 0.0f});
//		glfwMakeContextCurrent(window);
        glfwSwapBuffers(this.window);
        glfwSwapBuffers(this.console);
	}
	
	private void renderStats() {
		textutil.drawString("TURN", 440, 1220, 3, new Float[]{0.0f, 0.0f, 0.0f});
		textutil.drawString(playerNames[0], 65, 1220, 2, new Float[]{0.0f, 0.0f, 0.0f});
		textutil.drawString(playerNames[1], 900 - (playerNames[1].length() * 10), 1220, 2, new Float[]{0.0f, 0.0f, 0.0f});
	}
	
	@Override
	public void logTurn(Gomoku game, int x, int y, byte value, Collection<String> logs) {
		for (String log : logs) {
			this.report.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 1.0f, 1.0f}, log));
		}
		this.updateBoard();
	}
	
	@Override
	public String name(byte value) {
		if (value == 1) {
			return "Victini";
		}
		else {
			return "Claydol";
		}
	}
	
	@Override
	public void report(String message) {
		// TODO Auto-generated method stub
		this.report.add(new SimpleEntry<Float[],String>(new Float[]{ 1.0f, 0.0f, 0.0f}, message));
		this.updateBoard();
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
