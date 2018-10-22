package org.us._42.laphicet.gomoku;

public interface PlayerController {
	/**
	 * Returns a name for the Player.
	 * 
	 * @param value An value representing the player's piece.
	 * This can be used to implement multiple players within one PlayerController.
	 * @return A name.
	 */
	public String name(byte value);
	
	/**
	 * Reports an error message to the player.
	 * 
	 * @param message An error message.
	 */
	public void report(String message);
	
	/**
	 * Informs the player that a piece has been placed.
	 * 
	 * @param x The x coordinate on the game board.
	 * @param y The y coordinate on the game board.
	 * @param value The value of the piece.
	 */
	public void informMove(int x, int y, byte value);
	
	/**
	 * Returns a move to be played on the given Gomoku game board.
	 * 
	 * @param game The game controller for the game.
	 * @param value The piece to be played.
	 * @param coords The output buffer for the played coordinates. coords[0] = x, coords[1] = y
	 */
	public void getMove(GameController game, byte value, int[] coords);
}
