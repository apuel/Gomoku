package org.us._42.laphicet.gomoku.ai;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.Gomoku.AdjacentAlignment;
import org.us._42.laphicet.gomoku.PlayerController;

public class Tini implements PlayerController {
	/**
	 * Used on placements that capture tokens.
	 */
	private static final int CAPTURE_PRIORITY = 40;
	
	/**
	 * Used on placements concerned with adjacent tokens.
	 */
	private static final int ADJACENT_PRIORITY = 20;
	
	/**
	 * Used on placements saving tokens from capture.
	 */
	private static final int DEFENSE_PRIORITY = 50;
	
	/**
	 * Used on placements reducing the risk of a fatal follow up.
	 */
	private static final int LAST_DITCH_PRIORITY = 200;
	
	private Gomoku game = null;
	private int value = 1;
	
	private Random rng = new SecureRandom();
	private Set<Long> tokens = new HashSet<Long>();
	
	private int x = -1;
	private int y = -1;
	private int priority = 0;
	
	/**
	 * Evaluates if a placement should be made based on priority.
	 * 
	 * @param x The x coordinate at which a token could be placed.
	 * @param y The y coordinate at which a token could be placed.
	 * @param priority How much the the placement is worth.
	 */
	private void evaluate(int x, int y, int priority) {
		if ((priority > this.priority) || (this.x == -1) || (this.y == -1) ||
			((priority == this.priority) && (this.rng.nextInt(8) == 0)))
		{
			this.x = x;
			this.y = y;
			this.priority = priority;
		}
	}
	
	/**
	 * Checks if a position is free for placing a token.
	 * 
	 * @param x The x coordinate in question.
	 * @param y The y coordinate in question.
	 * @return Whether or not the position is free for placing a token.
	 */
	private boolean isFree(int x, int y) {
		return ((this.game.getToken(x, y) == 0) &&
				!(this.game.createsDoubleThree(x, y, this.value)) &&
				!(this.game.isCaptured(x, y, this.value)));
	}
	
	/**
	 * Get the number of tokens adjacent of an unplaced token (including itself) in a specific alignment.
	 * 
	 * @param x The x coordinate of the token in question.
	 * @param y The y coordinate of the token in question.
	 * @param value The value of the token in question.
	 * @param alignment The alignment on the adjacent tokens.
	 * @return The number of tokens adjacent of a unplaced token, including itself.
	 */
	private int getUpdatedAdjacents(int x, int y, int value, AdjacentAlignment alignment) {
		int prev = 0;
		int next = 0;
		
		if (this.game.getToken(x - alignment.dx, y - alignment.dy) == value) {
			prev = this.game.getAdjacentTokenCount(x - alignment.dx, y - alignment.dy, alignment);
		}
		if (this.game.getToken(x + alignment.dx, y + alignment.dy) == value) {
			next = this.game.getAdjacentTokenCount(x + alignment.dx, y + alignment.dy, alignment);
		}
		
		return (prev + 1 + next);
	}
	
	/**
	 * Attempts to find a suitable spot to capture a specific token from.
	 * 
	 * @param x The x coordinate of the token to be captured.
	 * @param y The y coordinate of the token to be captured.
	 * @param value The value of the token to be captured.
	 * @param priority The base priority for the capture.
	 */
	private void attemptCapture(int x, int y, int value, int priority) {
		if (!this.game.isInDanger(x, y, value)) {
			return;
		}
		
		for (AdjacentAlignment alignment : AdjacentAlignment.values()) {
			int dx = alignment.dx;
			int dy = alignment.dy;
			
			for (int xi = x - (dx * 2), yi = y - (dy * 2); xi != (x + (dx * 3)) || yi != (y + (dy * 3)); xi += dx, yi += dy) {
				if ((xi != x || yi != y) && this.isFree(xi, yi) && this.game.wouldCapture(x, y, xi, yi, this.value)) {
					int p = priority + (this.game.countCaptures(xi, yi, this.value) * CAPTURE_PRIORITY);
					int adjacent = this.getUpdatedAdjacents(xi, yi, value, alignment);
					
					if (adjacent >= Gomoku.ADJACENT_TO_WIN) {
						this.evaluate(xi, yi, p + LAST_DITCH_PRIORITY);
					}
					else if (adjacent > (Gomoku.ADJACENT_TO_WIN / 2)) {
						this.evaluate(xi, yi, p + (ADJACENT_PRIORITY * 4));
					}
					else {
						this.evaluate(xi, yi, p);
					}
				}
			}
		}
	}
	
	/**
	 * Tries to find a suitable location to place a token around a specific token or its adjacent tokens.
	 * 
	 * @param x The x coordinate of the token in question.
	 * @param y The y coordinate of the token in question.
	 * @param value The value of the token in question.
	 * @param alignment The alignment in which a set of tokens are arranged.
	 * @param priority The base priority for the placement.
	 */
	private void surroundToken(int x, int y, int value, AdjacentAlignment alignment, int priority) {
		int dx = alignment.dx;
		int dy = alignment.dy;
		
		for (int i = 0; i < 2; i++) {
			for (int xi = x + dx, yi = y + dy; ; xi += dx, yi += dy) {
				int token = this.game.getToken(xi, yi);
				if (token != value) {
					if (this.isFree(xi, yi)) {
						int p = priority + (this.game.countCaptures(xi, yi, this.value) * CAPTURE_PRIORITY);
						int adjacent = this.getUpdatedAdjacents(xi, yi, value, alignment);
						
						if (adjacent >= Gomoku.ADJACENT_TO_WIN) {
							this.evaluate(xi, yi, p + LAST_DITCH_PRIORITY);
						}
						else if (adjacent > (Gomoku.ADJACENT_TO_WIN / 2)) {
							this.evaluate(xi, yi, p + (ADJACENT_PRIORITY * 4));
						}
						else {
							this.evaluate(xi, yi, p);
						}
					}
					break;
				}
			}
			
			dx = -dx; dy = -dy;
		}
	}
	
