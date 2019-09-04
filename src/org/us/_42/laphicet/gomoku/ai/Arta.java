package org.us._42.laphicet.gomoku.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.Gomoku.Alignment;
//import org.us._42.laphicet.gomoku.ai.Lydeka.Play;
import org.us._42.laphicet.gomoku.PlayerController;

public class Arta implements PlayerController, AIController {

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
	private class Prediction implements Comparable<Prediction> {
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
			if (value == playerNumber) {
				this.totalScore += this.move.score;
			}
			this.totalScore += this.highestScore(this.nextPlay);
		}
		
		@Override
		public int compareTo(Prediction o) {
			return (Double.compare(o.move.score, this.move.score));
		}
	}
	
	private int playerNumber;
	private int enemyNumber;
	private int[] playerValues = new int[Gomoku.PLAYER_COUNT];

	private double[][] gameBoard = new double[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH];
	private double[][] scoreBoard = new double[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH];
	
//	private List<Prediction> minimax = new ArrayList<Prediction>();
	
	private static final double PLAYERCHAIN = 2.0;
	private static final double ENEMYCHAIN = 5.0;
	private static final double CAPTURE = 3.0;
	private static final double WILLBECAPTURE = 5.0;
	private static final double NANO = 1000000000.0;
	
	private static final int NEGATIVE = 0;
	private static final int POSITIVE = 1;
	
	private final int minmaxAmount;
	private final int minmaxDepth;
	
	private double timeTaken;
	private Random randomize = new Random();
