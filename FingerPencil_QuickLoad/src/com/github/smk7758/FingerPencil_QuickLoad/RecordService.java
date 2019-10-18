package com.github.smk7758.FingerPencil_QuickLoad;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import com.github.smk7758.FingerPencil_QuickLoad.Main.LogLevel;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;

public class RecordService extends ScheduledService<Image> {
	static final int provisionalCameraNumber = 0;
	VideoCapture vc = null;
	VideoWriter vw = null;
	int cameraNumber = 0;
	Path videoOutputFolderPath;
	Path videoOutputFilePath = null;
	Mat inputImage = new Mat();

	public RecordService(Path videoOutputFolderPath, int cameraNumber) {
		this.cameraNumber = cameraNumber;
		this.videoOutputFolderPath = videoOutputFolderPath;
		initialize();

		this.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				close();
			}
		});
	}

	public RecordService(Path videoOutputPath) {
		this(videoOutputPath, provisionalCameraNumber);
	}

	public void initialize() {
		vc = new VideoCapture();
		vc.open(cameraNumber);
		vw = new VideoWriter();
		// 出力動画ファイルの初期化
		// avi -> 'M', 'J', 'P', 'G'
		// mp4 -> 32
		// TODO: FileIO.getFilePathはFileのみでFolderに対応していない！

		videoOutputFilePath = Paths.get(videoOutputFolderPath.toString(),
				"rec_" + System.currentTimeMillis() + ".mp4");

		Main.printDebug("videoOutputFilePath: " + videoOutputFilePath.toString(), LogLevel.DEBUG);

		vw.open(videoOutputFilePath.toString(), 32, 29,
				new Size(vc.get(Videoio.CV_CAP_PROP_FRAME_WIDTH), vc.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT)));

		Main.printDebug("initialize", LogLevel.DEBUG);
	}

	private void close() {
		vc.release();
		vw.release();

		vc = null;
		vw = null;
		Main.printDebug("close: RecordService.", LogLevel.DEBUG);
	}

	// @Override
	// public void reset() {
	// super.reset();
	//
	// initialize();
	// }

	@Override
	public boolean cancel() {
		Main.printDebug("Service is stopped.", LogLevel.DEBUG);
		return super.cancel();
	}

	@Override
	protected Task<Image> createTask() {
		return new Task<Image>() {
			@Override
			protected Image call() throws Exception {
				if (!vc.isOpened()) {
					Main.printDebug("VC is not opened.", LogLevel.ERROR);
					this.cancel();
					return null;
				}

				if (!vc.read(inputImage)) {
					Main.printDebug("Cannot load camera image.", LogLevel.DEBUG);
					this.cancel();
					return null;
				}

				vw.write(inputImage);

				return convertMatToImage(inputImage);
			}
		};
	}

	private Image convertMatToImage(Mat inputImage) {
		MatOfByte byte_mat = new MatOfByte();
		Imgcodecs.imencode(".bmp", inputImage, byte_mat);

		return new Image(new ByteArrayInputStream(byte_mat.toArray()));
	}

	public Path getVideoOutputFilePath() {
		return videoOutputFilePath;
	}
}