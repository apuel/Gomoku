package org.us._42.laphicet.gomoku;

public interface PlayerController {
	/**
	 * Returns a name for the Player.
	 * 
	 * @return A name.
	 */
	public String Name();
	
	/**
	 * Reports an error message to the player.
	 * 
	 * @param message An error message.
	 */
	public void Report(String message);
	
	/**
	 * Returns a move to be played on the given Gomoku game board.
	 * 
	 * @param game The game controller for the game.
	 * @param piece The piece to be played.
	 * @param coords The output buffer for the played coordinates. coords[0] = x, coords[1] = y
	 */
	public void GetMove(GameController game, byte piece, int[] coords);
}
