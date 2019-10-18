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
			.get("C:\\Users\\Student_user\\Desktop\\ideal.xml");
	// private long start_time = System.currentTimeMillis();

	public double objectLength = 0.08; //ここが指の長さ　　単位はメートル
	private static String videoPath = "C:\\finger_pencil\\input\\CIMG5896.MOV"; // ここに動画のパス
	private static String filePath1 = "C:\\finger_pencil\\input\\pointA_cood_5896.TXT";//ここに点Aの座標のパス
	private static String filePath2 = "C:\\finger_pencil\\input\\pointB_cood_5896.TXT";//ここに点Bの座標のパス

	private static BufferedWriter br;

	private boolean isRunning = false;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("START");
	}

	 public static void main(String[] args) {
	 new Main(Paths.get(videoPath)).lunchProcess();
	 }

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

//			Mat mat = Imgcodecs.imread(videoPath);
//			Mat matHsv = new Mat();
//			Imgproc.cvtColor(mat, matHsv, Imgproc.COLOR_BGR2HSV);
//			final double objectLength = SubstituteFingerDetector.Diff(matHsv);
			Processer processer = new Processer(videoPath, objectLength, filePath1, filePath2);
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
