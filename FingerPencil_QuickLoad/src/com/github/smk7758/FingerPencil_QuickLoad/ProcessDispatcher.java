package com.github.smk7758.FingerPencil_QuickLoad;

import java.nio.file.Path;

import com.github.smk7758.FingerPencil.Main;

public class ProcessDispatcher {
	private final Path videoOutputFilePath;
	private Main main = null;

	public ProcessDispatcher(Path videoOutputFilePath) {
		this.videoOutputFilePath = videoOutputFilePath;
	}

	public void start() {
		main = new Main(videoOutputFilePath);
		main.lunchProcess();
	}

	public boolean isRunning() {
		return (main != null && main.isRunning()) ? true : false;
	}
}
