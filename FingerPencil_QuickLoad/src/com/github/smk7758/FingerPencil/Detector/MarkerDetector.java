package com.github.smk7758.FingerPencil.Detector;

import java.util.ArrayList;
import java.util.List;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.github.smk7758.FingerPencil.ImgProcessUtil;
import com.github.smk7758.FingerPencil.ListMap;

public class MarkerDetector {
	final Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);

	/**
	 * 指定されたマーカーIDに適するMarkerのindexを返します。
	 *
	 * @param markerIds
	 * @param id
	 * @return
	 */
	public static int getMarkerIdIndex(Mat markerIds, int id) {
		for (int i = 0; i < markerIds.height(); i++) {
			if ((int) markerIds.get(i, 0)[0] == id) {
				return i;
			}
		}
		return -1;
	}

	public static ListMap<Point, Integer> detectMarkerPoints(Mat inputImage, Mat outputImage, Dictionary dictionary) {
		// 台形4点のマーカーの中心座標。

		// final Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);

		List<Mat> corners = new ArrayList<>();
		Mat markerIds = new Mat();
		// DetectorParameters parameters = DetectorParameters.create();
		Aruco.detectMarkers(inputImage, dictionary, corners, markerIds);

		if (outputImage != null) {
			Aruco.drawDetectedMarkers(outputImage, corners);
		}

		ListMap<Point, Integer> detectedMarkerPoints = new ListMap<>();

		final List<Point> markerCenterPoints = getCornerCenterPoints(corners);
		// markerIds.get(i, 0);
		// cornerPoints.get(i):

		markerCenterPoints
				.forEach(point -> Imgproc.circle(outputImage, point, 5, new Scalar(0, 0, 255), -1, Imgproc.LINE_8));

		for (int i = 0; i < markerCenterPoints.size() && i < markerIds.rows(); i++) {
			detectedMarkerPoints.add(markerCenterPoints.get(i), (int) markerIds.get(i, 0)[0]);
		}
		return detectedMarkerPoints;
	}

	/**
	 * マーカーのコーナーの中央の点の取得
	 *
	 * @param corners Arucoで取得された点
	 * @return マーカーのコーナーの中央の点
	 */
	public static List<Point> getCornerCenterPoints(List<Mat> corners) {
		List<Point> points = new ArrayList<>();
		for (Mat mat : corners) {
			// TODO
			// System.out.println(mat.dump());

			List<Point> cornerPoints = new ArrayList<>();
			for (int row = 0; row < mat.height(); row++) {
				for (int col = 0; col < mat.width(); col++) {
					cornerPoints.add(new Point(mat.get(row, col)));
				}
			}

			points.add(new Point(ImgProcessUtil.getCenter(cornerPoints)));
		}
		return points;
	}

	public static void estimateMarkerPose(Mat inputImage, Mat outputImage, Dictionary dictionary, Mat cameraMatrix,
			MatOfDouble distortionCoefficients) {
		List<Mat> corners = new ArrayList<>(); // Matにcornerが(x, y)で4隅の点があり、Listはマーカーの数。
		Mat markerIds = new Mat();
		// DetectorParameters parameters = DetectorParameters.create();
		Aruco.detectMarkers(inputImage, dictionary, corners, markerIds);

		Aruco.drawDetectedMarkers(inputImage, corners, markerIds);

		estimateMarkerPose(outputImage, cameraMatrix, distortionCoefficients, corners, markerIds, null);

	}

	/**
	 * Markerの姿勢推定を行う。
	 *
	 * @param outputImage
	 * @param cameraMatrix
	 * @param distortionCoefficients
	 * @param corners
	 * @param markerIds
	 * @param markerParameter 返却物。(R, t), 順番はcorners(markerIds)に従う。
	 */
	public static void estimateMarkerPose(Mat outputImage, Mat cameraMatrix, Mat distortionCoefficients,
			List<Mat> corners, Mat markerIds, ListMap<Mat, Mat> markerParameters) {

		// ListMap<Mat, Mat> markerParameter = new ListMap<>(); // (R, t)のつもり。

		for (Mat corner : corners) {
			Mat rotationMatrix = new Mat(), translationVectors = new Mat(); // 受け取る
			List<Mat> corner_ = new ArrayList<>();
			corner_.add(corner);
			Aruco.estimatePoseSingleMarkers(corner_, 0.05f, cameraMatrix, distortionCoefficients,
					rotationMatrix, translationVectors);

			if (markerParameters != null) markerParameters.add(rotationMatrix, translationVectors);

			Aruco.drawAxis(outputImage, cameraMatrix, distortionCoefficients,
					rotationMatrix, translationVectors, 0.1f);
			Mat rotationMatrix_ = new Mat(3, 3, CvType.CV_64FC1);
			Calib3d.Rodrigues(rotationMatrix, rotationMatrix_);
//			Main.debugLog("[R]"+rotationMatrix_.dump(),LogLevel.DEBUG,"process | MarkerDetector");
//			Main.debugLog("[t]"+translationVectors.dump(),LogLevel.DEBUG,"process | MarkerDetector");
		}
	}

	/**
	 * @param inputImage
	 * @param outputImage 返却物。
	 * @param dictionary
	 * @param cameraMatrix
	 * @param distortionCoefficients
	 * @param markerIds 返却物。
	 * @param detectedMarkerPoints 返却物。
	 * @param markerParameters 返却物。(R, t), 順番はcorners(markerIds)に従う。
	 */
	public static void process(Mat inputImage, Mat outputImage, Dictionary dictionary,
			Mat cameraMatrix, Mat distortionCoefficients, Mat markerIds,
			ListMap<Point, Integer> detectedMarkerPoints, ListMap<Mat, Mat> markerParameters) {
		// 台形4点のマーカーの中心座標。
		List<Mat> corners = new ArrayList<>(); // Markerの4隅
		// Mat markerIds = new Mat(); // MarkerID
		// DetectorParameters parameters = DetectorParameters.create();
		Aruco.detectMarkers(inputImage, dictionary, corners, markerIds);

		if (outputImage != null) {
			Aruco.drawDetectedMarkers(outputImage, corners);
		}

		// ListMap<Point, Integer> detectedMarkerPoints = new ListMap<>();

		final List<Point> markerCenterPoints = getCornerCenterPoints(corners);
		// markerIds.get(i, 0);
		// cornerPoints.get(i):

		markerCenterPoints
				.forEach(point -> Imgproc.circle(outputImage, point, 5, new Scalar(0, 0, 255), -1, Imgproc.LINE_8));

		for (int i = 0; i < markerCenterPoints.size() && i < markerIds.rows(); i++) {
			detectedMarkerPoints.add(markerCenterPoints.get(i), (int) markerIds.get(i, 0)[0]);
		}

		estimateMarkerPose(outputImage, cameraMatrix, distortionCoefficients, corners, markerIds, markerParameters);

	}
}
