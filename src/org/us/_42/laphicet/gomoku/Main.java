package org.us._42.laphicet.gomoku;

public class Main {
	public static void main(String... args) {
		PlayerController player = new InputPlayerController();
		GameController game = new GameController(player, player);
		game.StartGame();
	}
}
