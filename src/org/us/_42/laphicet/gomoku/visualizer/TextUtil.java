package org.us._42.laphicet.gomoku.visualizer;

import static org.lwjgl.opengl.GL11.glColor3f;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * TextUtil - Object that generates an alphabet texture and uses it
 * to render text to the openGL window
 * 
 * @author mlu & Apuel
 */
public class TextUtil {
	
	private BufferedImage fontImage = null;
	private int[] alphabet = new int[127 - 33];
	public final int trueWidth;
	public final int width;
	public final int height;
	public final float SCALE = 10f;
	
	/**
	 * @throws IOException 
	 * 
	 */
	public TextUtil(String path, int elementInRow, int elementInColumn) throws IOException {
			fontImage = Renderer.getBufferedImage(path);
			trueWidth = fontImage.getWidth();
			width = (trueWidth - (elementInRow + 1)) / elementInRow;
			height = (fontImage.getHeight() - (elementInColumn + 1)) / elementInColumn;
	}
	
	/**
	 * 
	 */
	public void initAlphabet() {
		int x = 1;
		int y = 1;
		
		for (int i = 33; i < 127; i++) {
			this.alphabet[i - 33] = Renderer.initTexture(fontImage, x, y, width, height, true);
			x += width + 1;
			if (x + width > trueWidth) {
				x = 1;
				y += height + 1;
			}
		}
	}
	
	/**
	 * 
	 * @param line
	 * @param x
	 * @param y
	 * @param size
	 * @param color
	 */
	public void drawString(String line, int x, int y, float size, Float[] color) {
		int newWidth = (int) (width * (size / SCALE));
		int newHeight = (int) (height * (size / SCALE));
		glColor3f(color[0], color[1], color[2]);
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c >= 33 && c <= 126) {
				Renderer.renderTexture(alphabet[line.charAt(i) - 33], x, y, newWidth, newHeight);
			}
			x += newWidth;
		}
		glColor3f(1.0f, 1.0f, 1.0f);
	}
	
	public void drawStringBackwards(String line, int x, int y, float size, Float[] color) {
		int newWidth = (int) (width * (size / SCALE));
		int newHeight = (int) (height * (size / SCALE));
		glColor3f(color[0], color[1], color[2]);
		for (int i = line.length() - 1; i >= 0; i--) {
			char c = line.charAt(i);
			x -= newWidth;
			if (c >= 33 && c <= 126) {
				Renderer.renderTexture(alphabet[line.charAt(i) - 33], x, y, newWidth, newHeight);
			}
		}
		glColor3f(1.0f, 1.0f, 1.0f);
	}
}
