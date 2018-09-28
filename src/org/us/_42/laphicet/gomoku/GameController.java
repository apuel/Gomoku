package org.us._42.laphicet.gomoku;

public class GameController {
	private static final int BOARD_LENGTH = 19;
	private static final byte PLAYER_COUNT = 2;
	private static final int CAPTURES_TO_WIN = 10;

	private byte[][] board = new byte[BOARD_LENGTH][BOARD_LENGTH];
	
	private PlayerController[] players = new PlayerController[PLAYER_COUNT];
	private int[] captures = new int[PLAYER_COUNT];
	private byte current = 0;
	private byte winner = 0;
	
	public GameController(PlayerController... players) {
		if (players.length < PLAYER_COUNT) {
			throw new IllegalArgumentException("Not enough players to start a game!");
		}
		
		for (int i = 0; i < PLAYER_COUNT; i++) {
			if (players[i] == null) {
				throw new IllegalArgumentException("Player may not be null!");
			}
			this.players[i] = players[i];
		}
	}
	
	public int GetPiece(int x, int y) {
		if (x < 0 || x >= BOARD_LENGTH || y < 0 || y >= BOARD_LENGTH)
			return (-1);
		return (board[x][y]);
	}
	
	private boolean ValidateMove(int x, int y) {
		int piece = GetPiece(x, y);
		
		if (piece < 0) {
			players[this.current].Report("Coordinates not in bounds!");
			return (false);
		}
		
		if (piece != 0) {
			players[this.current].Report("There is already a piece in that position!");
			return (false);
		}
		
		return (true);
	}
	
	public void StartGame() {
		int[] coords = new int[2];
		
		while (this.winner == 0) {
			players[this.current].GetMove(this, (byte)(this.current + 1), coords);
			if (!this.ValidateMove(coords[0], coords[1]))
				continue;
			
			//TEMP
			board[coords[0]][coords[1]] = (byte)(this.current + 1);
			
			this.current = (byte)((this.current + 1) % PLAYER_COUNT);
		}
	}
}
