package com.github.smk7758.FingerPencil_QuickLoad;

import java.nio.file.Paths;

import com.github.smk7758.FingerPencil_QuickLoad.Main.LogLevel;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class Controller {
	@FXML
	TextField cameraID, videoOutputFolderPath, filePath;
	@FXML
	ImageView imageView;
	@FXML
	Text recSecText;
	@FXML
	Button recButton, startButton;
	@FXML
	CheckBox isUseFilePath;

	RecordDispatcher recordDispatcher = null;
	ProcessDispatcher processDispatcher = null;
	private boolean DEBUG_PROCESS = false;

	@FXML
	public void initialize() {
	}

	@FXML
	public void onRecButton() {
		Main.printDebug("RecButton", LogLevel.DEBUG);

		if (videoOutputFolderPath.getText().isEmpty()) {
			Main.printDebug("Please write VideoOutputFolderPath.", LogLevel.ERROR);
			return;
		}

		if (recordDispatcher == null || recordDispatcher.isNotRecording()) {
			Main.printDebug("START REC", LogLevel.DEBUG);

			recordDispatcher = new RecordDispatcher(videoOutputFolderPath, cameraID, imageView, recSecText);
			recordDispatcher.startRecord();

			recButton.setText("Stop REC");
		} else {
			recordDispatcher.stopRecord(imageView);
			filePath.setText(recordDispatcher.getVideoOutputFilePath().toString());

			recButton.setText("REC");
		}
	}

	@FXML
	public void onStartButton() {
		if (DEBUG_PROCESS) {
			recordDispatcher = new RecordDispatcher(Paths.get("S:\\program\\TEST_\\CIMG1780.MOV"));
		} else if (isUseFilePath.isSelected()) {
			recordDispatcher = new RecordDispatcher(Paths.get(convertToValidPathString(filePath.getText())));
		}

		if (recordDispatcher == null) {
			Main.printDebug("You cannot start because of: recordDispatcher is null.", LogLevel.ERROR);
			return;
		} else if (!recordDispatcher.isNotRecording()) {
			Main.printDebug("It is still recording.", LogLevel.ERROR);
			return;
		}

		Main.printDebug("VideoOutputFilePath: " + recordDispatcher.getVideoOutputFilePath().toString(), LogLevel.DEBUG);

		if (processDispatcher == null) {
			processDispatcher = new ProcessDispatcher(recordDispatcher.getVideoOutputFilePath());
			processDispatcher.start();
		} else if (!processDispatcher.isRunning()) {
			Main.printDebug("The process has been finished!", LogLevel.INFO);

			processDispatcher = null;
		} else {
			// 実行しているよ！(→ 強制終了？)
			Main.printDebug("Please wait to complete process.", LogLevel.INFO);
		}
	}

	private String convertToValidPathString(String path) {
		path = path.trim();

		if (path.startsWith("\"") && path.endsWith("\"")) {
			path = path.substring(1, path.length() - 1);
		}

		return path;
	}
}
