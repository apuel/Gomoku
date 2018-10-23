package org.us._42.laphicet.gomoku.visualizer;

import java.util.Objects;

public class Piece {
	public final int x;
	public final int y;
	public final int player;
	
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
