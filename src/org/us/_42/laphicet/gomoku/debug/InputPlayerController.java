package org.us._42.laphicet.gomoku.debug;

import java.util.NoSuchElementException;
import java.util.Scanner;

import org.us._42.laphicet.gomoku.Gomoku;
import org.us._42.laphicet.gomoku.PlayerController;

public class InputPlayerController implements PlayerController {
	private static final Scanner scanner = new Scanner(System.in);
	
	@Override
	public String name(int value) {
		return ("User " + value);
	}
	
	@Override
	public void report(Gomoku game, String message) {
		System.err.println(message);
		System.err.flush();
	}
	
	@Override
	public void informChange(Gomoku game, int x, int y, int value) { }
	
	@Override
	public void informWinner(Gomoku game, int value) { }
	
	@Override
	public boolean getMove(Gomoku game, int piece, long key) {
		System.out.print("Next move ('x y'): ");
		try {
			String input = scanner.nextLine();
			String[] inputs = input.split(" ");
			if (inputs.length >= 2) {
				try {
					int x = Integer.parseInt(inputs[0]);
					int y = Integer.parseInt(inputs[1]);
					game.submitMove(x, y, key);
				}
				catch (NumberFormatException e) {
					this.report(game, "Failed to parse coordinates from input!");
					return (false);
				}
			}
		}
		catch (NoSuchElementException | IllegalStateException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return (true);
	}
}
