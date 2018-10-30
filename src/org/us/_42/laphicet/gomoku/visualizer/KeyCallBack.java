package org.us._42.laphicet.gomoku.visualizer;

import org.lwjgl.glfw.GLFWKeyCallback;
import static org.lwjgl.glfw.GLFW.*;

public class KeyCallBack extends GLFWKeyCallback {
	public static boolean[] keys = new boolean[65536];

	/**
	 * Checks to see what key is held
	 * 
	 * @param keycode
	 * @return Returns the key state if it was indeed pressed
	 */
	public static boolean isKeyDown(int keycode) {
		return keys[keycode];
	}
	
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		keys[key] = action != GLFW_RELEASE;
	}
	
}
