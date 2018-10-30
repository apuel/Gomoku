package org.us._42.laphicet.gomoku.ai;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.PlayerController;

public class Arta implements PlayerController {

	@Override
	public String name(int value) {
		return ("Arta");
	}

	@Override
	public void report(Gomoku game, String message) {
	}

	@Override
	public void informChange(Gomoku game, int x, int y, int value) {	
	}

	@Override
	public void informWinner(Gomoku game, int value) {
	}

	@Override
	public boolean getMove(Gomoku game, int value, long key) {
		for (int x = 0, y = 0; x < 19 && y < 19; x++) {
			if (game.getToken(x, y) == 0) {
				game.submitMove(x, y, key);
				break;
			}
			if (x == 18) {
				x = 0;
				y++;
			}
		}
		return (true);
	}

}
