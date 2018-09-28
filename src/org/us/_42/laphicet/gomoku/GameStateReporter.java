package org.us._42.laphicet.gomoku;

import java.util.Collection;

public interface GameStateReporter {
	/**
	 *  A handler for the placed piece and the events that occurred on the last given turn.
	 * 
	 * @param game The Gomoku game controller including the game board.
	 * @param x The x coordinate of the placed piece.
	 * @param y The y coordinate of the placed piece.
	 * @param piece The value of the placed piece.
	 * @param reports A collection of reports detailing events.
	 */
	public void ReportTurn(GameController game, int x, int y, byte piece, Collection<String> reports);
}
