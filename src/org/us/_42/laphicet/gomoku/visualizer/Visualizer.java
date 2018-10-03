package org.us._42.laphicet.gomoku.visualizer;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;
 

import java.nio.*;
import java.util.PrimitiveIterator.OfDouble;

public class Visualizer {
	
	/**
	 * Generating variables that can be modified to change board setup
	 * 
	 * @BOARD_SIZE is the NxN size matrix for the board for gomoku
	 * @BOARD_SPACE is the spacing between each point for the visualizer
	 */
	public static final byte BOARD_SIZE = 19;
	public static final byte BOARD_SPACE = 50;
	
	/**
	 * These values are for mouse interactions and used for the sole
	 * purpose of getting mouse position and input. These values
	 * are DoubleBuffer because the function that uses these
	 * @glfwGetCursorPos() takes in DoubleBuffers
	 */
	private DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
	private DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
	
	/**
	 * The window handler for the visualizer. This also creates the GLFW callbacks
	 * which will later be used with the Gomoku variant extensions
	 */
	private GLFWKeyCallback keyCallback;
	private long window;
	private boolean mousePressed = false;

	/**
	 * This is the start of the visualizer, it will first call:
	 * 
	 * @init() in order to setup glfw and the visualizer
	 * @createWindow() to create the windows
	 * @createCallbacks() to grab user inputs
	 * @displayWindow() to display the window for user to see
	 * @visualizeLoop() to loop the visualizer
	 * 
	 * If the loop ever exits we need to free all callbacks and
	 * destroy the window and properly terminate glfw, finally
	 * will guarantee this always happens
	 */
	public void start() {
		try {
			init();
			createWindow();
			createCallbacks();
			displayWindow();
			visualizeLoop();
		} 
		finally {
			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);
			glfwTerminate();
		}
	}

	/**
	 * This will init GLFW as well as set properties for resizing and 
	 * appearance state. It will also catch for any issue with loading GLFW
	 * 
	 * @GLFW_VISIBLE with @GLFW_FALSE sets window to hidden so we can 
	 * draw stuff onto it before display
	 * 
	 * @GLFW_RESIZABLE with @GLFW_TRUE allows window to be resized by user
	 */
	private void init() {
		if ( !glfwInit() )
			throw new RuntimeException("Unable to initialize visualizer");
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); 
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
	}
	
	/**
	 * This will create a GLFW window to the specific size
	 * @BOARD_SIZE + 1 multiplied by @BOARD_SPACE, exception will be thrown
	 * if something goes wrong
	 */
	private void createWindow() {
		window = glfwCreateWindow((BOARD_SIZE + 1) * BOARD_SPACE,
				(BOARD_SIZE + 1) * BOARD_SPACE, "Gomoku", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the visualizer window");
	}
	
	/**
	 * This function setups callbacks for keyboard and mouse.
	 * 
	 * It uses @GomokuKeyCallBack.java to catch keyboard responses
	 * It uses @GomokuCursorPosCallback.java to return back cursor position
	 * 
	 * Both are extensions from the GLFW variants
	 */
	private void createCallbacks() {
		glfwSetKeyCallback(window, keyCallback = new GomokuKeyCallBack());
	}

	/**
	 * This will make the window display properly.
	 * glfwSwapInterval enables v-sync
	 */
	private void displayWindow() {
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}
	
	/**
	 * This scans for user inputs. It uses the extended @GomokuKeyCallBack.isKeyDown() 
	 * to see what key is pressed and if it matches the key macro passed in.
	 * 
	 * @glfwGetMouseButton() will check to see if the mouse button is pressed 
	 * depending on which mouse button we want to know and compare to macro value.
	 * 
	 * @glfwGetCursorPos() will return us the position of the cursor when the mouse
	 * is pressed. We use @.get(0) for the mouse coords to get the actual value from
	 * the buffer that is set.
	 */
    private void scan(){
    	if (GomokuKeyCallBack.isKeyDown(GLFW_KEY_ESCAPE))
    		glfwSetWindowShouldClose(window, true);
    	if (!mousePressed && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
    		glfwGetCursorPos(window, mouseX, mouseY);
    		System.out.println("X: " + mouseX.get(0) + " Y: " + mouseY.get(0));
    		mousePressed = true;
    	}
    	if (mousePressed && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE) {
    		mousePressed = false;
    	}
    }
    
    /**
     * This will loop the visualizer, it will first setup the necessities
     * in order to run @GL (GL.createCapabilities())
     * 
     * It will loop while the window should not close
     * @glClear will clear the frame buffer
     * @glSwapBuffers will swap color buffers
     * @glfwPollEvents is for window events, without this callbacks won't be invoked
     * @scan() is to look for specific input and respond
     */
	private void visualizeLoop() {
		GL.createCapabilities();
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glfwSwapBuffers(window);
			glfwPollEvents();
			scan();
		}
	}

	public static void main(String[] args) {
		new Visualizer().start();
	}
}
