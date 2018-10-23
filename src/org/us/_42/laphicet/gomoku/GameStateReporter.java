package org.us._42.laphicet.gomoku;

import java.util.Collection;

public interface GameStateReporter {
	/**
	 * Handles a placed piece and other events that occurred on a given turn.
	 * 
	 * @param game The Gomoku game controller including the game board.
	 * @param x The x coordinate of the placed piece.
	 * @param y The y coordinate of the placed piece.
	 * @param value The value of the placed piece.
	 * @param reports A collection of reports detailing events.
	 */
	public void reportTurn(Gomoku game, int x, int y, byte value, Collection<String> reports);
}
