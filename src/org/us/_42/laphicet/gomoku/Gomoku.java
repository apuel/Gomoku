package org.us._42.laphicet.gomoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Gomoku {
	public static final int BOARD_LENGTH = 19;
	public static final int PLAYER_COUNT = 2;
	public static final int CAPTURES_TO_WIN = 10;
	
	private byte[][] board = new byte[BOARD_LENGTH][BOARD_LENGTH];
	
	private GameStateReporter reporter;
	private List<String> reports = new ArrayList<String>();
	
	private PlayerController[] players = new PlayerController[PLAYER_COUNT];
	private Set<PlayerController> set = new HashSet<PlayerController>();
	
	private int[] captures = new int[PLAYER_COUNT];
	private int current = 0;
	private int winner = 0;
	
	private boolean abort = false;
	private boolean running = false;
	
	/**
	 * Creates a new game controller for Gomoku.
	 * 
	 * @param reporter A handler for updated game states.
	 * @param players The player controllers representing the participants.
	 */
	public Gomoku(GameStateReporter reporter, PlayerController... players) {
		this.reporter = reporter;
		
		if (players.length < PLAYER_COUNT) {
			throw new IllegalArgumentException("Not enough players to start a game!");
		}
		
		for (int i = 0; i < PLAYER_COUNT; i++) {
			if (players[i] == null) {
				throw new IllegalArgumentException("Player may not be null!");
			}
			this.players[i] = players[i];
			this.set.add(players[i]);
		}
	}
	
	/**
	 * Creates a new game controller for Gomoku.
	 * 
	 * @param players The player controllers representing the participants.
	 */
	public Gomoku(PlayerController... players) {
		this(null, players);
	}
	
	/**
	 * Returns the value for the piece at the location.
	 * 
	 * @param x The x coordinate for the piece.
	 * @param y The y coordinate for the piece.
	 * @return The value of the piece at the given coordinates.
	 */
	public int getPiece(int x, int y) {
		if (x < 0 || x >= BOARD_LENGTH || y < 0 || y >= BOARD_LENGTH) {
			return (-1);
		}
		return (this.board[y][x]);
	}
	
	/**
	 * Validates if placing a piece at the given coordinates is allowed.
	 * 
	 * @param x The x coordinate for the piece.
	 * @param y The y coordinate for the piece.
	 * @param value The value of the piece.
	 * @return Whether or not the move is valid.
	 */
	private boolean validateMove(int x, int y, int value) {
		PlayerController player = this.players[this.current];
		int tmp = getPiece(x, y);
		
		if (tmp < 0) {
			player.report("Coordinates not in bounds!");
			return (false);
		}
		
		if (tmp != 0) {
			player.report("There is already a piece in that position!");
			return (false);
		}
		
		//Check whether or not this move would place the piece in a state of capture
		//Check whether or not this move creates a double three
		//If so, check for possible captures made by this move
		//If none, this move is not allowed to create a double three
		//Check if there is 5 in a row somewhere on the board, if so this move MUST counter it
		
		return (true);
	}
	
	private static String CAPTURE_FORMAT = "%s captured %s's piece at %d, %d.";
	
	/**
	 * Applies a capture (if possible) within the vector of dx and dy.
	 * 
	 * @param x The x coordinate of the placed piece.
	 * @param y The y coordinate of the placed piece.
	 * @param value The value of the placed piece.
	 * @param dx The x direction to attempt a capture in.
	 * @param dy The y direction to attempt a capture in.
	 */
	private void applyCapture(int x, int y, byte value, int dx, int dy) {
		PlayerController player = this.players[this.current];
		
		if (this.getPiece(x + (dx * 3), y + (dy * 3)) == value) {
			int v1 = this.getPiece(x + (dx * 1), y + (dy * 1));
			int v2 = this.getPiece(x + (dx * 2), y + (dy * 2));
			
			if (v1 > 0 && v1 != value && v2 > 0 && v2 != value) {
				this.reports.add(String.format(CAPTURE_FORMAT, player.name(value), this.players[v1 - 1].name((byte)v1), x + (dx * 1), y + (dy * 1)));
				this.reports.add(String.format(CAPTURE_FORMAT, player.name(value), this.players[v2 - 1].name((byte)v2), x + (dx * 2), y + (dy * 2)));
				this.board[y + (dy * 1)][x + (dx * 1)] = 0;
				this.board[y + (dy * 2)][x + (dx * 2)] = 0;
				this.captures[this.current]++;
				
				for (PlayerController p : this.set) {
					p.informMove(x + (dx * 1), y + (dy * 1), (byte)0);
					p.informMove(x + (dx * 2), y + (dy * 2), (byte)0);
				}
			}
		}
	}
	
	/**
	 * Applies captures possible from a given move.
	 * 
	 * @param x The x coordinate of the placed piece.
	 * @param y The y coordinate of the placed piece.
	 * @param value The value of the placed piece.
	 */
	private void applyCaptures(int x, int y, byte value) {
		this.applyCapture(x, y, value, -1, +0);
		this.applyCapture(x, y, value, +1, +0);
		this.applyCapture(x, y, value, +0, -1);
		this.applyCapture(x, y, value, +0, +1);
		
		this.applyCapture(x, y, value, -1, +1);
		this.applyCapture(x, y, value, +1, +1);
		this.applyCapture(x, y, value, +1, -1);
		this.applyCapture(x, y, value, -1, -1);
	}
	
	/**
	 * Tries to fetch the next input from 
	 */
	private void nextTurn() {
		int[] coords = new int[2];
		
		if (this.winner == 0 && !(this.abort)) {
			PlayerController player = this.players[this.current];
			byte value = (byte)(this.current + 1);
			int captures = this.captures[this.current];
			
			coords[0] = -1; coords[1] = -1;
			if (!player.getMove(this, value, coords)) {
				return;
			}
			if (!this.validateMove(coords[0], coords[1], value)) {
				return;
			}
			
			int x = coords[0];
			int y = coords[1];
			
			this.board[y][x] = value;
			this.reports.add(String.format("%s placed a piece at %d, %d.", player.name(value), x, y));
			
			for (PlayerController p : this.set) {
				p.informMove(x, y, value);
			}
			
			this.applyCaptures(x, y, value);
			if (captures >= CAPTURES_TO_WIN) {
				this.winner = value;
				this.reports.add(String.format("%s has captured %d times and won!", player.name(value), captures));
			}
			else {
				//If there is 5 in a row, check if it is possible to break it within the next turn
				//If not, flag the player as a winner
			}
			
			if (this.reporter != null) {
				this.reporter.reportTurn(this, x, y, value, this.reports);
			}
			this.reports.clear();
			
			this.current = (this.current + 1) % PLAYER_COUNT;
		}
	}
	
	/**
	 * Runs the next turn.
	 */
	public void next() {
		if (this.running) {
			return;
		}
		
		this.running = true;
		this.nextTurn();
		this.running = false;
	}
	
	/**
	 * Auto runs the duration of the game.
	 */
	public void auto() {
		if (this.running) {
			return;
		}
		
		this.running = true;
		while (this.winner == 0 && !(this.abort)) {
			this.nextTurn();
		}
		this.running = false;
	}
	
	/**
	 * Forces a fatal exit condition on the game's main loop.
	 */
	public void abort() {
		this.abort = true;
	}
	
	/**
	 * Resets the state of the game.
	 */
	public void reset() {
		if (!this.running) {
			for (int y = 0; y < BOARD_LENGTH; y++) {
				for (int x = 0; x < BOARD_LENGTH; x++) {
					this.board[y][x] = 0;
				}
			}
			
			reports.clear();
			this.current = 0;
			this.winner = 0;
			this.abort = false;
		}
	}
	
	/**
	 * Resets the state of the game.
	 * 
	 * @param reporter A handler for updated game states.
	 * @param players The player controllers representing the participants.
	 */
	public void reset(GameStateReporter reporter, PlayerController... players) {
		if (!this.running) {
			this.reset();
			
			this.reporter = reporter;
			
			if (players.length < PLAYER_COUNT) {
				throw new IllegalArgumentException("Not enough players to start a game!");
			}
			
			for (int i = 0; i < PLAYER_COUNT; i++) {
				if (players[i] == null) {
					throw new IllegalArgumentException("Player may not be null!");
				}
				this.players[i] = players[i];
				this.set.add(players[i]);
			}
		}
	}
	
	/**
	 * Resets the state of the game.
	 * 
	 * @param players The player controllers representing the participants.
	 */
	public void reset(PlayerController... players) {
		this.reset(null, players);
	}
}