	/**
	 * Evaluates a specific token on the game board and what moves can be done from its presence.
	 * 
	 * @param x The x coordinate of the token in question.
	 * @param y The y coordinate of the token in question.
	 * @param value The value of the token in question.
	 */
	private void evaluateToken(int x, int y, int value) {
		for (AdjacentAlignment alignment : AdjacentAlignment.values()) {
			int adjacent = this.game.getAdjacentTokenCount(x, y, alignment);
			
			if (value != this.value) {
				if (adjacent >= Gomoku.ADJACENT_TO_WIN) {
					this.attemptCapture(x, y, value, LAST_DITCH_PRIORITY);
				}
				else if (adjacent > (Gomoku.ADJACENT_TO_WIN / 2)) {
					this.attemptCapture(x, y, value, adjacent * (ADJACENT_PRIORITY * 2));
				}
				else {
					this.attemptCapture(x, y, value, adjacent * ADJACENT_PRIORITY);
				}
			}
			
			if (adjacent >= (Gomoku.ADJACENT_TO_WIN - 1)) {
				this.surroundToken(x, y, value, alignment, LAST_DITCH_PRIORITY);
			}
			else if (adjacent > (Gomoku.ADJACENT_TO_WIN / 2)) {
				this.surroundToken(x, y, value, alignment, adjacent * (ADJACENT_PRIORITY * 2));
			}
			else {
				this.surroundToken(x, y, value, alignment, adjacent * ADJACENT_PRIORITY);
			}
			
			if (value == this.value && this.game.isInDanger(x, y, value)) {
				this.surroundToken(x, y, value, alignment, adjacent * DEFENSE_PRIORITY);
			}
		}
	}
	
	/**
	 * Evaluates logical moves that could be made and decides on one.
	 */
	private void evaluateMoves() {
		for (long token : this.tokens) {
			int x = (int)(token >> 32);
			int y = (int)(token & 0xFFFFFFFF);
			int value = this.game.getToken(x, y);
			this.evaluateToken(x, y, value);
		}
		
		if (this.x == -1 || this.y == -1) {
			for (int i = 1; i < (Gomoku.BOARD_LENGTH - 1); i++) {
				if (this.isFree(i, 0)) {
					this.evaluate(i, 0, 0);
				}
				if (this.isFree(0, i)) {
					this.evaluate(0, i, 0);
				}
				if (this.isFree(i, Gomoku.BOARD_LENGTH - 1)) {
					this.evaluate(i, Gomoku.BOARD_LENGTH - 1, 0);
				}
				if (this.isFree(Gomoku.BOARD_LENGTH - 1, i)) {
					this.evaluate(Gomoku.BOARD_LENGTH - 1, i, 0);
				}
			}
			
			if (this.x == -1 || this.y == -1) {
				do {
					this.x = this.rng.nextInt(Gomoku.BOARD_LENGTH);
					this.y = this.rng.nextInt(Gomoku.BOARD_LENGTH);
				}
				while (!(this.isFree(this.x, this.y)));
			}
		}
	}
	
	@Override
	public String name(Gomoku game, int value) {
		return (this.getClass().getSimpleName());
	}
	
	@Override
	public void report(Gomoku game, String message) {
		throw new RuntimeException(this.name(null, 0) + " >> " + message);
	}
	
	@Override
	public void informChange(Gomoku game, int x, int y, int value) {
		if (this.game == null || this.game != game) {
			return;
		}
		
		this.x = -1;
		this.y = -1;
		this.priority = 0;
		
		long token = ((long)x << 32) | (long)y;
		
		if (value == 0) {
			this.tokens.remove(token);
		}
		else {
			this.tokens.add(token);
			
			if (value != this.value) {
				for (AdjacentAlignment alignment : AdjacentAlignment.values()) {
					int adjacent = this.game.getAdjacentTokenCount(x, y, alignment);
					if (adjacent >= Gomoku.ADJACENT_TO_WIN) {
						this.attemptCapture(x, y, value, 2000);
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void informWinner(Gomoku game, int value) { }
	
	@Override
	public boolean getMove(Gomoku game, int value, long key) {
		if (this.game == null || this.game != game) {
			return (false);
		}
		
		this.evaluateMoves();
		game.submitMove(this.x, this.y, key);
		return (true);
	}
	
	@Override
	public void gameStart(Gomoku game, int value) {
		if (this.game != null) {
			throw new IllegalStateException("This AI only supports playing one game at a time.");
		}
		
		this.game = game;
		this.value = value;
		for (int y = 0; y < Gomoku.BOARD_LENGTH; y++) {
			for (int x = 0; x < Gomoku.BOARD_LENGTH; x++) {
				if (game.getToken(x, y) != 0) {
					this.tokens.add(((long)x << 32) | (long)y);
				}
			}
		}
	}
	
	@Override
	public void gameEnd(Gomoku game) {
		if (this.game == game) {
			this.game = null;
			this.tokens.clear();
		}
	}
}
