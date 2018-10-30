package org.us._42.laphicet.gomoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Gomoku {
	public static final int BOARD_LENGTH = 19;
	public static final int PLAYER_COUNT = 2;
	public static final int CAPTURES_TO_WIN = 10;
	
	private static class Token {
		private int[] adjacent = new int[4];
		private final int value;
		
		private Token(int value) {
			this.value = value;
		}
	}
	
	private Token[][] board = new Token[BOARD_LENGTH][BOARD_LENGTH];
	
	private GameStateReporter reporter;
	private List<String> logs = new ArrayList<String>();
	
	private PlayerController[] players = new PlayerController[PLAYER_COUNT];
	private Set<PlayerController> set = new HashSet<PlayerController>();
	
	private int[] captures = new int[PLAYER_COUNT];
	private int[] placed = new int[PLAYER_COUNT];
	
	private int turn = 0;
	
	private Token check5 = null;
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
	 * Gets a player by their token value.
	 * 
	 * @param value The player's token value.
	 * @return The number of currently placed tokens.
	 */
	public PlayerController getPlayerController(int value) {
		if (value > 0 && value <= PLAYER_COUNT) {
			return (this.players[value - 1]);
		}
		return (null);
	}
	
	/**
	 * Gets the current turn.
	 * 
	 * @return The current turn.
	 */
	public int getTurn() {
		return (this.turn);
	}
	
	/**
	 * Gets the number of captures a certain player has done.
	 * 
	 * @param value The player's token value.
	 * @return The number of successful captures.
	 */
	public int getCaptureCount(int value) {
		if (value > 0 && value <= PLAYER_COUNT) {
			return (this.captures[value - 1]);
		}
		return (-1);
	}
	
	/**
	 * Gets the number of tokens that a player currently has placed.
	 * 
	 * @param value The player's token value.
	 * @return The number of currently placed tokens.
	 */
	public int getTokensPlaced(int value) {
		if (value > 0 && value <= PLAYER_COUNT) {
			return (this.placed[value - 1]);
		}
		return (-1);
	}
	
	/**
	 * Returns the value for the piece at the location.
	 * 
	 * @param x The x coordinate for the token.
	 * @param y The y coordinate for the token.
	 * @return The value of the token at the given coordinates.
	 */
	public int getToken(int x, int y) {
		if (x < 0 || x >= BOARD_LENGTH || y < 0 || y >= BOARD_LENGTH) {
			return (-1);
		}
		if (this.board[y][x] != null) {
			return (this.board[y][x].value);
		}
		return (0);
	}
	
	/**
	 * Updates all adjacent tokens in a certain direction.
	 * 
	 * @param x The added token's x coordinate.
	 * @param y The added token's y coordinate.
	 * @param token The token being placed.
	 * @param dx The x direction to update.
	 * @param dy The y direction to update.
	 * @param direction The direction ID on the tokens.
	 */
	private void updateAdjacents(int x, int y, Token token, int dx, int dy, int direction) {
		int prev = 0;
		int next = 0;
		
		if (this.getToken(x - dx, y - dy) == token.value) {
			prev = this.board[y - dy][x - dx].adjacent[direction];
		}
		if (this.getToken(x + dx, y + dy) == token.value) {
			next = this.board[y + dy][x + dx].adjacent[direction];
		}
		
		token.adjacent[direction] = prev + 1 + next;
		for (int i = 1; i <= prev; i++) {
			this.board[y - (dy * i)][x - (dx * i)].adjacent[direction] = token.adjacent[direction];
		}
		for (int i = 1; i <= next; i++) {
			this.board[y + (dy * i)][x + (dx * i)].adjacent[direction] = token.adjacent[direction];
		}
	}
	
	/**
	 * Places a token on the game board.
	 * 
	 * @param x The x coordinate for the token.
	 * @param y The y coordinate for the token.
	 * @param value The value of the token.
	 * @return Whether the piece was place or not.
	 */
	private boolean setToken(int x, int y, int value) {
		if (this.board[y][x] != null) {
			return (false);
		}
		
		this.board[y][x] = new Token(value);
		this.updateAdjacents(x, y, this.board[y][x], 1, 0, 0);
		this.updateAdjacents(x, y, this.board[y][x], 0, 1, 1);
		this.updateAdjacents(x, y, this.board[y][x], 1, 1, 2);
		this.updateAdjacents(x, y, this.board[y][x], 1, -1, 3);
		return (true);
	}
	
	/**
	 * Re-calculates the number of adjacent tokens in a direction.
	 * It then updates them all accordingly.
	 * 
	 * @param x The added token's x coordinate.
	 * @param y The added token's y coordinate.
	 * @param token The token being removed.
	 * @param dx The x direction to update.
	 * @param dy The y direction to update.
	 * @param direction The direction ID on the tokens.
	 */
	private void resetAdjacents(int x, int y, Token token, int dx, int dy, int direction) {
		int prev = 0;
		for (int i = 1; this.getToken(x - (dx * i), y - (dy * i)) == token.value; i++) {
			prev++;
		}
		for (int i = 1; i <= prev; i++) {
			this.board[y - (dy * i)][x - (dx * i)].adjacent[direction] = prev;
		}
		
		int next = 0;
		for (int i = 1; this.getToken(x + (dx * i), y + (dy * i)) == token.value; i++) {
			next++;
		}
		for (int i = 1; i <= next; i++) {
			this.board[y + (dy * i)][x + (dx * i)].adjacent[direction] = next;
		}
	}
	
	/**
	 * Removes a token on the game board.
	 * 
	 * @param x The x coordinate of the token.
	 * @param y The y coordinate of the token.
	 */
	private void clearToken(int x, int y) {
		if (this.board[y][x] == null) {
			return;
		}
		
		this.resetAdjacents(x, y, this.board[y][x], 1, 0, 0);
		this.resetAdjacents(x, y, this.board[y][x], 0, 1, 1);
		this.resetAdjacents(x, y, this.board[y][x], 1, 1, 2);
		this.resetAdjacents(x, y, this.board[y][x], 1, -1, 3);
		
		if (this.board[y][x] == this.check5) {
			this.logs.add(this.players[this.check5.value - 1].name(this.check5.value) + " no longer has 5 tokens in a row!");
		}
		
		this.board[y][x] = null;
	}
	
	private static String CAPTURE_FORMAT = "%s captured %s's token at %d, %d.";
	
	/**
	 * Applies a capture (if possible) within the direction of dx and dy.
	 * 
	 * @param x The x coordinate of the placed token.
	 * @param y The y coordinate of the placed token.
	 * @param value The value of the placed token.
	 * @param dx The x direction to attempt a capture in.
	 * @param dy The y direction to attempt a capture in.
	 */
	private void applyCapture(int x, int y, int value, int dx, int dy) {
		PlayerController player = this.players[value - 1];
		
		if (this.getToken(x + (dx * 3), y + (dy * 3)) == value) {
			int x1 = x + (dx * 1);
			int y1 = y + (dy * 1);
			int x2 = x + (dx * 2);
			int y2 = y + (dy * 2);
			
			int v1 = this.getToken(x1, y1);
			int v2 = this.getToken(x2, y2);
			
			if (v1 > 0 && v1 != value && v2 > 0 && v2 != value) {
				this.logs.add(String.format(CAPTURE_FORMAT, player.name(value), this.players[v1 - 1].name(v1), x1, y1));
				this.logs.add(String.format(CAPTURE_FORMAT, player.name(value), this.players[v2 - 1].name(v2), x2, y2));
				this.placed[v1 - 1]--;
				this.placed[v2 - 1]--;
				this.clearToken(x1, y1);
				this.clearToken(x2, y2);
				this.captures[this.turn % PLAYER_COUNT]++;
				
				for (PlayerController p : this.set) {
					p.informChange(this, x1, y1, 0);
					p.informChange(this, x2, y2, 0);
				}
			}
		}
	}
	
	/**
	 * Applies captures possible from a given move.
	 * 
	 * @param x The x coordinate of the placed token.
	 * @param y The y coordinate of the placed token.
	 * @param value The value of the placed token.
	 */
	private void applyCaptures(int x, int y, int value) {
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
	 * Checks if a capture is possible within the direction of dx and dy.
	 * 
	 * @param x The x coordinate of the token.
	 * @param y The y coordinate of the token.
	 * @param value The value of the token.
	 * @param dx The x direction to attempt a capture in.
	 * @param dy The y direction to attempt a capture in.
	 * @return Whether or not the token would be capturable in this direction.
	 */
	private boolean checkCapturable(int x, int y, int value, int dx, int dy) {
		int prev = this.getToken(x - dx, y - dy);
		int next = this.getToken(x + dx, y + dy);
		
		if (prev == value && next != value && next > 0) {
			return (this.getToken(x - (dx * 2), y - (dy * 2)) == next);
		}
		if (next == value && prev != value && prev > 0) {
			return (this.getToken(x + (dx * 2), y + (dy * 2)) == prev);
		}
		
		return (false);
	}
	
	/**
	 * Checks if a capture is possible on this token.
	 * 
	 * @param x The x coordinate of the token.
	 * @param y The y coordinate of the token.
	 * @param value The value of the token.
	 * @return Whether or not the token would be capturable.
	 */
	private boolean isCapturable(int x, int y, int value) {
		return (this.checkCapturable(x, y, value, +1, +0) ||
				this.checkCapturable(x, y, value, +0, +1) ||
				this.checkCapturable(x, y, value, +1, +1) ||
				this.checkCapturable(x, y, value, +1, -1));
	}
	
	/**
	 * Validates if placing a token at the given coordinates is allowed.
	 * 
	 * @param x The x coordinate for the token.
	 * @param y The y coordinate for the token.
	 * @param value The value of the token.
	 * @return Whether or not the move is valid.
	 */
	private boolean validateMove(int x, int y, int value) {
		PlayerController player = this.players[value - 1];
		int token = getToken(x, y);
		
		if (token < 0) {
			player.report(this, String.format("Coordinates (%d, %d) not in bounds!", x, y));
			return (false);
		}
		
		if (token != 0) {
			player.report(this, "There is already a token at that position!");
			return (false);
		}
		
		if (this.isCapturable(x, y, value)) {
			player.report(this, "You may not place a piece into a capture!");
			return (false);
		}
		
		//Check whether or not this move creates a double three
		//If so, check for possible captures made by this move
		//If none, this move is not allowed to create a double three
		
		return (true);
	}
	
	/**
	 * Tries to fetch the next input from 
	 */
	private void nextTurn() {
		int[] coords = new int[2];
		
		if (this.winner == 0 && !(this.abort)) {
			PlayerController player = this.players[this.turn % PLAYER_COUNT];
			int value = (this.turn % PLAYER_COUNT) + 1;
			
			coords[0] = -1; coords[1] = -1;
			if (!player.getMove(this, value, coords)) {
				return;
			}
			if (!this.validateMove(coords[0], coords[1], value)) {
				return;
			}
			
			int x = coords[0];
			int y = coords[1];
			
			this.setToken(x, y, value);
			this.placed[this.turn % PLAYER_COUNT]++;
			this.logs.add(String.format("%s placed a token at %d, %d.", player.name(value), x, y));
			
			this.applyCaptures(x, y, value);
			int captures = this.captures[this.turn % PLAYER_COUNT];
			if (captures >= CAPTURES_TO_WIN) {
				this.winner = value;
				this.logs.add(String.format("%s has captured %d times and won!", player.name(value), captures));
			}
			else if (this.check5 == null) {
				for (int i = 0; i < 4; i++) {
					if (this.board[y][x].adjacent[i] >= 5) {
						this.check5 = this.board[y][x];
						break;
					}
				}
				
				if (this.check5 != null) {
					this.logs.add(player.name(value) + " placed at least 5 tokens in a row!");
					this.logs.add("This move must be countered before the next turn or they will win!");
				}
			}
			else {
				for (int i = 0; i < 4; i++) {
					if (this.check5.adjacent[i] >= 5) {
						this.winner = this.check5.value;
						this.logs.add(player.name(this.check5.value) + " has won!");
						break;
					}
				}
				
				if (this.winner == 0) {
					this.logs.add(this.players[this.check5.value - 1].name(this.check5.value) + " no longer has 5 tokens in a row!");
					this.check5 = null;
				}
			}
			
			if (this.winner == 0) {
				this.turn++;
			}
			
			for (PlayerController p : this.set) {
				p.informChange(this, x, y, value);
				if (this.winner != 0) {
					p.informWinner(this, this.winner);
				}
			}
			
			if (this.reporter != null) {
				this.reporter.logTurn(this, x, y, value, this.logs);
			}
			this.logs.clear();
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
					this.board[y][x] = null;
				}
			}
			
			this.logs.clear();
			this.turn = 0;
			this.check5 = null;
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
			this.set.clear();
			
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
