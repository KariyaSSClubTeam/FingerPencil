package com.github.smk7758.FingerPencil_QuickLoad;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opencv.core.Core;

import com.github.smk7758.FingerPencil.FileIO;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	public static final String SOFTWARE_NAME = "FingerPencil_QuickLoad";
	public static boolean DEBUG_MODE = true;
	public static boolean FILE_LOGGING = false;
	private final static String logFilePath = "";
	private static BufferedWriter br;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		if (FILE_LOGGING) {
			try {
				br = Files.newBufferedWriter(Paths.get(FileIO.getFilePath(logFilePath, "log", "txt")));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		launch(args);

		if (FILE_LOGGING) {
			try {
				br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			Scene scene = new Scene(FXMLLoader.load(getClass().getResource("FingerPencil_QuickLoad.fxml")));
			// Set Title
			primaryStage.setTitle(SOFTWARE_NAME);
			// Set Window
			primaryStage.setResizable(false);
			// Set Scene
			primaryStage.setScene(scene);
			primaryStage.show();
			// primaryStage.onShownProperty();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public enum LogLevel {
		ERROR, WARN, INFO, DEBUG
	}

	public static void printDebug(String message, LogLevel logLevel, String fromSuffix) {
		printDebug(message + " @" + fromSuffix, logLevel);
	}

	public static void printDebug(String message, LogLevel logLevel) {
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
