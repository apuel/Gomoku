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
	private List<Integer[]> pieces = new ArrayList<Integer[]>();
	private byte current_player = 1;
	private int[] textures = new int[2];

	public static final byte BOARD_SIZE = 19;
	public static final byte BOARD_SPACE = 50;
	
	private DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
	private DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
	
	private GLFWKeyCallback keyCallback;
	private long window;
	private boolean mousePressed = false;

	public void start() {
		init();
		createWindow();
		createCallbacks();
		displayWindow();
		beginVisualize();
	}

	public void end() {
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		glfwTerminate();
	}

	private void init() {
		if ( !glfwInit() )
			throw new RuntimeException("Unable to initialize visualizer");
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); 
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
	}
	
	private void createWindow() {
		window = glfwCreateWindow((BOARD_SIZE + 1) * BOARD_SPACE,
				(BOARD_SIZE + 1) * BOARD_SPACE, "Gomoku", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the visualizer window");
	}
	
	private void createCallbacks() {
		glfwSetKeyCallback(window, keyCallback = new GomokuKeyCallBack());
	}

	private void displayWindow() {
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}
	
    private void scan(int[] coords){
    	if (GomokuKeyCallBack.isKeyDown(GLFW_KEY_ESCAPE))
    		glfwSetWindowShouldClose(window, true);
    	if (!mousePressed && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
    		glfwGetCursorPos(window, mouseX, mouseY);
    		coords[0] = (int)Math.round(mouseX.get(0)/BOARD_SPACE) - 1;
    		coords[1] = (int)Math.round(mouseY.get(0)/BOARD_SPACE) - 1;
    		mousePressed = true;
    	}
    	if (mousePressed && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE) {
    		mousePressed = false;
    	}
    }
    
    private void drawLine(float x1, float y1, float x2, float y2) {
    	glBegin(GL_LINES);
//    		glColor3f(0, 1, 0);
    		glVertex2f(x1, y1);
    		glVertex2f(x2, y2);
    	glEnd();   
    }
    
    private void initBoard() {
    	glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, (BOARD_SIZE + 1) * BOARD_SPACE, 0, (BOARD_SIZE + 1) * BOARD_SPACE, -1, 1);
        glMatrixMode(GL_MODELVIEW);
    }
    
    private void renderBoard() {
    	for (float i = 1; i <= BOARD_SIZE; i++) {
    		drawLine(BOARD_SPACE, i * BOARD_SPACE, BOARD_SIZE * BOARD_SPACE, i * BOARD_SPACE);
    		drawLine(i * BOARD_SPACE, BOARD_SPACE, i * BOARD_SPACE, BOARD_SIZE * BOARD_SPACE);
    	}
    }
    
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
    
    private void renderPieces() {
    	for (Integer[] piece : this.pieces) {
    		placePiece(this.textures[piece[2]], piece[0] * BOARD_SPACE, ((int)(BOARD_SIZE + 1) * BOARD_SPACE) - (piece[1] * BOARD_SPACE));
    	}
    }

	private void beginVisualize() {
		GL.createCapabilities();
		initBoard();
		
		try {
			this.textures[0] = initTexture("victini.png");
			this.textures[1] = initTexture("claydol.png");
		} catch (IOException e1) {
			glfwSetWindowShouldClose(window, true);
			e1.printStackTrace();
		}
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		renderBoard();
        glfwSwapBuffers(window);
	}

	@Override
	public void reportTurn(GameController game, int x, int y, byte piece, Collection<String> reports) {
		this.pieces.add(new Integer[] {x + 1, y + 1, piece - 1});
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		renderBoard();
		renderPieces();
        glfwSwapBuffers(window);
	}

	@Override
	public String name() {
		if (current_player == 1)
			return "Victini";
		else
			return "Claydol";
	}

	@Override
	public void report(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void informTurn(int x, int y, byte value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getMove(GameController game, byte piece, int[] coords) {
		current_player = piece;
		while (coords[0] == -1 && !glfwWindowShouldClose(window)) {
			glfwPollEvents();
			scan(coords);		
		}
		if (glfwWindowShouldClose(window)) {
			game.abort();
		}
	}
}
