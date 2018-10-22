package org.us._42.laphicet.gomoku;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputPlayerController implements PlayerController {
	private static final Scanner scanner = new Scanner(System.in);
	
	@Override
	public String name(byte value) {
		return ("User " + value);
	}
	
	@Override
	public void report(String message) {
		System.err.println(message);
		System.err.flush();
	}
	
	@Override
	public void informMove(int x, int y, byte value) { }
	
	@Override
	public void getMove(GameController game, byte piece, int[] coords) {
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
