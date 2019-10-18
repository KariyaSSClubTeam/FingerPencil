<<<<<<< HEAD
package com.github.smk7758.FingerPencil;

import java.nio.file.Paths;
import java.util.AbstractMap;
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
import com.github.smk7758.FingerPencil.Detector.PointIO;

public class Processer {
	private final String VIDEO_PATH;
	private final String File_Path1;
	private final String File_Path2;
	private double objectLength;
	final Dictionary MARKER_DICTIONARY = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);

	Mat homographyMatrix = null;
	CameraParameter cameraParameter;
	CalculateTouchPoint calculatePoint;

	public Processer(String videoPath, double objectLength, String filePath1, String filePath2) {
		cameraParameter = new CameraParameter(Main.cameraParameterFilePath);

		this.File_Path1 = filePath1;
		this.File_Path2 = filePath2;
		this.VIDEO_PATH = videoPath;
		this.objectLength = objectLength;
		calculatePoint = new CalculateTouchPoint(cameraParameter);
	}

	public void run() {
		VideoCapture vc = createVideoCreater();
		VideoWriter vw = createVideoWriter(vc);
		VideoWriter vw2 = createVideoWriter(vc);

		Mat image = new Mat();
		Mat firstImage = null;
		List<Point> fingerPoints = new ArrayList<>(); // 入力動画上の座標の集まり。
		List<double[]> perspectedPoints = new ArrayList<>(); // 出力用画像上の座標の集まり。
		List<double[]> perspectedPointsall = new ArrayList<>();

		int i = 1;
		while (vc.isOpened() && vc.read(image)) {
			if (firstImage == null) firstImage = image.clone();
			boolean test = loop(vc, image, firstImage, fingerPoints, perspectedPoints, perspectedPointsall, vw,
					File_Path1, File_Path2, i);
			if (!test) break;
			i = i + 1;
		}

		outputFingerPointsImage(firstImage, fingerPoints);
		outputProspectedPointsImage(perspectedPoints);
		outputProspectedPointsText(perspectedPoints);

		vc.release();
		vw.release();

		System.out.println("FINISH!!");
	}

	// false -> 続行不可能。
	// true -> 続行可能。
	public boolean loop(VideoCapture vc, Mat mat, Mat firstImage, List<Point> fingerPoints,
			List<double[]> perspectedPoints, List<double[]> perspectedPointsall, VideoWriter vw, String filePath1,
			String filePath2, int i) {

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
		// final Optional<Entry<Point, Point>> fingerPoint = SubstituteFingerDetector.getSubstituteFingerPoint(matHsv,
		// outputImage);

		Point pointRed = PointIO.loadRedPoint(filePath1, i);
		Point pointBlue = PointIO.loadBluePoint(filePath2, i);
		final Optional<Entry<Point, Point>> fingerPoint = Optional
				.of(new AbstractMap.SimpleEntry<Point, Point>(pointRed, pointBlue));

//		if (!fingerPoint.isPresent() || Double.isNaN(fingerPoint.get().getKey().x)
//				|| Double.isNaN(fingerPoint.get().getKey().y)) {
//			Main.debugLog("Cannot get finger point.", LogLevel.ERROR, "Processer");
//			return true;
		if (fingerPoint.get().getKey() == null || fingerPoint.get().getValue() == null) {
			Main.debugLog("Cannot get finger point.", LogLevel.ERROR, "Processer");
			return true;
		}

		fingerPoints.add(fingerPoint.get().getKey());

		Imgproc.circle(outputImage, fingerPoint.get().getKey(), 5, new Scalar(0, 255, 0), -1, Imgproc.LINE_8);

		System.out.println("getMarkerIdIndex: " + MarkerDetector.getMarkerIdIndex(markerIds, 2)); // TODO

		Main.debugLog("Start CalculatePoint.", LogLevel.INFO, "Processer");

		final int markerIdIndex = MarkerDetector.getMarkerIdIndex(markerIds, 2);
		if (markerIdIndex == -1) {
			Main.debugLog("Cannot detect marker (id=2).", LogLevel.ERROR);
			return true;
		}
		Entry<Mat, Mat> markerParameter = markerParameters.get(markerIdIndex);
		Entry<Point, Point> fingerPointsProjected = calculatePoint.process(fingerPoint.get(),
				markerParameter.getKey(), markerParameter.getValue(), objectLength, outputImage);

		Imgproc.circle(outputImage, fingerPointsProjected.getKey(), 100, new Scalar(0, 10, 100), -1);
		Imgproc.circle(outputImage, fingerPointsProjected.getValue(), 100, new Scalar(100, 10, 0), -1);

		Main.debugLog("End CalculatePoint.", LogLevel.INFO, "Processer");

		// 透視変換
		// Mat perspectedImageall = Transformer.transformPoint(fingerPoint.get().getKey(), homographyMatrix,
		// firstImage.size());
		// Optional<List<double[]>> perspectedPointsall_tmp = ImgProcessUtil.getCenterPointContrus(perspectedImageall);
		// if (!perspectedPointsall_tmp.isPresent()) {
		// System.err.println("Cannot transform point.");
		// } else {
		// final double[] perspectedPointall = perspectedPointsall_tmp.get().get(0); // get largest
		// perspectedPointsall.add(perspectedPointall);
		// }

		Main.debugLog("Bdiff_processer: " +calculatePoint.BdiffSize_(fingerPoint.get(),
				markerParameter.getKey(), markerParameter.getValue(), objectLength, outputImage) , LogLevel.DEBUG, "loop | Processer");
		Mat R1 = new Mat(new Size(2, 2),CvType.CV_64FC1);
		Mat R2 = R1.clone();
		R1.put(0, 0, new double[] {0,-1});
		R1.put(1, 0, new double[] {1,0});
		R2.put(0, 0, new double[] {-1,0});
		R2.put(1, 0, new double[] {0,-1});

		Main.debugLog("π/2R:"+R1.dump(), LogLevel.DEBUG);
		if (calculatePoint.BdiffSize_(fingerPoint.get(),
				markerParameter.getKey(), markerParameter.getValue(), objectLength, outputImage) < 45) {//この１９０が接地判定の基準（閾値的な）大きくすると点が増えます
			Mat perspectedImage = Transformer.transformPoint(fingerPoint.get().getKey(), homographyMatrix,
					firstImage.size());
			Optional<List<double[]>> perspectedPoints_tmp = ImgProcessUtil.getCenterPointContrus(perspectedImage);
			if (!perspectedPoints_tmp.isPresent()) {
				System.err.println("Cannot transform point.");
			} else {

//				if (perspectedPoints.size() < 0){
//					return true;
//				}
					double[] perspectedPoint = perspectedPoints_tmp.get().get(0); // get largest


//				ここから回転
//				Mat pp = new Mat(new Size(1,2),R1.type());
//				Mat dst1 = pp.clone();
//				Mat dst2 = pp.clone();
//				pp.put(0, 0, perspectedPoint);
//				System.out.println("pp:"+pp.dump());
//				Core.gemm(R1, pp, 1, new Mat(), 1, dst1);
////				Core.gemm(R2, dst, 1, new Mat(), 1, dst);
////				Core.rotate(pp, dst1, Core.ROTATE_90_CLOCKWISE);
//				Core.flip(dst1, dst2, 1);
////				System.out.println("dst::"+dst.dump());
////				System.out.println("dst:"+dst.get(0,0)[0]+","+dst.get(0, 0)[1]);
//
//				String tmp = dst2.dump().replace("[", "");
//				tmp = tmp.replace("]", "");
//				System.out.println(tmp);
//				String[] output = tmp.split(";");
//				perspectedPoint = new double[] {Double.parseDouble(output[0])/*+600*/,Double.parseDouble(output[1])};
//				ここまで回転


				perspectedPoints.add(perspectedPoint);
			}
		}else {
			perspectedPoints.add( new double[] {0.0,0.0});
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
		fingerPoints.forEach(point -> {
			System.out.println(point);
			Imgproc.circle(outputFingerPointsMat,point, 1, new Scalar(0, 255, 0), -1, Imgproc.LINE_8);

		});


		Imgcodecs.imwrite(FileIO.getFilePath(VIDEO_PATH, "finger_points", "jpg"), outputFingerPointsMat);
	}

	public void outputProspectedPointsImage(List<double[]> perspectedPoints) {
		final Mat outputPerspectedMat = Mat.zeros(new Size(600, 600), CvType.CV_16S);
		perspectedPoints.forEach(point -> Imgproc.circle(outputPerspectedMat,
				new Point(point), 3, new Scalar(255), -1, Imgproc.LINE_8));

		Imgcodecs.imwrite(FileIO.getFilePath(VIDEO_PATH, "perspected_points", "jpg"), outputPerspectedMat);
	}
}
=======
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
>>>>>>> 6b432b5f135671bfb9507e70dd9c408c06270564
