package org.us._42.laphicet.gomoku.visualizer;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glVertex2i;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

/**
 * Renderer - Shared functions being used by other classes 
 * that will render the visualizer
 * 
 * @author mlu & apuel
 */
public final class Renderer {
    /**
     * Initializes a texture from an image.
     * 
     * @param path The internal resource path for the image.
     * @return The GL texture id
     * @throws IOException If there was an error attempting to read the image.
     */
    public static BufferedImage getBufferedImage(String path) throws IOException {
    	InputStream stream = Visualizer.class.getResourceAsStream(path);
    	BufferedImage image = ImageIO.read(stream);
    	stream.close();
    	
    	return (image);
    }
    
    /**
     * This will take a BufferedImage and make a texture out of it
     * 
     * @param image The BufferedImage that will be textured
     * @param x The starting X position in the BufferedImage
     * @param y The starting Y position in the BufferedImage
     * @param width The width of the texture being created
     * @param height The height of the texture being created
     * @param invert Whether texture should be inverted or not
     * @return returns the texture ID after generating it
     */
    public static int initTexture(BufferedImage image, int x, int y, int width, int height, boolean invert) {
    	int pixels[] = new int[width * height];
    	image.getRGB(x, y, width, height, pixels, 0, width);
    	
    	ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
    	for(int j = 0; j < height; ++j) {
    	    for(int i = 0; i < width; ++i) {
    	        int pixel = pixels[i + j * width];
    	        buffer.put((byte)(invert ? (0xFF - ((pixel >> 16) & 0xFF)) : ((pixel >> 16) & 0xFF)));
    	        buffer.put((byte)(invert ? (0xFF - ((pixel >> 16) & 0xFF)) : ((pixel >> 8) & 0xFF)));
    	        buffer.put((byte)(invert ? (0xFF - (pixel & 0xFF)) : (pixel & 0xFF)));
    	        buffer.put((byte)((pixel >> 24) & 0xFF));
    	    }
    	}
    	buffer.flip();
    	
    	int texture = glGenTextures(); 
        glBindTexture(GL_TEXTURE_2D, texture); 
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    	return (texture);
    }
    
    
    /**
     * Draws a piece on the visualizer.
     * 
     * @param texture The texture id to draw.
     * @param x The absolute x coordinate on the visualizer window.
     * @param y The absolute y coordinate on the visualizer window.
     * @param offset_x 
     * @param offset_y
     */
    public static void renderTexture(int texture, int x, int y, int offset_x, int offset_y) {
    	glBindTexture(GL_TEXTURE_2D, texture);
    	
    	glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
    	glEnable(GL_TEXTURE_2D); 
    	glBegin(GL_QUADS);
    	{
    		glTexCoord2f(0, 1);
    		glVertex2i(x - offset_x, y - offset_y);
    		glTexCoord2f(1, 1);
    		glVertex2i(x + offset_x, y - offset_y);
    		glTexCoord2f(1, 0);
    		glVertex2i(x + offset_x, y + offset_y);
    		glTexCoord2f(0, 0);
    		glVertex2i(x - offset_x, y + offset_y);
    	}
    	glEnd();
    	glDisable(GL_TEXTURE_2D);
    	glPopMatrix();
    }
    
    /**
     * Draws a line from one pair of coordinates to another.
     * 
     * @param x1 The x coordinate for the first pair.
     * @param y1 The y coordinate for the first pair.
     * @param x2 The x coordinate for the second pair.
     * @param y2 The y coordinate for the second pair.
     */
    public static void drawLine(float x1, float y1, float x2, float y2, float size) {
    	glColor3f(0.0f, 0.0f, 0.0f);
    	glBegin(GL_LINES);
    	{
    		for (float i = -size; i < size; i += 0.01f) {
    			glVertex2f(x1 + i, y1 + i);
    			glVertex2f(x2 + i, y2 + i);
    		}
    	}
    	glEnd();
    	glColor3f(1.0f, 1.0f, 1.0f);
    }
    
	/**
	 * Displays the visualizer window.
	 */
	public static void displayWindow(long window) {
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}
}
