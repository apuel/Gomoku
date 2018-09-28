package org.us._42.laphicet.gomoku;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputPlayerController implements PlayerController {
	Scanner scanner = new Scanner(System.in);
	
	@Override
	public String Name() {
		return ("User");
	}
	
	@Override
	public void Report(String message) {
		System.err.println(message);
	}
	
	@Override
	public void GetMove(GameController game, byte piece, int[] coords) {
		try {
			System.out.print("Next move ('x y'): ");
			String input = this.scanner.nextLine();
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
