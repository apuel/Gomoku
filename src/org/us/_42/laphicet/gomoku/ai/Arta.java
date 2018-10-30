package org.us._42.laphicet.gomoku.ai;

import java.util.Set;
import java.util.TreeSet;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.PlayerController;

public class Arta implements PlayerController {

	private static class Play implements Comparable<Play> {
		private double score;
		private final int x;
		private final int y;
		
		private Play(double score, int x, int y) {
			this.score = score;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Play)) {
				return (false);
			}
			return (((Play)o).x == this.x && ((Play)o).y == this.y);
		}
		
		@Override
		public int compareTo(Play o) {
			return (Double.compare(o.score, this.score));
		}
	}
	
	private int playerNumber = 0;

	private double[][] scoreBoard = new double[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH];
	private Set<Play> bestMoves = new TreeSet<Play>();

	
	
	
	/**
	 * Calculates the weight and score for heuristics
	 * Captures weight 0.25
	 * 
	 * @param game
	 * @param x
	 * @param y
	 * @return
	 */
	private double calcValue(Gomoku game, int x, int y) {
		double score = 1;
		score = score + ((game.countCaptures(x, y, this.playerNumber)) * 0.25);
		return (score);
	}
	
	/**
	 * This will scan the entire board and fill it with a score. It'll than move all
	 * the info into an TreeSet of objects where score is tied to its x and y position.
	 */
	private void scanBoard(Gomoku game) {
		this.bestMoves.clear();
		for (int x = 0, y = 0; x < Gomoku.BOARD_LENGTH && y < Gomoku.BOARD_LENGTH; x++) {
			this.scoreBoard[x][y] = calcValue(game, x, y);
			this.bestMoves.add(new Play(this.scoreBoard[x][y], x, y));
			if (x == 18) {
				x = -1;
				y++;
			}
		}
	}
	
	@Override
	public String name(Gomoku game, int value) {
		return ("Arta");
	}

	@Override
	public void report(Gomoku game, String message) {
		System.err.println("[Arta] Tuturuu~");
	}

	@Override
	public void informChange(Gomoku game, int x, int y, int value) {	
	}

	@Override
	public void informWinner(Gomoku game, int value) {
	}

	@Override
	public boolean getMove(Gomoku game, int value, long key) {
		if (game.getTurn() == 0) {
			game.submitMove(9, 9, key);
			System.out.println("0.0 9 9");
		}
		else {
			scanBoard(game);
			for (Play move : this.bestMoves) {
				if (game.getToken(move.x, move.y) == 0 &&
					!game.createsDoubleThree(move.x, move.y, value) &&
					!game.isCaptured(move.x, move.y, value))
				{
					game.submitMove(move.x, move.y, key);
					System.out.println(move.score + " " + move.x + " " + move.y);
					break;
				}
			}
		}
		return (true);
	}
	
	@Override
	public void gameStart(Gomoku game, int value) {
		this.playerNumber = value;
	}
	
	@Override
	public void gameEnd(Gomoku game) {
	}
}
