package com.github.smk7758.GetSubstituteFingerPoint;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Main {
	private static final boolean DEBUG_MODE = true;
	private final String picPathString = "F:\\FingerPencil\\TokaiFesta\\2.png";

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		new Main().processer();
	}

	public void processer() {
		if (!Files.exists(Paths.get(picPathString))) {
			System.err.println("File do not exsits.");
			return;
		}

		Mat srcImage = Imgcodecs.imread(picPathString);

		Mat srcHsvImage = new Mat();
		Imgproc.cvtColor(srcImage, srcHsvImage, Imgproc.COLOR_BGR2HSV);

		Mat outputImage = srcImage.clone();
		final Optional<Point> fingerPoint = SubstituteFingerDetector.getSubstituteFingerPoint(srcHsvImage, outputImage);

		if (!fingerPoint.isPresent()) {
			System.err.println("Cannot get finger point.");
			return;
		}

		Imgproc.circle(outputImage, fingerPoint.get(), 5, new Scalar(0, 255, 0), -1, Imgproc.LINE_8);

		Imgcodecs.imwrite(FileIO.getFilePath(picPathString, "test", "png"), outputImage);
		System.out.println("FINISH!");
	}

	public enum LogLevel {
		ERROR, WARN, INFO, DEBUG
	}

	public static void debugLog(String message, LogLevel logLevel, String fromSuffix) {
		debugLog(message + " @" + fromSuffix, logLevel);
	}

	public static void debugLog(String message, LogLevel logLevel) {
		if (DEBUG_MODE) System.out.println("[" + logLevel.toString() + "] " + message);
	}
}
