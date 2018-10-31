package org.us._42.laphicet.gomoku.ai;

import java.util.Set;
import java.util.TreeSet;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.Gomoku.AdjacentAlignment;
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
	
	private static final double PLAYERCHAIN = 2.2;
	private static final double ENEMYCHAIN = 2.1;
	private static final double CAPTURE = 2.5;
	private static final double WILLBECAPTURE = 1.5;
	
	
	/**
	 * Will validate if chain is big enough to fit, currently doesn't do that but will soon
	 * @param game
	 * @param x
	 * @param y
	 * @param value
	 * @param direction 0 = vertical, 1 = horizontal, 2 = diag, 3 = diag neg
	 * @return
	 */
	private int checkChain(Gomoku game, int x, int y, int value, int direction) {
		int ret = 0;
		
		if (direction == 0) {
			ret += game.getToken(x - 1, y) == value ? 1 : 0;
			ret += game.getToken(x + 1, y) == value ? 1 : 0;
		}
		else if (direction == 1) {
			ret += game.getToken(x, y - 1) == value ? 1 : 0;
			ret += game.getToken(x, y + 1) == value ? 1 : 0;
		}
		else if (direction == 2) {
			ret += game.getToken(x - 1, y - 1) == value ? 1 : 0;
			ret += game.getToken(x + 1, y + 1) == value ? 1 : 0;
		}
		else if (direction == 3) {
			ret += game.getToken(x + 1, y - 1) == value ? 1 : 0;
			ret += game.getToken(x - 1, y + 1) == value ? 1 : 0;
		}
		return (ret);
	}

	/**
	 * Checks the surrounding pieces and calculates the weight of how effective
	 * that piece played will be
	 * 
	 * Chaining a piece next to it = 1.5, blocking an enemy = 0.5
	 * 
	 * @param game
	 * @param x
	 * @param y
	 * @param d
	 * @return
	 */
//	private double checkSurrounding(Gomoku game, int x, int y) {
//		return ( this.getScoreLeft(game, x, y, x - 1, y + 0, 0, 0, 0) +
//				 this.getScoreDown(game, x, y, x + 0, y - 1, 0, 0, 0) +
//				this.getScoreRight(game, x, y, x + 1, y + 0, 0, 0, 0) +
//				   this.getScoreUp(game, x, y, x + 0, y + 1, 0, 0, 0) +
//		     this.getScoreDownLeft(game, x, y, x - 1, y - 1, 0, 0, 0) +
//			  this.getScoreUpRight(game, x, y, x + 1, y + 1, 0, 0, 0) +
//			   this.getScoreUpLeft(game, x, y, x - 1, y + 1, 0, 0, 0) +
//			this.getScoreDownRight(game, x, y, x + 1, y - 1, 0, 0, 0));
//	}
	
	private double checkSurrounding(Gomoku game, int x, int y) {
		double score = 0;
		double tmp = 0;
		
		for (AdjacentAlignment chain : AdjacentAlignment.values()) { 
			if (game.getToken(x - chain.dx, y - chain.dy) == this.playerNumber) {
				tmp += Math.pow(PLAYERCHAIN, game.getAdjacentTokenCount(x - chain.dx, y - chain.dy, chain));
			}
			else if (game.getToken(x - chain.dx, y - chain.dy) > 0) {
				tmp += Math.pow(ENEMYCHAIN, game.getAdjacentTokenCount(x - chain.dx, y - chain.dy, chain));
			}
			if (game.getToken(x + chain.dx, y + chain.dy) == this.playerNumber) {
				tmp += Math.pow(PLAYERCHAIN, game.getAdjacentTokenCount(x + chain.dx, y + chain.dy, chain));
			}
			else if (game.getToken(x + chain.dx, y + chain.dy) > 0) {
				tmp += Math.pow(ENEMYCHAIN, game.getAdjacentTokenCount(x + chain.dx, y + chain.dy, chain));
			}
			if (tmp > score) {
				score = tmp;
			}
			tmp = 0;
		}
		return (score);
	}
	
	private double captureThreat(Gomoku game, int x, int y) {
		int piece = game.getToken(x, y);
		int tmp = 0;
		int score = 0;
		
		for (AdjacentAlignment enemy : AdjacentAlignment.values()) {
			tmp = game.getToken(x - enemy.dx, y - enemy.dy);
			if (tmp != piece && tmp != 0 && tmp != -1) {
				score += game.wouldCapture(x, y, x - enemy.dx, y - enemy.dy, piece) ? 1 : 0;
			}
			tmp = game.getToken(x + enemy.dx, y + enemy.dy);
			if (tmp != piece && tmp != 0 && tmp != -1) {
				score += game.wouldCapture(x, y, x + enemy.dx, y + enemy.dy, piece) ? 1 : 0;
			}			
		}
		return (Math.pow(WILLBECAPTURE, score));
	}
	
	/**
	 * Calculates the weight and score for heuristics
	 * Captures weight (1^possible captures / 2)
	 * 
	 * @param game
	 * @param x
	 * @param y
	 * @return
	 */
	private double calcValue(Gomoku game, int x, int y) {
		return (Math.pow(CAPTURE, game.countCaptures(x, y, this.playerNumber))) -
				this.captureThreat(game, x, y) +
				this.checkSurrounding(game, x, y);
//		return (this.checkSurrounding(game, x, y));
	}
	
	/**
	 * This will scan the entire board and fill it with a score. It'll than move all
	 * the info into an TreeSet of objects where score is tied to its x and y position.
	 */
	private void scanBoard(Gomoku game) {
		this.bestMoves.clear();
		System.out.println("Begin Score");
		for (int x = 0, y = 0; x < Gomoku.BOARD_LENGTH && y < Gomoku.BOARD_LENGTH; x++) {
			this.scoreBoard[x][y] = (game.getToken(x, y) == 0) ? calcValue(game, x, y) : 0;
			this.bestMoves.add(new Play(this.scoreBoard[x][y], x, y));
			System.out.println("SCORE: " + this.scoreBoard[x][y] + " X: " + x + " Y: " + y);
			if (x == 18) {
				x = -1;
				y++;
			}
		}
		System.out.print("End Score");
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
			System.out.println("Start 0.0 9 9");
		}
		else {
			scanBoard(game);
			for (Play move : this.bestMoves) {
				if (game.getToken(move.x, move.y) == 0 &&
					!game.createsDoubleThree(move.x, move.y, value) &&
					!game.isCaptured(move.x, move.y, value))
				{
					game.submitMove(move.x, move.y, key);
					System.out.println("\nMove Made Score: " + move.score + " X: " + move.x + " Y: " + move.y);
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
