package org.us._42.laphicet.gomoku.debug;

import java.util.Collection;

import org.us._42.laphicet.gomoku.GameStateReporter;
import org.us._42.laphicet.gomoku.Gomoku;

public class ConsoleStateReporter implements GameStateReporter {
	@Override
	public void reportTurn(Gomoku game, int x, int y, byte piece, Collection<String> reports) {
		System.out.println("[Game State]\n---------------------------------------------------------");
		for (int yi = 0; yi < Gomoku.BOARD_LENGTH; yi++) {
			for (int xi = 0; xi < Gomoku.BOARD_LENGTH; xi++) {
				int position = game.getPiece(xi, yi);
				
				if (xi == x && yi == y) {
					System.out.print(String.format("[%d]", position));
				}
				else {
					System.out.print(String.format(" %d ", position));
				}
			}
			System.out.print("\n");
		}
		
		for (String report : reports) {
			System.out.println("[REPORT] " + report);
		}
		System.out.flush();
	}
}
