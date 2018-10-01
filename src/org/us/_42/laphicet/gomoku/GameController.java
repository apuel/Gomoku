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
		return (this.board[y][x]);
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
		PlayerController player = this.players[this.current];
		int position = GetPiece(x, y);
		
		if (position < 0) {
			player.Report("Coordinates not in bounds!");
			return (false);
		}
		
		if (position != 0) {
			player.Report("There is already a piece in that position!");
			return (false);
		}
		
		//Check whether or not this move would place the piece in a state of capture
		//Check whether or not this move creates a double three
		//If so, check for possible captures made by this move
		//If none, this move is not allowed to create a double three
		//Check if there is 5 in a row somewhere on the board, if so this move MUST counter it
		
		return (true);
	}
	
	/**
	 * Begins a new game of Gomoku.
	 */
	public void StartGame() {
		int[] coords = new int[2];
		
		while (this.winner == 0) {
			PlayerController player = this.players[this.current];
			byte value = (byte)(this.current + 1);
			int captures = this.captures[this.current];
			
			coords[0] = -1; coords[1] = -1;
			player.GetMove(this, value, coords);
			if (!this.ValidateMove(coords[0], coords[1], value))
				continue;
			
			int x = coords[0];
			int y = coords[1];
			
			this.board[y][x] = value;
			this.reports.add(String.format("%s placed a piece at %d, %d.", player.Name(), x, y));
			
			//Apply captures
			
			if (captures >= CAPTURES_TO_WIN) {
				this.winner = value;
				this.reports.add(String.format("%s has captured %d times!", player.Name(), captures));
			}
			
			//If there is 5 in a row, check if it is possible to break it within the next turn
			//If not, flag the player as a winner
			
			if (this.reporter != null) {
				this.reporter.ReportTurn(this, x, y, value, this.reports);
			}
			this.reports.clear();
			
			for (PlayerController p : this.players) {
				p.InformTurn(x, y, value);
			}
			
			this.current = (byte)((this.current + 1) % PLAYER_COUNT);
		}
	}
}
