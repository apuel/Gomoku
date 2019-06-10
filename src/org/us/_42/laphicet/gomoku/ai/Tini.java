package org.us._42.laphicet.gomoku.ai;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.Gomoku.Alignment;
import org.us._42.laphicet.gomoku.PlayerController;

public class Tini implements PlayerController, AIController {
	private static final PlayerController NULL_CONTROLLER = new PlayerController() {
		@Override public String name(Gomoku game, int value) { return ("(null)"); }
		@Override public void report(Gomoku game, String message) { }
		@Override public void informChange(Gomoku game, int x, int y, int value) { }
		@Override public void informWinner(Gomoku game, int value) { }
		@Override public boolean getMove(Gomoku game, int value, long key) { return (false); }
		@Override public void gameStart(Gomoku game, int value) { }
		@Override public void gameEnd(Gomoku game) { }
	};
	
	private Gomoku game = null;
	private int value = 1;
	
	private Random rng = new SecureRandom();
	private Set<Long> tokens = new HashSet<Long>();
	
	private int x = -1;
	private int y = -1;
	private int priority = 0;
	
	private double elapsed = 0.0;
	
	private static class TreeNode {
		private Gomoku game;
		private TreeNode[] nodes;
		
		private TreeNode(int length) {
			this.nodes = new TreeNode[length];
		}
		
		@Override
		public TreeNode clone() {
			TreeNode node = new TreeNode(this.nodes.length);
			node.game = this.game;
			
			for (int i = 0; i < node.nodes.length; i++) {
				if (this.nodes[i] != null) {
					node.nodes[i] = this.nodes[i].clone();
				}
				else {
					node.nodes[i] = null;
				}
			}
			return (node);
		}
	}
	
	private TreeNode node;
	
	private Map<Long,Integer> moves = new HashMap<Long,Integer>();
	private int depth;
	private boolean minimax;
	
	/**
	 * Creates a new instance of the Tini Gomoku AI.
	 * 
	 * @param moves The number of moves to consider at each step.
	 * @param depth How many turns to consider before making a decision.
	 * @param minimax Whether or not to use minimax calculations.
	 */
	public Tini(int moves, int depth, boolean minimax) {
		this.depth = depth;
		this.minimax = minimax;
		
		if ((moves > 1) && (depth > 0)) {
			this.node = new TreeNode(moves);
		}
		else {
			this.node = new TreeNode(0);
		}
	}
	
	/**
	 * Creates a new instance of the Tini Gomoku AI.
	 * 
	 * @param moves The number of moves to consider at each step.
	 * @param depth How many turns to consider before making a decision.
	 */
	public Tini(int moves, int depth) {
		this(moves, depth, false);
	}
	
	/**
	 * Creates a new instance of the Tini Gomoku AI.
	 */
	public Tini() {
		this(1, 0, false);
	}
	
	/* ----------- *
	 * Predictions *
	 * ----------- */
	
	private Tini self = null;
	private Tini next = null;
	private PlayerController[] controllers = null;
	private Gomoku[] games = null;
	
	/**
	 * Get the number of captures made by opponents for a specified game.
	 * 
	 * @param game The game to query.
	 * @return The number of captures made by opponents for a specified game.
	 */
	private int getOpponentCaptureCount(Gomoku game) {
		int captures = 0;
		for (int i = 1; i <= Gomoku.PLAYER_COUNT; i++) {
			if (i == this.value) {
				continue;
			}
			captures += game.getCaptureCount(i);
		}
		return (captures);
	}
	
	/**
	 * Evaluates a prediction against a current 'best' prediction.
	 * 
	 * @param node The node we're evaluating.
	 * @param best The current best node.
	 * @return The best node found.
	 */
	private TreeNode evaluatePrediction(TreeNode node, TreeNode best) {
		if (node == null) {
			return (best);
		}
		
		if (best == null) {
			best = node;
		}
		else {
			if (((node.game.getWinner() == this.value) && (best.game.getWinner() != this.value)) ||
				((node.game.getWinner() == 0) && ((best.game.getWinner() != 0) && (best.game.getWinner() != this.value))) ||
				(node.game.getCaptureCount(this.value) > best.game.getCaptureCount(this.value)) ||
				(this.getOpponentCaptureCount(node.game) < this.getOpponentCaptureCount(best.game))) {
				best = node;
			}
		}
		
		for (TreeNode n : node.nodes) {
			best = this.evaluatePrediction(n, best);
		}
		return (best);
	}
	
