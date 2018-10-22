package org.us._42.laphicet.gomoku.visualizer;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.us._42.laphicet.gomoku.GameController;
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
import java.io.InputStream;
import java.nio.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

public class Visualizer implements PlayerController, GameStateReporter {
	public static final byte BOARD_SIZE = 19;
	public static final byte BOARD_SPACE = 50;
	public static final int BOARD_WIDTH = (BOARD_SIZE + 1) * BOARD_SPACE;
	
	private List<Integer[]> pieces = new ArrayList<Integer[]>();
	private byte current_player = 1;
	private int[] textures = new int[2];
	
	private DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
	private DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
	
	private GLFWKeyCallback keyCallback;
	private long window;
	private boolean mousePressed = false;
	
	/**
	 * Initializes the visualizer window.
	 */
	private void init() {
		if (!glfwInit()) {
			throw new RuntimeException("Unable to initialize visualizer");
		}
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); 
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		
		this.window = glfwCreateWindow(BOARD_WIDTH, BOARD_WIDTH, "Gomoku", NULL, NULL);
		if (this.window == NULL) {
			throw new RuntimeException("Failed to create the visualizer window");
		}
		
		this.keyCallback = new GomokuKeyCallBack();
		glfwSetKeyCallback(window, this.keyCallback);
	}
	
	/**
	 * Displays the visualizer window.
	 */
	private void displayWindow() {
		glfwMakeContextCurrent(this.window);
		glfwSwapInterval(1);
		glfwShowWindow(this.window);
	}
	
	/**
	 * Scans for key changes on the GL window.
	 * 
	 * @param coords The output buffer for game-board coordinates.
	 */
    private void scan(int[] coords){
    	if (GomokuKeyCallBack.isKeyDown(GLFW_KEY_ESCAPE)) {
    		glfwSetWindowShouldClose(this.window, true);
    	}
    	if (!this.mousePressed && glfwGetMouseButton(this.window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
    		glfwGetCursorPos(this.window, this.mouseX, this.mouseY);
    		coords[0] = (int)Math.round(this.mouseX.get(0)/BOARD_SPACE) - 1;
    		coords[1] = (int)Math.round(this.mouseY.get(0)/BOARD_SPACE) - 1;
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
    	glBegin(GL_LINES);
    	{
    		glVertex2f(x1, y1);
    		glVertex2f(x2, y2);
    	}
    	glEnd();
    }
    
    /**
     * Renders a blank board on the visualizer window.
     */
    private void renderBoard() {
    	for (float i = 1; i <= BOARD_SIZE; i++) {
    		drawLine(BOARD_SPACE, i * BOARD_SPACE, BOARD_SIZE * BOARD_SPACE, i * BOARD_SPACE);
    		drawLine(i * BOARD_SPACE, BOARD_SPACE, i * BOARD_SPACE, BOARD_SIZE * BOARD_SPACE);
    	}
    }
    
    /**
     * Draws a piece on the visualizer.
     * 
     * @param texture The texture id to draw.
     * @param x The absolute x coordinate on the visualizer window.
     * @param y The absolute y coordinate on the visualizer window.
     */
    private void placePiece(int texture, int x, int y) {
    	glBindTexture(GL_TEXTURE_2D, texture);
    	
    	glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
    	glEnable(GL_TEXTURE_2D); 
    	glBegin(GL_QUADS);
    	{
    		glTexCoord2f(0, 1);
    		glVertex2i(x - (128/5),y - (128/5));
    		glTexCoord2f(1, 1);
    		glVertex2i(x + (128/5), y - (128/5));
    		glTexCoord2f(1, 0);
    		glVertex2i(x + (128/5), y + (128/5));
    		glTexCoord2f(0, 0);
    		glVertex2i(x - (128/5), y + (128/5));
    	}
    	glEnd();
    	glDisable(GL_TEXTURE_2D);
    	glPopMatrix();
    }
    
    /**
     * Initializes a texture from an image.
     * 
     * @param path The internal resource path for the image.
     * @return The GL texture id
     * @throws IOException If there was an error attempting to read the image.
     */
    private int initTexture(String path) throws IOException {
    	InputStream stream = Visualizer.class.getResourceAsStream(path);
    	BufferedImage image = ImageIO.read(stream);
    	
    	int width = image.getWidth();
    	int height = image.getHeight();
    	int pixels[] = new int[width * height];
    	image.getRGB(0, 0, width, height, pixels, 0, width);
    	
    	ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
    	for(int y = 0; y < height; ++y) {
    	    for(int x = 0; x < width; ++x) {
    	        int pixel = pixels[x + y * width];
    	        buffer.put((byte) ((pixel >> 16) & 0xFF));
    	        buffer.put((byte) ((pixel >> 8) & 0xFF));
    	        buffer.put((byte) (pixel & 0xFF));
    	        buffer.put((byte) ((pixel >> 24) & 0xFF));
    	    }
    	}
    	buffer.flip();
    	
    	int texture = glGenTextures(); 
        glBindTexture(GL_TEXTURE_2D, texture); 
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    	return texture;
    }
    
    /**
     * Renders the currently placed pieces onto the visualizer.
     */
    private void renderPieces() {
    	for (Integer[] piece : this.pieces) {
    		placePiece(this.textures[piece[2]], piece[0] * BOARD_SPACE, BOARD_WIDTH - (piece[1] * BOARD_SPACE));
    	}
    }
    
    /**
     * Draws the initial visualizer state and initializes token textures.
     */
	private void beginVisualize() {
		GL.createCapabilities();
		
		glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, BOARD_WIDTH, 0, BOARD_WIDTH, -1, 1);
        glMatrixMode(GL_MODELVIEW);
		
		try {
			this.textures[0] = initTexture("victini.png");
			this.textures[1] = initTexture("claydol.png");
		}
		catch (IOException e) {
			glfwSetWindowShouldClose(this.window, true);
			e.printStackTrace();
		}
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		renderBoard();
        glfwSwapBuffers(this.window);
	}
	
	/**
	 * Initialize and display the visualizer window and its initial states.
	 */
	public void start() {
		init();
		displayWindow();
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
	
	@Override
	public void reportTurn(GameController game, int x, int y, byte value, Collection<String> reports) {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		renderBoard();
		renderPieces();
        glfwSwapBuffers(this.window);
	}
	
	@Override
	public String name() {
		if (this.current_player == 1) {
			return "Victini";
		}
		else {
			return "Claydol";
		}
	}
	
	@Override
	public void report(String message) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void informMove(int x, int y, byte value) {
		if (value != 0) {
			this.pieces.add(new Integer[] {x + 1, y + 1, value - 1});
		}
		else {
			//Remove piece matching coordinates from this.pieces
		}
	}
	
	@Override
	public void getMove(GameController game, byte piece, int[] coords) {
		this.current_player = piece;
		while (coords[0] == -1 && !glfwWindowShouldClose(this.window)) {
			glfwPollEvents();
			scan(coords);		
		}
		if (glfwWindowShouldClose(this.window)) {
			game.abort();
		}
	}
}