//	private long lastKey;
	
	
	public Arta(int amount, int depth) {
		this.minmaxAmount = amount;
		this.minmaxDepth = depth;
	}
	
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
	
	private int enemyChain(Gomoku game, int x, int y, int value) {
		int enemyChainLength = 0;
		int enemyChainValue = 0;
		for (int i = 0; i < 4; i++) {
			enemyChainLength = this.checkChain(game, x, enemyChainLength, value, i);
			enemyChainValue = enemyChainValue > enemyChainLength ? enemyChainValue : enemyChainLength;
		}
		return (enemyChainValue);
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
		
//		if (align == AdjacentAlignment.HORIZONTAL) {
			if (direction == Arta.NEGATIVE) {
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
				blocked += checkIfBlocked(game, x - chain.dx, y - chain.dy, value, chainLength, chain, Arta.NEGATIVE);
				tmp += Math.pow(PLAYERCHAIN, chainLength);
			}
			else if (game.getToken(x - chain.dx, y - chain.dy) > 0) {
				chainLength = game.getAdjacentTokenCount(x - chain.dx, y - chain.dy, chain);
				blocked += checkIfBlocked(game, x - chain.dx, y - chain.dy, value, chainLength, chain, Arta.NEGATIVE);
				tmp += Math.pow(ENEMYCHAIN, chainLength);
			}
			if (game.getToken(x + chain.dx, y + chain.dy) == value) {
				chainLength = game.getAdjacentTokenCount(x + chain.dx, y + chain.dy, chain);
				blocked += checkIfBlocked(game, x + chain.dx, y + chain.dy, value, chainLength, chain, Arta.POSITIVE);
				tmp += Math.pow(PLAYERCHAIN, chainLength);
			}
			else if (game.getToken(x + chain.dx, y + chain.dy) > 0) {
				chainLength = game.getAdjacentTokenCount(x + chain.dx, y + chain.dy, chain);
				blocked += checkIfBlocked(game, x + chain.dx, y + chain.dy, value, chainLength, chain, Arta.POSITIVE);
				tmp += Math.pow(ENEMYCHAIN, chainLength);
			}
//			tmp /= blocked;
			tmp -= blocked;
			if (tmp > score) {
				score = tmp;
			}
			blocked = 1;
			tmp = 0;
		}
		return (score);
	}
	
	/**
	 * Creates a copy of the current game state into gameBoard
	 * 
	 * @param game The gomoku game to copy the state from
	 */
	public int[][] copyMoveBoard(int[][] game) {
		int[][] copyBoard = new int[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH];
		for (int x = 0, y = 0; x < Gomoku.BOARD_LENGTH && y < Gomoku.BOARD_LENGTH; x++) {
			copyBoard[x][y] = game[x][y];
			if (x == 18) {
				x = -1;
				y++;
			}
		}
		return (copyBoard);
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
	private List<Play> bestMoves = new ArrayList<Play>();
	
	
	private List<Prediction> getMinimax(List<Prediction> minimax, Gomoku game, int amount, int maxDepth, int currentDepth, int[][] moveBoard, int playerValue) {
//		Prediction finalMove = new Prediction(new Play(0, -1, -1));
		List<int[][]> updatedBoard = new ArrayList<int[][]>();
		System.out.println("Minimax");
		int i = 0;
		List<Play> copyMoves = new ArrayList<Play>(this.bestMoves);
		for (Play move : copyMoves) {
			System.out.println("Inside loop");
			minimax.add(new Prediction(move, playerValues[playerValue % 2]));
			updatedBoard.add(this.copyMoveBoard(moveBoard));
			updatedBoard.get(i)[minimax.get(i).move.x][minimax.get(i).move.y] = playerValues[playerValue % 2];
			System.out.println("Depth : " + currentDepth + " Inserting Piece of X [" + minimax.get(i).move.x + "] Y [" + minimax.get(i).move.y + "] Score - " + minimax.get(i).move.score);
			if (++i >= amount) {
				currentDepth++;
				for (int j = 0; j < amount && currentDepth < maxDepth; j++) {
					System.out.println("Passing into scanBoard X " + minimax.get(j).move.x + " Y " + minimax.get(j).move.y);
					this.scanBoard(game, updatedBoard.get(j), playerValues[playerValue % 2]);
					this.getMinimax(minimax.get(j).nextPlay, game, amount, maxDepth, currentDepth, updatedBoard.get(j), playerValue++);
				}
				break; 
			}
		}
		return (minimax);
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
		return (Math.pow(CAPTURE, game.countCaptures(x, y, this.playerNumber)))
//				- this.captureThreat(game, x, y, value)
				+ (Math.pow(2.5, this.enemyChain(game, x, y, value)))
				- (game.isInDanger(x, y, value) ? 0 : WILLBECAPTURE )
				+ this.checkSurrounding(game, x, y, value);
	}
	
	
	/**
	 * Max length to check for double three is 4
	 * @param x
	 * @param y
	 * @param moveBoard
	 * @param value
	 * @return
	 */
	// AABXPOOXB
	// ABXOPOXBA
	// BXOOPXBAA
	
	// AAAXPXOOX
	// AXOXPOXAA
	// XOXOPXAAA
	
	// AAAXPOXOX
	// AAXOPXOXA
	// XOOXPXAAA
	// B = block or free
	// A = any piece
	// O = same value
	// P = piece places
	
	private static final Set<String> DOUBLETHREE_PERMUTATIONS;
	private static final String[] BASE_TWO = {
		"AABXPOOXB", "ABXOPOXBA", "BXOOPXBAA",
		"AABXPOOXX", "ABXOPOXXA", "BXOOPXXAA",
		"AAXXPOOXB", "AXXOPOXBA", "XXOOPXBAA",
		"AAXXPOOXX", "AXXOPOXXA", "XXOOPXXAA",
	};
	private static final String[] BASE_THREE = {
		"AAAXPXOOX", "AXOXPOXAA", "XOXOPXAAA",
		"AAAXPOXOX", "AAXOPXOXA", "XOOXPXAAA"
	};
	
	static {
		HashSet<String> set = new LinkedHashSet<String>();
		char[] values = { 'B', 'O', 'X' };
		int[] position = { 0, 0 };
		char[] hash = new char[9];
		
		for (int i = 0; i < BASE_TWO.length;) {
			int count = 0;
			for (int x = 0; x < 9; x++) {
				char c = BASE_TWO[i].charAt(x);
				if (c == 'A') {
					hash[x] = values[position[count++]];
				}
				else {
					hash[x] = c;
				}
			}
			
			if (++position[0] > 2) {
				++position[1];
				position[0] = 0;
			}
			if (position[1] > 2) {
				position[1] = 0;
				position[0] = 0;
				i++;
			}
			set.add(new String(hash));
		}
		
		int[] pos = { 0, 0, 0 };
		for (int i = 0; i < BASE_THREE.length;) {
			int count = 0;
			for (int x = 0; x < 9; x++) {
				char c = BASE_THREE[i].charAt(x);
				if (c == 'A') {
					hash[x] = values[pos[count++]];
				}
				else {
					hash[x] = c;
				}
			}
			
			if (++pos[0] > 2) {
				++pos[1];
				pos[0] = 0;
			}
			if (pos[1] > 2) {
				++pos[2];
				pos[1] = 0;
			}
			if (pos[2] > 2) {
				pos[2] = 0;
				pos[1] = 0;
				pos[0] = 0;
				i++;
			}
			set.add(new String(hash));
		}
		
		DOUBLETHREE_PERMUTATIONS = Collections.unmodifiableSet(set);
	}
	
	// BNSAKJFDBASKJD.contains("Sstring");
	
	private boolean safeFromDoubleThrees(int x, int y, int[][] moveBoard, int value) {
		int totalThrees = 0;
		
		for (Alignment align : Alignment.values()) {
			char[] position = new char[9];
			int currentPosition = 4;
			position[currentPosition] = 'P';
			for (int i = 1; i <  4; i++) {
				if ((x - (align.dx * i) >= 0) && (y - (align.dy * i) >= 0) && (x + (align.dx * i) < 19) && (y + (align.dy * i) < 19) && 
						(x + (align.dx * i) >= 0) && (y + (align.dy * i) >= 0) && (x - (align.dx * i) < 19) && (y - (align.dy * i) < 19)){
					if (moveBoard[x - (align.dx * i)][y - (align.dy * i)] == value) {
						position[currentPosition - i] = 'O';
					}
					else if(moveBoard[x - (align.dx * i)][y - (align.dy * i)] > 0 || moveBoard[x - (align.dx * i)][y - (align.dy * i)] == -1) {
						position[currentPosition - i] = 'B';
					}
					else {
						position[currentPosition - i] = 'X';
					}
					
					if (moveBoard[x + (align.dx * i)][y + (align.dy * i)] == value) {
						position[currentPosition + i] = 'O';
					}
					else if(moveBoard[x + (align.dx * i)][y + (align.dy * i)] > 0 || moveBoard[x + (align.dx * i)][y + (align.dy * i)] == -1) {
						position[currentPosition + i] = 'B';
					}
					else {
						position[currentPosition + i] = 'X';
					}
				}
			}

			totalThrees += 1;
		}
		return (totalThrees > 1);
	}
	
	// value = 0, 0 % 2 = 0, player number
	// is value 0 , want enemy, (value + 1) % 2 = enemy number, in this 0 + 1 % 2 = 1
	
	// horizontal (x - 2, x + 1) (x - 1, x + 2)
	// vertical (y - 2, y + 1) (y - 1, y + 2)
	// diagp (x - 2 & y - 2, x + 1 & y + 1) (x - 1 & y - 1, x + 2 & y + 2) 
	// diagn (x - 2 & y + 2, x + 1, y - 1) (x - 1 & y + 1, x + 2, y - 2)
	
	private boolean safeFromCapture(int x, int y, int[][] moveBoard, int value) {
		for (Alignment align : Alignment.values()) {
			if (x - (align.dx * 2) >= 0 && y - (align.dy) >= 0 && x + (align.dx) < 19 && y + (align.dy * 2) < 19 && x - (align.dx) > 0 &&
					x + (align.dx * 2) >= 0 && y + (align.dy) >= 0 && x - (align.dx) < 19 && y - (align.dy * 2) < 19 && x + (align.dx) > 0 &&
					y + (align.dy * 2) >= 0) {
				if (moveBoard[x - (align.dx * 2)][y - (align.dy)] == this.playerValues[(value + 1) % 2] &&
					moveBoard[x + (align.dx)][y + (align.dy * 2)] == this.playerValues[(value + 1) % 2] &&
					moveBoard[x - (align.dx)][y - (align.dy)] == this.playerValues[(value + 1) % 2]) {
						return (false);
				}
			}
			else if (x - (align.dx) >= 0 && y - (align.dy * 2) >= 0 && x + (align.dx * 2) < 19 && y + (align.dy) < 19 && x + (align.dx * 2) > 0 &&
					x + (align.dx) >= 0 && y + (align.dy * 2) >= 0 && x - (align.dx * 2) < 19 && y - (align.dy) < 19 && x - (align.dx * 2) > 0 
					&& y + (align.dy * 2) < 19 && y - (align.dy * 2) < 19) {
				if (moveBoard[x - (align.dx)][y - (align.dy * 2)] == this.playerValues[(value + 1) % 2] &&
					moveBoard[x + (align.dx * 2)][y + (align.dy)] == this.playerValues[(value + 1) % 2] &&
					moveBoard[x + (align.dx)][y + (align.dy)] == this.playerValues[(value + 1) % 2]) {
						return (false);
				}
			}
		}
		return (true);
	}
	
	/**
	 * Will check to see if the piece is free, and if it is placing into a capture or a double three
	 * @param x
	 * @param y
	 * @param moveBoard
	 * @return
	 */
	private boolean isIllegaMove(int x, int y, int[][] moveBoard, int value) {
		return (moveBoard[x][y] == 0 && this.safeFromCapture(x, y, moveBoard, value) && this.safeFromDoubleThrees(x, y, moveBoard, value));
	}
	
	/**
	 * This will scan the entire board of the copied state moveBoard and update a scoreboard to it.
	 * It will also store it into a list that will be sorted and returned
	 */
	private List<Play> scanBoard(Gomoku game, int[][] moveBoard, int value) {
		this.bestMoves.clear();
		for (int x = 0, y = 0; x < Gomoku.BOARD_LENGTH && y < Gomoku.BOARD_LENGTH; x++) {
			if (isIllegaMove(x, y, moveBoard, value)) {
				System.out.println("Legal move " + x + " " + y);
				this.scoreBoard[x][y] = calcValue(game, x, y, value);
				this.bestMoves.add(new Play(this.scoreBoard[x][y], x, y));
			}
			else { System.out.println("AASDASDSDSDSDASDASDASD"); }
			if (x == 18) {
				x = -1;
				y++;
			}
		}
		Collections.sort(this.bestMoves);
		return (this.bestMoves);
	}
	
	
	/**
	 * Creates a copy of the current game state into gameBoard
	 * 
	 * @param game The gomoku game to copy the state from
	 */
	public int[][] copyGameState(Gomoku game) {
		int[][] copyBoard = new int[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH];
		for (int x = 0, y = 0; x < Gomoku.BOARD_LENGTH && y < Gomoku.BOARD_LENGTH; x++) {
			copyBoard[x][y] = game.getToken(x, y);
			if (x == 18) {
				x = -1;
				y++;
			}
		}
		return (copyBoard);
	}
	
	@Override
	public String name(Gomoku game, int value) {
		return ("Arta");
	}

	@Override
	public void report(Gomoku game, String message) {
		System.out.println(message);
		System.err.println("[Arta] Tuturuu~");
	}

	@Override
	public void informChange(Gomoku game, int x, int y, int value) {	
	}

	@Override
	public void informWinner(Gomoku game, int value) {
	}

//	private Random rng = new Random();
	@Override
	public boolean getMove(Gomoku game, int value, long key) {
		long startTime = System.nanoTime();
		if (game.getTurn() == 0) {
			game.submitMove(this.randomize.nextInt(Gomoku.BOARD_LENGTH), this.randomize.nextInt(Gomoku.BOARD_LENGTH), key);
		}
		else {
			System.out.println("Test 1");
			this.scanBoard(game, this.copyGameState(game), playerNumber);
			List<Prediction> moveToPlay = new ArrayList<Prediction>();
			moveToPlay = this.getMinimax(new ArrayList<Prediction>(), game,
					this.minmaxAmount, this.minmaxDepth * Gomoku.PLAYER_COUNT, 0,
					this.copyGameState(game), 0);
//			moveToPlay = this.getMinimax(new ArrayList<Prediction>(), game,
//					this.minmaxAmount, this.minmaxDepth * Gomoku.PLAYER_COUNT, 0,
//					new int[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH], 0);
			System.out.println("Test 2");
			List<Play> sortPlayList = new ArrayList<Play>();
			for (Prediction sortItem : moveToPlay) {
				System.out.println("Sorting");
				sortItem.getTotal();
				sortPlayList.add(new Play(sortItem.totalScore, sortItem.move.x, sortItem.move.y));
			}
			System.out.println("Test 3");
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
			System.out.println("Test 4");
		}
		System.out.println("Test");
		return (true);
//		for (String a : DOUBLETHREE_PERMUTATIONS) {
//			System.out.println(a);
//		}
//		while (true) {
//			int x = rng.nextInt(Gomoku.BOARD_LENGTH);
//			int y = rng.nextInt(Gomoku.BOARD_LENGTH);
//			
//			if (game.getToken(x, y) == 0) {
//				game.submitMove(x, y, key);
//				break;
//			}
//		}
//		return (true);
	}
	
	@Override
	public void gameStart(Gomoku game, int value) {
		if (value == 1) {
			playerNumber = value;
			enemyNumber = value + 1;
		}
		else {
			playerNumber = value;
			enemyNumber = value - 1;
		}
		this.playerValues[0] = playerNumber;
		this.playerValues[1] = enemyNumber;
	}
	
	@Override
	public void gameEnd(Gomoku game) {
	}

	@Override
	public double getTimeElapsed() {
		return (this.timeTaken);
	}
}
