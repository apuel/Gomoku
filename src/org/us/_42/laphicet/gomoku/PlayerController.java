package org.us._42.laphicet.gomoku;

public interface PlayerController {
	public void Report(String message);
	public void GetMove(GameController game, byte piece, int[] coords);
}
