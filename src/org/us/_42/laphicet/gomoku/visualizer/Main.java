package org.us._42.laphicet.gomoku.visualizer;

import java.awt.GraphicsEnvironment;

import org.us._42.laphicet.gomoku.GameStateReporter;
import org.us._42.laphicet.gomoku.Gomoku;

public class Main {
	static {
		System.setProperty("java.awt.headless", "true");
		if (!GraphicsEnvironment.isHeadless()) {
			throw new RuntimeException("Application requires java.awt.headless to be true!");
		}
	}
	
	public static void main(String... args) {
		Visualizer vis =  new Visualizer();
		Gomoku game = new Gomoku((GameStateReporter)vis, vis, vis);
		
		vis.start();
		game.auto();
		vis.end();
	}
}
