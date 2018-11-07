package org.us._42.laphicet.gomoku.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.Gomoku.Alignment;
import org.us._42.laphicet.gomoku.PlayerController;


public class Lydeka implements PlayerController, AIController {

	/**
	 * Used to hold the current score and its X Y coords
	 */
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
		public int compareTo(Play o) {
			return (Double.compare(o.score, this.score));
		}
	}
	
	/**
	 * Used to hold the current score and go further in to see what the best moves may be
	 */
	private static class Prediction implements Comparable<Prediction> {
		private Play move;
		private double totalScore;
		private List<Prediction> nextPlay = new ArrayList<Prediction>();
		private int value;
		
		private Prediction(Play givePiece, int value) {
			this.move = givePiece;
			this.totalScore = 0;
			this.value = value;
		}
		
		private double highestScore(List<Prediction> checkMove) {
			double ret = 0;
			for (Prediction possibleMoves : checkMove) {
				possibleMoves.getTotal();
				if (possibleMoves.totalScore > ret) {
					ret = possibleMoves.totalScore;
				}
			}
			return (ret);
		}
		
		private void getTotal() {
			if (value == Lydeka.playerNumber) {
				this.totalScore += this.move.score;
			}
			this.totalScore += this.highestScore(this.nextPlay);
//			System.out.println("getTotal(); called, total score " + this.totalScore + " value is " + this.value);
		}
		
		@Override
		public int compareTo(Prediction o) {
			return (Double.compare(o.move.score, this.move.score));
		}
	}
	
	private static int playerNumber;
	private int enemyNumber;
	private int[] togglePlayer = new int[Gomoku.PLAYER_COUNT];

	private double[][] scoreBoard = new double[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH];
	private List<Play> bestMoves = new ArrayList<Play>();
	
//	private List<Prediction> minimax = new ArrayList<Prediction>();
	
	private static final double PLAYERCHAIN = 2.0;
	private static final double ENEMYCHAIN = 3.5;
	private static final double CAPTURE = 2.5;
	private static final double WILLBECAPTURE = 25.0;
	private static final double NANO = 1000000000.0;
	
	private static final int NEGATIVE = 0;
	private static final int POSITIVE = 1;
	
	private final int minmaxAmount;
	private final int minmaxDepth;
	
	private double timeTaken;
