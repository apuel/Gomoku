package org.us._42.laphicet.gomoku;

import org.us._42.laphicet.gomoku.visualizer.Visualizer;

public class Main {
	public static void main(String... args) {
		Visualizer vis =  new Visualizer();
		GameController game = new GameController((GameStateReporter)vis, vis, vis);
		
		vis.start();
		game.start();
		vis.end();
	}
}
