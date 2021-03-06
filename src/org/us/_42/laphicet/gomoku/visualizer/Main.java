package org.us._42.laphicet.gomoku.visualizer;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.us._42.laphicet.gomoku.Gomoku;

public class Main {
	static {
		System.setProperty("java.awt.headless", "true");
		if (!(GraphicsEnvironment.isHeadless())) {
			throw new RuntimeException("Application must be run in headless mode!");
		}
	}
	
	/**
	 * Attempts to verify that the program is running on the main JVM thread.
	 * If verification fails or the program is not running on the first thread,
	 * it will launch a new copy of itself on the first thread.
	 * 
	 * The newly launched application inherits IO from the current one, so System.out will stay in tact.
	 */
	private static void XstartOnFirstThread() {
		try {
			String os = System.getProperty("os.name");
			if (!(os.contains("Mac")) && !(os.contains("Darwin"))) {
				return;
			}
			
			long pid = Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
			String env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid);
			if ((env != null) && env.equals("1")) {
				return;
			}
		}
		catch (Exception e) { }
		
		try {
			Process process = new ProcessBuilder(
				String.format("%s%sbin%sjava", System.getProperty("java.home"), File.separator, File.separator),
				"-XstartOnFirstThread",
				"-cp",
				System.getProperty("java.class.path"),
				Main.class.getName(),
				"-ignoreThreadCheck"
			).inheritIO().start();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					process.destroy();
				}
			});
			
			process.waitFor();
			System.exit(process.exitValue());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String... args) {
		if (!(Arrays.asList(args).contains("-ignoreThreadCheck"))) {
			XstartOnFirstThread();
		}
		
		Visualizer vis =  new Visualizer();
		Gomoku game = vis.start();
		while (!(vis.isEnded())) {
			game.auto();
			vis.results(game);
			game.reset();
		}
		vis.end();
	}
}
