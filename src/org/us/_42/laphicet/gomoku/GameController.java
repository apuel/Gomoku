package org.us._42.laphicet.gomoku;

import java.util.ArrayList;

public class GameController {
	public static final int BOARD_LENGTH = 19;
	public static final byte PLAYER_COUNT = 2;
	public static final int CAPTURES_TO_WIN = 10;

	private byte[][] board = new byte[BOARD_LENGTH][BOARD_LENGTH];
	
	private GameStateReporter reporter;
	private ArrayList<String> reports = new ArrayList<String>();
	
	private PlayerController[] players = new PlayerController[PLAYER_COUNT];
	private int[] captures = new int[PLAYER_COUNT];
	private byte current = 0;
	private byte winner = 0;
	
	/**
	 * Creates a new game controller for Gomoku.
	 * 
	 * @param reporter A handler for updated game states.
	 * @param players The player controllers representing the participants.
	 */
	public GameController(GameStateReporter reporter, PlayerController... players) {
		this.reporter = reporter;
		
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
	
	/**
	 * Creates a new game controller for Gomoku.
	 * 
	 * @param players The player controllers representing the participants.
	 */
	public GameController(PlayerController... players) {
		this(null, players);
	}
	
	/**
	 * Returns the value for the piece at the location.
	 * 
	 * @param x The x coordinate for the piece.
	 * @param y The y coordinate for the piece.
	 * @return The value of the piece at the given coordinates.
	 */
	public int GetPiece(int x, int y) {
		if (x < 0 || x >= BOARD_LENGTH || y < 0 || y >= BOARD_LENGTH)
			return (-1);
		return (board[x][y]);
	}
	
	/**
	 * Validates if placing a piece at the given coordinates os allowed.
	 * 
	 * @param x The x coordinate for the piece.
	 * @param y The y coordinate for the piece.
	 * @param piece The value of the piece.
	 * @return Whether or not the move is valid.
	 */
	private boolean ValidateMove(int x, int y, int piece) {
		int position = GetPiece(x, y);
		
		if (position < 0) {
			players[this.current].Report("Coordinates not in bounds!");
			return (false);
		}
		
		if (position != 0) {
			players[this.current].Report("There is already a piece in that position!");
			return (false);
		}
		
		return (true);
	}
	
	/**
	 * Begins a new game of Gomoku.
	 */
	public void StartGame() {
		int[] coords = new int[2];
		
		while (this.winner == 0) {
			this.players[this.current].GetMove(this, (byte)(this.current + 1), coords);
			if (!this.ValidateMove(coords[0], coords[1], (byte)(this.current + 1)))
				continue;
			
			//TEMP
			board[coords[0]][coords[1]] = (byte)(this.current + 1);
			this.reports.add(String.format("%s placed a piece at %d, %d.", this.players[this.current].Name(), coords[0], coords[1]));
			
			if (this.reporter != null) {
				this.reporter.ReportTurn(this, coords[0], coords[1], (byte)(this.current + 1), this.reports);
			}
			this.reports.clear();
			
			this.current = (byte)((this.current + 1) % PLAYER_COUNT);
		}
	}
}