	/**
	 * Generates an n-ary tree with predictions for the next 'depth' amount of turns.
	 * Afterwards it will proceed to determine which game turned out the best and use that strategy.
	 * This will disregard whatever priority the move may have had before.
	 */
	private void evaluatePredicitons() {
		if ((this.moves.size() <= 1) || (this.depth <= 0) || (this.node.nodes.length <= 1)) {
			return;
		}
		
		if (this.self == null || this.next == null || this.controllers == null) {
			this.self = new Tini();
			this.next = new Tini(this.node.nodes.length, this.depth - 1);
			this.controllers = new PlayerController[Gomoku.PLAYER_COUNT];
			for (int i = 0; i < Gomoku.PLAYER_COUNT; i++) {
				if (i == (this.value - 1)) {
					this.controllers[i] = self;
				}
				else if (i == (this.value % Gomoku.PLAYER_COUNT)) {
					this.controllers[i] = next;
				}
				else {
					this.controllers[i] = NULL_CONTROLLER;
				}
			}
		}
		
		if (this.games == null) {
			this.games = new Gomoku[this.node.nodes.length];
		}
		
		List<Entry<Long,Integer>> moves = new ArrayList<Entry<Long,Integer>>(this.moves.entrySet());
		Collections.shuffle(moves, this.rng);
		moves.sort(new Comparator<Entry<Long,Integer>>() {
			@Override
			public int compare(Entry<Long,Integer> a, Entry<Long,Integer> b) {
				return (b.getValue().compareTo(a.getValue()));
			}
		});
		
		TreeNode best = null;
		for (int i = 0; i < this.node.nodes.length; i++) {
			if (moves.isEmpty()) {
				this.node.nodes[i] = null;
				continue;
			}
			
			if (this.games[i] == null) {
				this.games[i] = this.game.clone(null, this.controllers);
			}
			else {
				this.games[i].cloneOf(this.game);
			}
			
			this.self.gameStart(this.games[i], this.value);
			this.next.gameStart(this.games[i], (this.value % Gomoku.PLAYER_COUNT) + 1);
			
			long token;
			if (this.minimax && (i % 2 != 0)) {
				token = moves.remove(moves.size() - 1).getKey();
			}
			else {
				token = moves.remove(0).getKey();
			}
			
			int x = (int)(token >> 32);
			int y = (int)(token & 0xFFFFFFFF);
			
			this.self.x = x;
			this.self.y = y;
			this.self.priority = -1;
			this.games[i].next(); //Make the designated move
			this.games[i].next(); //Branch!
			this.node.nodes[i] = this.next.node.clone();
			
			this.self.gameEnd(this.games[i]);
			this.next.gameEnd(this.games[i]);
			
			TreeNode node = this.evaluatePrediction(this.node.nodes[i], best);
			if (node != best) {
				this.x = x;
				this.y = y;
				best = node;
			}
		}
		
		moves.clear();
	}
	
	/* ----------------- *
	 * Move calculations *
	 * ----------------- */
	
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
	private int getUpdatedAdjacents(int x, int y, int value, Alignment alignment) {
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
	private int getAdjacentCapacity(int x, int y, int value, Alignment alignment) {
		int dx = alignment.dx;
		int dy = alignment.dy;
		
		int prev = 0;
		for (int xi = x - dx, yi = y - dy; this.isFree(xi, yi, value) || (this.game.getToken(xi, yi) == value); xi -= dx, yi -= dy) {
			prev++;
		}
		
		int next = 0;
		for (int xi = x + dx, yi = y + dy; this.isFree(xi, yi, value) || (this.game.getToken(xi, yi) == value); xi += dx, yi += dy) {
			next++;
		}
		
		return (prev + 1 + next);
	}
	
	/**
	 * Evaluates a specific token on the game board and what placements can be made from its presence.
	 * 
	 * @param x The x coordinate of the token in question.
	 * @param y The y coordinate of the token in question.
	 * @param value The value of the token in question.
	 */
	private void evaluateToken(int x, int y, int value) {
		for (Alignment alignment : Alignment.values()) {
			int dx = alignment.dx;
			int dy = alignment.dy;
			
			//The most tokens of this value that could possibly fit adjacently in this alignment
			int capacity = this.getAdjacentCapacity(x, y, value, alignment);
			
			for (int i = 0; i < 2; i++) {
				for (int xi = x + dx, yi = y + dy; ; xi += dx, yi += dy) {
					//search for the first non-'value' token
					int token = this.game.getToken(xi, yi);
					if (token != value) {
						if (this.isFree(xi, yi, this.value)) {
							//number of captures this would get us
							int captures = this.game.countCaptures(xi, yi, this.value);
							//number of adjacents there could be if a token of the same type was placed
							int adjacent = this.getUpdatedAdjacents(xi, yi, value, alignment);
							
							//find the best number of adjacent tokens we would get by placing this token
							int adjacents = 1;
							for (Alignment a : Alignment.values()) {
								int n = this.getUpdatedAdjacents(xi, yi, value, a);
								if (n > adjacents) {
									adjacents = n;
								}
							}
							
							//calculate priority
						}
						break;
					}
				}
				
				dx = -dx; dy = -dy;
			}
		}
	}
	
	/**
	 * Evaluates on all logical moves that could be made and decides on one based on priority.
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
	
	/* ------------------ *
	 * Overridden methods *
	 * ------------------ */
	
	@Override
	public String name(Gomoku game, int value) {
		return (this.getClass().getSimpleName());
	}
	
	@Override
	public void report(Gomoku game, String message) {
		throw new RuntimeException("[" + this.name(null, 0) + "] Received report: " + message);
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
		}
	}
	
	@Override
	public void informWinner(Gomoku game, int value) {
		if (this.game == game) {
			for (int i = 0; i < this.node.nodes.length; i++) {
				this.node.nodes[i] = null;
			}
		}
	}
	
	@Override
	public boolean getMove(Gomoku game, int value, long key) {
		if (this.game == null || this.game != game) {
			return (false);
		}
		
		long start = System.nanoTime();
		if (this.priority != -1) {
			this.evaluateMoves();
			this.evaluatePredicitons();
		}
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
		this.node.game = game;
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
			this.node.game = null;
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
