package com.github.smk7758.FingerPencil_QuickLoad;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.smk7758.FingerPencil_QuickLoad.Main.LogLevel;

import javafx.animation.AnimationTimer;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class RecordDispatcher {
	AnimationTimer imageAnimation = null;
	RecordService recordService = null;
	boolean hadBeenRecording = false;
	long startMillSec = 0;

	private Path videoOutputFolderPath = null;

	public RecordDispatcher(TextField videoOutputFolderPath, TextField cameraID, ImageView imageView, Text recSecText) {
		if (videoOutputFolderPath.getText().isEmpty()) {
			Main.printDebug("VideoOutputPath is empty.", LogLevel.ERROR);
			return;
		}

		Main.printDebug("videoOutputFolderPath: " + Paths.get(videoOutputFolderPath.getText()).toString(),
				LogLevel.DEBUG); // TODO

		try {
			short cameraID_ = Short.valueOf(cameraID.getText());
			recordService = new RecordService(Paths.get(videoOutputFolderPath.getText()), cameraID_);
		} catch (NumberFormatException ex) {
			Main.printDebug("CameraID is not short number type.", LogLevel.INFO);
			recordService = new RecordService(Paths.get(videoOutputFolderPath.getText()));
		}

		imageAnimation = new AnimationTimer() {
			@Override
			public void handle(long now) {
				imageView.setImage(recordService.getLastValue());
				recSecText.setText((int) ((System.currentTimeMillis() - startMillSec) / 1000) + " (s)");
			}
		};
	}

	// Debug用
	public RecordDispatcher(Path videoOutputFolderPath) {
		this.videoOutputFolderPath = videoOutputFolderPath;

		Main.printDebug("videoOutputFolderPath: " + videoOutputFolderPath.toString(), LogLevel.DEBUG);

		hadBeenRecording = true;
	}

	public void startRecord() {
		imageAnimation.start();
		recordService.start();

		startMillSec = System.currentTimeMillis();
		Main.printDebug("started REC", LogLevel.DEBUG);
	}

	public void stopRecord(ImageView imageView) {
		Main.printDebug("stopButton", LogLevel.DEBUG);

		if (isNotRecording()) {
			Main.printDebug("It is not Recording yet. You Must start recording.", LogLevel.ERROR);
			return;
		}

		imageAnimation.stop();
		recordService.cancel();

		imageView.setImage(null);
		imageAnimation = null;
		hadBeenRecording = true;
	}

	public boolean isNotRecording() {
		return (imageAnimation == null || hadBeenRecording);
	}

	public Path getVideoOutputFilePath() {
		return (recordService != null) ? recordService.getVideoOutputFilePath() : videoOutputFolderPath;
		// Debug用のやつ込み(ちゃんと動いて？)
	}
}
