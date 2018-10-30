package org.us._42.laphicet.gomoku;

import java.util.Collection;

public interface GameStateReporter {
	/**
	 * Reports a placed token and other events that occurred on a given turn.
	 * 
	 * @param game The Gomoku game controller including the game board.
	 * @param x The x coordinate of the placed token.
	 * @param y The y coordinate of the placed token.
	 * @param value The value of the placed token.
	 * @param logs A collection of logs detailing events.
	 */
	public void logTurn(Gomoku game, int x, int y, int value, Collection<String> logs);
}
