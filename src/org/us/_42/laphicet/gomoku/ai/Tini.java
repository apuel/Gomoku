package org.us._42.laphicet.gomoku.ai;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.Gomoku.AdjacentAlignment;
import org.us._42.laphicet.gomoku.PlayerController;

public class Tini implements PlayerController, AIController {
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
	
	/**
	 * Used on placements where a fatal placement has already been made.
	 */
	private static final int FUTILE_PRIORITY = 1000;
	
	private Gomoku game = null;
	private int value = 1;
	
	private Random rng = new SecureRandom();
	private Set<Long> tokens = new HashSet<Long>();
	
	private int x = -1;
	private int y = -1;
	private int priority = 0;
	
	private double elapsed = 0.0;
	
	private Map<Long,Integer> moves = new HashMap<Long,Integer>();
	private int depth;
	
	private static class TreeNode {
		int value;
		
		int x;
		int y;
		int priority;
		
		private TreeNode[] nodes;
		
		private TreeNode(int length) {
			this.nodes = new TreeNode[length];
		}
		
		@Override
		public TreeNode clone() {
			TreeNode result = new TreeNode(this.nodes.length);
			result.value = this.value;
			result.x = this.x;
			result.y = this.y;
			result.priority = this.priority;
			for (int i = 0; i < result.nodes.length; i++) {
				result.nodes[i] = this.nodes[i].clone();
			}
			return (result);
		}
	}
	
	private TreeNode node;
	
	/**
	 * Creates a new instance of the Tini Gomoku AI.
	 * 
	 * @param nmoves The number of moves to consider at each step.
	 * @param depth How many turns to consider before making a decision.
	 */
	public Tini(int moves, int depth) {
		this.depth = depth;
		
		if (moves > 1 && depth > 0) {
			this.node = new TreeNode(moves);
		}
		else {
			this.node = new TreeNode(0);
		}
	}
	
	/**
	 * Creates a new instance of the Tini Gomoku AI.
	 */
	public Tini() {
		this(1, 0);
	}
	
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
		
