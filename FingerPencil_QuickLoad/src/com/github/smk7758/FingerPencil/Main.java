package com.github.smk7758.FingerPencil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.opencv.core.Core;

public class Main {
	public static boolean DEBUG_MODE = true;
	private static final boolean FILE_LOGGING = true;
	public static final Path cameraParameterFilePath = Paths
			.get("F:\\FingerPencil\\TokaiFesta\\CC\\CameraCalibration_2019-07-12.xml");
	// private long start_time = System.currentTimeMillis();

	public final double objectLength = 60; // 長さ60mm //TODO
	private String videoPath = "S:\\FingerPencil\\touchPoint\\CIMG1780.MOV"; // TODO
	private static BufferedWriter br;

	private boolean isRunning = false;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("START");
	}

	// public static void main(String[] args) {
	// new Main().lunchProcesser();
	// }

	public Main(Path videoPath) {
		this.videoPath = videoPath.toString();
	}

	public void lunchProcess() {
		isRunning = true;
		if (Files.exists(Paths.get(videoPath))) {

			if (FILE_LOGGING) {
				try {
					br = Files.newBufferedWriter(Paths.get(FileIO.getFilePath(videoPath, "log", "txt")));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			Processer processer = new Processer(videoPath, objectLength);
			processer.run();

			if (FILE_LOGGING) {
				try {
					br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			System.err.println("Cannot lunch the first process because of unexisting video file.");
		}
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public enum LogLevel {
		ERROR, WARN, INFO, DEBUG
	}

	public static void debugLog(String message, LogLevel logLevel, String fromSuffix) {
		debugLog(message + " @" + fromSuffix, logLevel);
	}

	public static void debugLog(String message, LogLevel logLevel) {
		if (logLevel.equals(LogLevel.DEBUG) && !DEBUG_MODE) return;

		if (FILE_LOGGING) {
			try {
				br.write("[" + logLevel.toString() + "] " + message + System.lineSeparator());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println("[" + logLevel.toString() + "] " + message);
		}
	}
}
