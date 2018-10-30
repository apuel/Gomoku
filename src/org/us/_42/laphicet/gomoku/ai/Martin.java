package org.us._42.laphicet.gomoku.ai;

import java.util.Random;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.PlayerController;

public class Martin implements PlayerController {
	private Random rng = new Random();
	
	@Override
	public String name(Gomoku game, int value) {
		return ("Martin");
	}
	
	@Override
	public void report(Gomoku game, String message) {
		System.err.println("[Martin] Whoops, can't place there.");
	}
	
	@Override
	public void informChange(Gomoku game, int x, int y, int value) { }
	
	@Override
	public void informWinner(Gomoku game, int value) { }
	
	@Override
	public boolean getMove(Gomoku game, int value, long key) {
		while (true) {
			int x = rng.nextInt(Gomoku.BOARD_LENGTH);
			int y = rng.nextInt(Gomoku.BOARD_LENGTH);
			
			if (game.getToken(x, y) == 0) {
				game.submitMove(x, y, key);
				break;
			}
		}
		return (true);
	}
	
	@Override
	public void gameStart(Gomoku game, int value) { }
	
	@Override
	public void gameEnd(Gomoku game) { }
}
