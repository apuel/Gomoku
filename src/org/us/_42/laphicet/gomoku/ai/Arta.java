package org.us._42.laphicet.gomoku.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.PlayerController;
import org.us._42.laphicet.gomoku.visualizer.Piece;

public class Arta implements PlayerController {

	private static class Play {
		private double score;
		private final int x;
		private final int y;
		
		private Play(double score, int x, int y) {
			this.score = score;
			this.x = x;
			this.y = y;
		}
		
		private double getScore() {
			return (this.score);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Play)) {
				return (false);
			}
			return (((Play)o).x == this.x && ((Play)o).y == this.y);
		}
		
		@Override
		public int hashCode() {
			return (Objects.hash(x, y));
		}
		
	}

	private double[][] scoreBoard = new double[Gomoku.BOARD_LENGTH][Gomoku.BOARD_LENGTH];
	private Set<Play> bestMoves = new HashSet<Play>();
	
	private Random test = new Random();
	
	private double calcValue() {
		return (test.nextInt(100));
	}
	
	private void scanBoard() {
		for (int x = 0, y = 0; x < Gomoku.BOARD_LENGTH && y < Gomoku.BOARD_LENGTH; x++) {
			this.scoreBoard[x][y] = calcValue();
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
	}

	@Override
	public void informChange(Gomoku game, int x, int y, int value) {	
	}

	@Override
	public void informWinner(Gomoku game, int value) {
	}

	@Override
	public boolean getMove(Gomoku game, int value, long key) {
		scanBoard();
		return (true);
	}
	
	@Override
	public void gameStart(Gomoku game, int value) {
	}
	
	@Override
	public void gameEnd(Gomoku game) {
	}
}
