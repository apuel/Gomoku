package org.us._42.laphicet.gomoku;

public class Main {
	public static void main(String... args) {
		PlayerController player = new InputPlayerController();
		GameStateReporter reporter = new ConsoleStateReporter();
		GameController game = new GameController(reporter, player, player);
		game.start();
	}
}