		if (priority > 0 && this.depth > 0) {
			long token = ((long)x << 32) | (long)y;
			
			if (this.moves.containsKey(token)) {
				int prev = this.moves.get(token);
				if (prev > priority) {
					return;
				}
			}
			this.moves.put(token, priority);
		}
	}
	
	/**
	 * Checks if a position is free for placing a token.
	 * 
	 * @param x The x coordinate in question.
	 * @param y The y coordinate in question.
	 * @return Whether or not the position is free for placing a token.
	 */
	private boolean isFree(int x, int y, int value) {
		return ((this.game.getToken(x, y) == 0) &&
				!(this.game.createsDoubleThree(x, y, value)) &&
				!(this.game.isCaptured(x, y, value)));
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
	 * Gets the number of adjacent tokens that could fit into this alignment at this placement.
	 * 
	 * @param x The x coordinate of the token in question.
	 * @param y The y coordinate of the token in question.
	 * @param value The value of the token in question.
	 * @param alignment The alignment on the adjacent tokens.
	 * @return The number of adjacent tokens that could fit into this alignment at this placement.
	 */
	private int getAdjacentCapacity(int x, int y, int value, AdjacentAlignment alignment) {
		int dx = alignment.dx;
		int dy = alignment.dy;
		
		int prev = 0;
		for (int xi = x - dx, yi = y - dy; this.isFree(xi, yi, value) || this.game.getToken(xi, yi) == value; xi -= dx, yi -= dy) {
			prev++;
		}
		
		int next = 0;
		for (int xi = x + dx, yi = y + dy; this.isFree(xi, yi, value) || this.game.getToken(xi, yi) == value; xi += dx, yi += dy) {
			next++;
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
				if ((xi != x || yi != y) && this.isFree(xi, yi, this.value) && this.game.wouldCapture(x, y, xi, yi, this.value)) {
					int captures = this.game.countCaptures(xi, yi, this.value);
					int p = priority + (captures * CAPTURE_PRIORITY);
					
					if ((this.game.getCaptureCount(this.value) + captures) >= (Gomoku.CAPTURES_TO_WIN - 2)) {
						p += (captures * LAST_DITCH_PRIORITY);
					}
					
					for (AdjacentAlignment a : AdjacentAlignment.values()) {
						int adjacent = this.getUpdatedAdjacents(xi, yi, value, a);
						int capacity = this.getAdjacentCapacity(xi, yi, value, alignment);
						
						if (adjacent >= (Gomoku.ADJACENT_TO_WIN - 2) && capacity >= Gomoku.ADJACENT_TO_WIN) {
							this.evaluate(xi, yi, p + (adjacent * LAST_DITCH_PRIORITY));
						}
						else {
							this.evaluate(xi, yi, p + (adjacent * ADJACENT_PRIORITY));
						}
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
		int capacity = this.getAdjacentCapacity(x, y, value, alignment);
		
		for (int i = 0; i < 2; i++) {
			for (int xi = x + dx, yi = y + dy; ; xi += dx, yi += dy) {
				int token = this.game.getToken(xi, yi);
				if (token != value) {
					if (this.isFree(xi, yi, this.value)) {
						int p = priority + (this.game.countCaptures(xi, yi, this.value) * CAPTURE_PRIORITY);
						int adjacent = this.getUpdatedAdjacents(xi, yi, value, alignment);
						
						if (adjacent - 1 - this.game.getAdjacentTokenCount(x, y, alignment) >= Gomoku.ADJACENT_TO_WIN) {
							break;
						}
						
						if (adjacent >= (Gomoku.ADJACENT_TO_WIN - 2) && capacity >= Gomoku.ADJACENT_TO_WIN) {
							this.evaluate(xi, yi, p + (adjacent * LAST_DITCH_PRIORITY));
						}
						else if (value == this.value && this.game.isInDanger(x, y, value)) {
							this.evaluate(xi, yi, p + (adjacent * DEFENSE_PRIORITY));
						}
						else {
							this.evaluate(xi, yi, p + (adjacent * ADJACENT_PRIORITY));
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
				this.attemptCapture(x, y, value, 0);
			}
			if (adjacent < Gomoku.ADJACENT_TO_WIN) {
				this.surroundToken(x, y, value, alignment, 0);
			}
		}
	}
	
	/**
	 * Evaluates logical moves that could be made and decides on one.
	 */
	private void evaluateMoves() {
		if (this.priority < 0) {
			return;
		}
		
		for (long token : this.tokens) {
			int x = (int)(token >> 32);
			int y = (int)(token & 0xFFFFFFFF);
			int value = this.game.getToken(x, y);
			this.evaluateToken(x, y, value);
		}
		
		if (this.x == -1 || this.y == -1) {
			for (int i = 1; i < (Gomoku.BOARD_LENGTH - 1); i++) {
				if (this.isFree(i, 0, this.value)) {
					this.evaluate(i, 0, 0);
				}
				if (this.isFree(0, i, this.value)) {
					this.evaluate(0, i, 0);
				}
				if (this.isFree(i, Gomoku.BOARD_LENGTH - 1, this.value)) {
					this.evaluate(i, Gomoku.BOARD_LENGTH - 1, 0);
				}
				if (this.isFree(Gomoku.BOARD_LENGTH - 1, i, this.value)) {
					this.evaluate(Gomoku.BOARD_LENGTH - 1, i, 0);
				}
			}
			
			if (this.x == -1 || this.y == -1) {
				do {
					this.x = this.rng.nextInt(Gomoku.BOARD_LENGTH);
					this.y = this.rng.nextInt(Gomoku.BOARD_LENGTH);
				}
				while (!(this.isFree(this.x, this.y, this.value)));
			}
		}
	}
	
	private Tini self = null;
	private Tini next = null;
	
	/**
	 * Generates an n-ary tree with possible outcomes for the next 'depth' amount of turns.
	 */
	private void generateTree() {
		if (this.priority <= 0 || this.node.nodes.length <= 1 || this.depth <= 0) {
			return;
		}
		
		if (this.self == null || this.next == null) {
			this.self = new Tini(this.node.nodes.length, this.depth - 1);
			this.next = new Tini(this.node.nodes.length, this.depth - 1);
		}
		
		List<Entry<Long,Integer>> moves = new ArrayList<Entry<Long,Integer>>(this.moves.entrySet());
		moves.sort(new Comparator<Entry<Long,Integer>>() {
			@Override
			public int compare(Entry<Long,Integer> a, Entry<Long,Integer> b) {
				return (b.getValue().compareTo(a.getValue()));
			}
		});
		
		for (int i = 0; i < this.node.nodes.length; i++) {
			if (moves.size() <= i) {
				this.node.nodes[i] = null;
				break;
			}
			
			Entry<Long,Integer> move = moves.get(i);
			long token = move.getKey();
			int x = (int)(token >> 32);
			int y = (int)(token & 0xFFFFFFFF);
			int priority = move.getValue();
			
			Gomoku game = this.game.clone(null, this.self);
			this.self.gameStart(game, this.value);
			this.next.gameStart(game, (this.value % Gomoku.PLAYER_COUNT) + 1);
			
			this.self.x = x;
			this.self.y = y;
			this.self.priority = -1;
			game.next(); //Make the designated move
			game.next(); //Branch!
			
			this.node.nodes[i] = next.node.clone();
			this.node.nodes[i].x = x;
			this.node.nodes[i].y = y;
			this.node.nodes[i].priority = priority;
			
			this.self.gameEnd(game);
			this.next.gameEnd(game);
		}
		
		moves.clear();
	}
	
	/**
	 * Evaluates the previously generated n-ary tree and decides on a move to make.
	 */
	private void evaluateTree() {
		if (this.priority <= 0 || this.node.nodes.length <= 1 || this.depth <= 0) {
			return;
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
		
		long token = ((long)x << 32) | (long)y;
		
		if (value == 0) {
			this.tokens.remove(token);
		}
		else {
			this.tokens.add(token);
			
			if (value != this.value) {
				for (AdjacentAlignment alignment : AdjacentAlignment.values()) {
					if (this.game.getAdjacentTokenCount(x, y, alignment) >= Gomoku.ADJACENT_TO_WIN) {
						this.attemptCapture(x, y, value, FUTILE_PRIORITY);
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void informWinner(Gomoku game, int value) {
		if (this.game == game) {
		}
	}
	
	@Override
	public boolean getMove(Gomoku game, int value, long key) {
		if (this.game == null || this.game != game) {
			return (false);
		}
		
		long start = System.nanoTime();
		this.evaluateMoves();
		this.generateTree();
		this.evaluateTree();
		game.submitMove(this.x, this.y, key);
		this.elapsed = (double)(System.nanoTime() - start) / 1000000000.0;
		
		this.x = -1;
		this.y = -1;
		this.priority = 0;
		this.moves.clear();
		return (true);
	}
	
	@Override
	public void gameStart(Gomoku game, int value) {
		if (this.game != null) {
			throw new IllegalStateException("This AI only supports playing one game at a time.");
		}
		
		this.game = game;
		this.value = value;
		this.node.value = value;
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
			
			this.x = -1;
			this.y = -1;
			this.priority = 0;
			this.moves.clear();
		}
	}
	
	@Override
	public double getTimeElapsed() {
		return (this.elapsed);
	}
}
