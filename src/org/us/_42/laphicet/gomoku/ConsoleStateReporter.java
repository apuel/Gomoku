package org.us._42.laphicet.gomoku;

import java.util.Collection;

public class ConsoleStateReporter implements GameStateReporter {
	@Override
	public void ReportTurn(GameController game, int x, int y, byte piece, Collection<String> reports) {
		System.out.println("[Game State]\n---------------------------------------------------------");
		for (int yi = 0; yi < GameController.BOARD_LENGTH; yi++) {
			for (int xi = 0; xi < GameController.BOARD_LENGTH; xi++) {
				int position = game.GetPiece(xi, yi);
				
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
