package org.us._42.laphicet.gomoku;

import org.us._42.laphicet.gomoku.visualizer.Visualizer;

public class Main {
	public static void main(String... args) {
		PlayerController player = new InputPlayerController();
		GameStateReporter reporter = new ConsoleStateReporter();
		Visualizer vis =  new Visualizer();
		vis.start();
		GameController game = new GameController((GameStateReporter)vis, vis, vis);
		game.start();
		vis.end();
	}
}
