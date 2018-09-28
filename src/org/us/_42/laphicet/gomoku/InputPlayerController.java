package org.us._42.laphicet.gomoku;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputPlayerController implements PlayerController {
	private static final Scanner scanner = new Scanner(System.in);
	private int id = 1;
	
	@Override
	public String Name() {
		return ("User " + this.id);
	}
	
	@Override
	public void Report(String message) {
		System.err.println(message);
		System.err.flush();
	}
	
	@Override
	public void GetMove(GameController game, byte piece, int[] coords) {
		this.id = piece;
		
		System.out.print("Next move ('x y'): ");
		try {
			String input = scanner.nextLine();
			String[] inputs = input.split(" ");
			if (inputs.length != 2) {
				coords[0] = -1;
				coords[1] = -1;
			}
			else {
				try {
					coords[0] = Integer.parseInt(inputs[0]);
					coords[1] = Integer.parseInt(inputs[1]);
				}
				catch (NumberFormatException e) {
					coords[0] = -1;
					coords[1] = -1;
				}
			}
		}
		catch (NoSuchElementException | IllegalStateException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
