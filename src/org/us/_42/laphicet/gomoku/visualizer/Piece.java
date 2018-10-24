package org.us._42.laphicet.gomoku.visualizer;

import java.util.Objects;

/**
 * Piece - An object that stores piece information for the visualizer
 * 
 * @author mlu & apuel
 */
public class Piece {
	public final int x;
	public final int y;
	public final int player;
	
	/**
	 * Constructor that stores values for the piece
	 * 
	 * @param x Horizontal position on the board
	 * @param y Vertical position on the board
	 * @param player The player that placed the piece
	 */
	public Piece(int x, int y, int player) {
		this.x = x;
		this.y = y;
		this.player = player;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Piece)) {
			return (false);
		}
		return (((Piece)o).x == this.x && ((Piece)o).y == this.y);
	}
	
	@Override
	public int hashCode() {
		return (Objects.hash(x, y));
	}
}
