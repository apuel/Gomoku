package org.us._42.laphicet.gomoku;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputPlayerController implements PlayerController {
	private static final Scanner scanner = new Scanner(System.in);
	private int id = 1;
	
	@Override
	public String name() {
		return ("User " + this.id);
	}
	
	@Override
	public void report(String message) {
		System.err.println(message);
		System.err.flush();
	}
	
	@Override
	public void informTurn(int x, int y, byte value) { }
	
	@Override
	public void getMove(GameController game, byte piece, int[] coords) {
		this.id = piece;
		
		System.out.print("Next move ('x y'): ");
		try {
			String input = scanner.nextLine();
			String[] inputs = input.split(" ");
			if (inputs.length >= 2) {
				try {
					int x = Integer.parseInt(inputs[0]);
					int y = Integer.parseInt(inputs[1]);
					coords[0] = x;
					coords[1] = y;
				}
				catch (NumberFormatException e) { }
			}
		}
		catch (NoSuchElementException | IllegalStateException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