//	private long lastKey;
	
	
	public Lydeka(int amount, int depth) {
		this.minmaxAmount = amount;
		this.minmaxDepth = depth;
	}
	

	private int checkNegativeAdjacency(Gomoku game, int x, int y, int value, int chainLength, Alignment align, int currentPiece) {
		for (int i = chainLength; i < 6; i++) {
			currentPiece = game.getToken(x - (align.dx * i), y - (align.dy * i));
			if (currentPiece != 0 || currentPiece != value) {
				 return (1);
			}
		}
		return (0);
	}
	
	private int checkIfBlocked(Gomoku game, int x, int y, int value, int chainLength, Alignment align, int direction) {
		int currentPiece;
		
//		if (align == Alignment.HORIZONTAL) {
			if (direction == Lydeka.NEGATIVE) {
				for (int i = chainLength; i < 6; i++) {
					currentPiece = game.getToken(x - (align.dx * i), y - (align.dy * i));
					if (currentPiece != 0 || currentPiece != value) {
						 return (1);
					}
				}
			}
			else {
				for (int i = chainLength; i < 6; i++) {
					currentPiece = game.getToken(x + (align.dx * i), y + (align.dy * i));
					if (currentPiece != 0 || currentPiece != value) {
						 return (1);
					}
				}
			}
//		}
		return (0);
	}
	
	private double checkSurrounding(Gomoku game, int x, int y, int value) {
		double score = 0;
		double tmp = 0;
		int blocked = 1;
		int chainLength = 0;
		
		for (Alignment chain : Alignment.values()) { 
			if (game.getToken(x - chain.dx, y - chain.dy) == value) {
				chainLength = game.getAdjacentTokenCount(x - chain.dx, y - chain.dy, chain);
				blocked += checkIfBlocked(game, x - chain.dx, y - chain.dy, value, chainLength, chain, Lydeka.NEGATIVE);
				tmp += Math.pow(PLAYERCHAIN, chainLength);
			}
			else if (game.getToken(x - chain.dx, y - chain.dy) > 0) {
				chainLength = game.getAdjacentTokenCount(x - chain.dx, y - chain.dy, chain);
				blocked += checkIfBlocked(game, x - chain.dx, y - chain.dy, value, chainLength, chain, Lydeka.NEGATIVE);
				tmp += Math.pow(ENEMYCHAIN, chainLength);
			}
			if (game.getToken(x + chain.dx, y + chain.dy) == value) {
				chainLength = game.getAdjacentTokenCount(x + chain.dx, y + chain.dy, chain);
				blocked += checkIfBlocked(game, x + chain.dx, y + chain.dy, value, chainLength, chain, Lydeka.POSITIVE);
				tmp += Math.pow(PLAYERCHAIN, chainLength);
			}
			else if (game.getToken(x + chain.dx, y + chain.dy) > 0) {
				chainLength = game.getAdjacentTokenCount(x + chain.dx, y + chain.dy, chain);
				blocked += checkIfBlocked(game, x + chain.dx, y + chain.dy, value, chainLength, chain, Lydeka.POSITIVE);
				tmp += Math.pow(ENEMYCHAIN, chainLength);
			}
			tmp /= blocked;
			if (tmp > score) {
				score = tmp;
			}
			blocked = 1;
			tmp = 0;
		}
		return (score);
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
	private double calcValue(Gomoku game, int x, int y, int value) {
		return (Math.pow(CAPTURE, game.countCaptures(x, y, Lydeka.playerNumber)))
//				- this.captureThreat(game, x, y, value)
				- (game.isInDanger(x, y, value) ? 0 : WILLBECAPTURE )
				+ this.checkSurrounding(game, x, y, value);
	}
	
	/**
	 * This will scan the entire board and fill it with a score. It'll than move all
	 * the info into an TreeSet of objects where score is tied to its x and y position.
	 */
	private void scanBoard(Gomoku game, double[][] moveBoard, int value) {
		this.bestMoves.clear();
//		System.out.println("Begin Score");
		for (int x = 0, y = 0; x < Gomoku.BOARD_LENGTH && y < Gomoku.BOARD_LENGTH; x++) {
			if (moveBoard[x][y] != 0) {
				System.out.println("MOVE BOARD " + x + " " + y);
				this.scoreBoard[x][y] = moveBoard[x][y];
			}
			else if (game.getToken(x, y) == 0 && !game.createsDoubleThree(x, y, value) && !game.isCaptured(x,  y,  value)) {
//					System.out.println("APPROVED MOVE BOARD " + x + " " + y);
					this.scoreBoard[x][y] =  calcValue(game, x, y, value);
			}
			else {
				this.scoreBoard[x][y] =  0;
			}
			this.bestMoves.add(new Play(this.scoreBoard[x][y], x, y));
//			System.out.println("SCORE: " + this.scoreBoard[x][y] + " X: " + x + " Y: " + y);
			if (x == 18) {
				x = -1;
				y++;
			}
		}
		Collections.sort(this.bestMoves);
//		System.out.print("End Score");
	}
	
	/**
	 * Returns a list of minimax moves 
	 * 
	 * @param minimax Current list of moves
	 * @param game
	 * @param amount
	 * @param maxDepth
	 * @param currentDepth
	 * @param moveBoard
	 * @param playerValue
	 * @return
	 */
	private List<Prediction> getMinimax(List<Prediction> minimax, Gomoku game, int amount, int maxDepth, int currentDepth, double[][] moveBoard, int playerValue) {
//		Prediction finalMove = new Prediction(new Play(0, -1, -1));
		List<double[][]> updatedBoard = new ArrayList<double[][]>();
		
		int i = 0;
		for (Play move : this.bestMoves) {
//			System.out.println(this.bestMoves.size());
			minimax.add(new Prediction(move, togglePlayer[playerValue % 2]));
			updatedBoard.add(moveBoard);
			updatedBoard.get(i)[minimax.get(i).move.x][minimax.get(i).move.y] = togglePlayer[playerValue % 2];
//			System.out.println("Depth : " + currentDepth + " Inserting Piece of X [" + minimax.get(i).move.x + "] Y [" + minimax.get(i).move.y + "] Score - " + minimax.get(i).move.score);
			if (++i >= amount) {
				currentDepth++;
				for (int j = 0; j < amount && currentDepth < maxDepth; j++) {
//					System.out.println("Passing into scanBoard X " + minimax.get(j).move.x + " Y " + minimax.get(j).move.y);
					this.scanBoard(game, updatedBoard.get(j), togglePlayer[playerValue % 2]);
					this.getMinimax(minimax.get(j).nextPlay, game, amount, maxDepth, currentDepth, updatedBoard.get(j), playerValue++);
				}
				break; 
			}
		}
		return (minimax);
	}
	
	@Override
	public String name(Gomoku game, int value) {
		return ("Lydeka");
	}

	@Override
	public void report(Gomoku game, String message) {
		System.err.println("[Lydeka] Tuturuu~");

	}

	@Override
	public void informChange(Gomoku game, int x, int y, int value) {	
	}

	@Override
	public void informWinner(Gomoku game, int value) {
	}

	@Override
	public boolean getMove(Gomoku game, int value, long key) {
//		System.out.println("Player " + this.playerNumber + " Enemy " + this.enemyNumber);
		long startTime = System.nanoTime();
//		this.lastKey = key;
		if (game.getTurn() == 0) {
			game.submitMove(9, 9, key);
//			System.out.println("Start 0.0 9 9");
		}
		else {
			this.scanBoard(game, new double[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH], 0);
			List<Prediction> moveToPlay = new ArrayList<Prediction>();
			moveToPlay = this.getMinimax(new ArrayList<Prediction>(), game,
					this.minmaxAmount, this.minmaxDepth * Gomoku.PLAYER_COUNT, 0,
					new double[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH], 0);
			List<Play> sortPlayList = new ArrayList<Play>();
			for (Prediction sortItem : moveToPlay) {
				sortItem.getTotal();
				sortPlayList.add(new Play(sortItem.totalScore, sortItem.move.x, sortItem.move.y));
			}
			Collections.sort(sortPlayList);
			for (Play playMove : sortPlayList) {
				System.out.println("\nCHECKING VALUE " + key + " KEY " + playMove.score + " X: " + playMove.x + " Y: " + playMove.y);
				if (game.getToken(playMove.x, playMove.y) == 0 &&
						!game.createsDoubleThree(playMove.x, playMove.y, value) &&
					!game.isCaptured(playMove.x, playMove.y, value))
				{
					System.out.println("\nMove Made Score: " + key + " KEY " + playMove.score + " X: " + playMove.x + " Y: " + playMove.y);
					game.submitMove(playMove.x, playMove.y, key);
					this.timeTaken = (double)(System.nanoTime() - startTime) / NANO;
					break;
				}
			}
		}
		System.out.println("Test");
		return (true);
	}
	
	@Override
	public void gameStart(Gomoku game, int value) {
		if (value == 1) {
			Lydeka.playerNumber = value;
			this.enemyNumber = value + 1;
		}
		else {
			Lydeka.playerNumber = value;
			this.enemyNumber = value - 1;
		}
		this.togglePlayer[0] = Lydeka.playerNumber;
		this.togglePlayer[1] = this.enemyNumber;
	}
	
	@Override
	public void gameEnd(Gomoku game) {
	}

	@Override
	public double getTimeElapsed() {
		return (this.timeTaken);
	}
}
