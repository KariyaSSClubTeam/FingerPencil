package com.github.smk7758.FingerPencil;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import com.github.smk7758.FingerPencil.Main.LogLevel;
import com.github.smk7758.FingerPencil.Detector.MarkerDetector;
import com.github.smk7758.FingerPencil.Detector.SubstituteFingerDetector;

public class Processer {
	private final String VIDEO_PATH;
	private final double objectLength;
	final Dictionary MARKER_DICTIONARY = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);

	Mat homographyMatrix = null;
	CameraParameter cameraParameter;
	CalculateTouchPoint calculatePoint;

	public Processer(String videoPath, double objectLength) {
		cameraParameter = new CameraParameter(Main.cameraParameterFilePath);

		this.VIDEO_PATH = videoPath;
		this.objectLength = objectLength;
		calculatePoint = new CalculateTouchPoint(cameraParameter);
	}

	public void run() {
		VideoCapture vc = createVideoCreater();
		VideoWriter vw = createVideoWriter(vc);

		Mat image = new Mat();
		Mat firstImage = null;
		List<Point> fingerPoints = new ArrayList<>(); // 入力動画上の座標の集まり。
		List<double[]> perspectedPoints = new ArrayList<>(); // 出力用画像上の座標の集まり。

		while (vc.isOpened() && vc.read(image)) {
			if (firstImage == null) firstImage = image.clone();
			boolean test = loop(vc, image, firstImage, fingerPoints, perspectedPoints, vw);
			if (!test) break;
		}

		outputFingerPointsImage(firstImage, fingerPoints);
		outputProspectedPointsImage(perspectedPoints);
		outputProspectedPointsText(perspectedPoints);

		vc.release();
		vw.release();

		System.out.println("FINISH!!");
	}

	// false -> 続行不能。
	// true -> 続行可能。
	public boolean loop(VideoCapture vc, Mat mat, Mat firstImage, List<Point> fingerPoints,
			List<double[]> perspectedPoints, VideoWriter vw) {

		Mat outputImage = mat.clone();

		Mat markerIds = new Mat();
		ListMap<Point, Integer> detectedMarkerPoints = new ListMap<>();
		ListMap<Mat, Mat> markerParameters = new ListMap<>();
		MarkerDetector.process(mat, outputImage, MARKER_DICTIONARY,
				cameraParameter.cameraMatrix, cameraParameter.distortionCoefficients,
				markerIds, detectedMarkerPoints, markerParameters);

		if (detectedMarkerPoints.size() < 4) {
			System.err.println("Cannot detect all of markers. Only detected: " + detectedMarkerPoints.size());
		} else {
			Optional<Mat> homographyMatrix_tmp = Transformer.getHomographyMatrix(detectedMarkerPoints);
			if (homographyMatrix_tmp.isPresent()) {
				homographyMatrix = homographyMatrix_tmp.get();
			} else {
				System.err.println("Cannot get homography matrix.");
			}
		}
		if (homographyMatrix == null || homographyMatrix.empty()) {
			System.err.println("There is no homographyMatrix. -> Skip.");
			return true;
		}

		Mat matHsv = new Mat();
		Imgproc.cvtColor(mat, matHsv, Imgproc.COLOR_BGR2HSV); // convert BGR -> HSV

		// 指の取得 (ここから)
		// final Optional<Point> fingerPoint = getFingerPoint(firstMat, matHsv);
		// 指の点の取得 (ここまでで)

		// 指取得(代替) - ここから
		final Optional<Entry<Point, Point>> fingerPoint = SubstituteFingerDetector.getSubstituteFingerPoint(matHsv,
				outputImage);
		// 指取得(代替) - ここまで

		if (!fingerPoint.isPresent() || Double.isNaN(fingerPoint.get().getKey().x)
				|| Double.isNaN(fingerPoint.get().getKey().y)) {
			Main.debugLog("Cannot get finger point.", LogLevel.ERROR, "Processer");
			return true;
		}

		fingerPoints.add(fingerPoint.get().getKey());

		Imgproc.circle(outputImage, fingerPoint.get().getKey(), 5, new Scalar(0, 255, 0), -1, Imgproc.LINE_8);

		System.out.println("getMarkerIdIndex: " + MarkerDetector.getMarkerIdIndex(markerIds, 2)); // TODO

		Main.debugLog("Start CalculatePoint.", LogLevel.INFO, "Processer");
		Entry<Mat, Mat> markerParameter = markerParameters.get(MarkerDetector.getMarkerIdIndex(markerIds, 2));
		Entry<Point, Point> fingerPointsProjected = calculatePoint.process(fingerPoint.get(),
				markerParameter.getKey(), markerParameter.getValue(), objectLength, outputImage);

		Imgproc.circle(outputImage, fingerPointsProjected.getKey(), 100, new Scalar(0, 10, 100), -1);
		Imgproc.circle(outputImage, fingerPointsProjected.getValue(), 100, new Scalar(100, 10, 0), -1);

		Main.debugLog("End CalculatePoint.", LogLevel.INFO, "Processer");

		// 透視変換
		Mat perspectedImage = Transformer.transformPoint(fingerPoint.get().getKey(), homographyMatrix,
				firstImage.size());
		Optional<List<double[]>> perspectedPoints_tmp = ImgProcessUtil.getCenterPointContrus(perspectedImage);
		if (!perspectedPoints_tmp.isPresent()) {
			System.err.println("Cannot transform point.");
		} else {
			final double[] perspectedPoint = perspectedPoints_tmp.get().get(0); // get largest
			perspectedPoints.add(perspectedPoint);
		}

		vw.write(outputImage);
		return true;
	}

	public VideoCapture createVideoCreater() {
		VideoCapture vc = new VideoCapture();
		{
			// 入力動画の初期化
			vc.open(VIDEO_PATH);
		}
		return vc;
	}

	public VideoWriter createVideoWriter(VideoCapture vc) {
		VideoWriter vw = new VideoWriter();
		{
			// 出力動画ファイルの初期化
			// avi -> 'M', 'J', 'P', 'G'
			// mp4 -> 32
			vw.open(FileIO.getFilePath(VIDEO_PATH, "finger_points", "mp4"), 32, 29,
					new Size(vc.get(Videoio.CV_CAP_PROP_FRAME_WIDTH), vc.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT)));
		}
		return vw;
	}

	public void outputProspectedPointsText(List<double[]> perspectedPoints) {
		FileIO.exportList(Paths.get(FileIO.getFilePath(VIDEO_PATH, "perspected_points", "txt")), perspectedPoints);
	}

	public void outputFingerPointsImage(Mat firstImage, List<Point> fingerPoints) {
		final Mat outputFingerPointsMat = firstImage.clone();
		fingerPoints.forEach(point -> Imgproc.circle(outputFingerPointsMat,
				point, 3, new Scalar(0, 255, 0), -1, Imgproc.LINE_8));

		Imgcodecs.imwrite(FileIO.getFilePath(VIDEO_PATH, "finger_points", "jpg"), outputFingerPointsMat);
	}

	public void outputProspectedPointsImage(List<double[]> perspectedPoints) {
		final Mat outputPerspectedMat = Mat.zeros(new Size(600, 600), CvType.CV_16S);
		perspectedPoints.forEach(point -> Imgproc.circle(outputPerspectedMat,
				new Point(point), 1, new Scalar(255), -1, Imgproc.LINE_8));

		Imgcodecs.imwrite(FileIO.getFilePath(VIDEO_PATH, "perspected_points", "jpg"), outputPerspectedMat);
	}
}
