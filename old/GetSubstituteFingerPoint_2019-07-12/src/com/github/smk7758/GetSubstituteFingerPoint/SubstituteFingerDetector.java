package com.github.smk7758.GetSubstituteFingerPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.github.smk7758.GetSubstituteFingerPoint.Main.LogLevel;

public class SubstituteFingerDetector {
	// OpenCV => 0<H<180, 0<S<255, 0<V<255
	// GIMP => 0<H<360, 0<S<100, 0<V<100 ::==>> H'=H * 1/2, S'=S*2, V'=V*2
	static final Scalar minHsvBlue = new Scalar(100, 160, 100), maxHsvBlue = new Scalar(135, 255, 255);
	// 205, 83, 58,, not: 198, 18, 68,, 206, 17, 62,,
	static final Scalar minHsvRed = new Scalar(170, 100, 65), maxHsvRed = new Scalar(180, 180, 180);
	// 353, 62, 34

	//	static final Scalar minHsvBlue = new Scalar(100, 80, 80), maxHsvBlue =new Scalar(135, 255, 255); // 学校のインク
	//	static final Scalar minHsvRed = new Scalar(160, 90, 50), maxHsvRed = new Scalar(180, 255, 200); // 学校のインク

	//だめ版
	//	static final Scalar minHsvBlue = new Scalar(100, 100, 80), maxHsvBlue = new Scalar(135, 255, 255);
	// 水色系 家のインク, GIMP: (129, 29, 56), (140, 31, 51)
	// (216, 53.6, 43.9)
	//	static final Scalar minHsvRed = new Scalar(170, 100, 100), maxHsvRed = new Scalar(180, 180, 180);
	//	 赤 家のインク, GIMP: (353, 71, 70), (357, 67, 63)
	//	 (2.8, 62.8, 67.5)

	static final Scalar minHsvRed_0 = new Scalar(0, 110, 65), maxHsvRed_0 = new Scalar(10, 240, 240);
	static final Scalar minHsvRed_1 = new Scalar(170, 110, 65), maxHsvRed_1 = new Scalar(180, 255, 255);
	// 356, 65, 40

	// static final Scalar hsv_blue_min = new Scalar(100, 50, 80), hsv_blue_max = new Scalar(135, 255, 200);
	// static final Scalar hsv_red_min = new Scalar(165, 90, 50), hsv_red_max = new Scalar(180, 255, 200);

	// final Scalar hsv_blue_min = new Scalar(90, 50, 30), hsv_blue_max = new Scalar(135, 255, 200);
	// final Scalar hsv_red_min = new Scalar(150, 50, 50), hsv_red_max = new Scalar(180, 255, 200);
	// final Point left_point_up = new Point(567, 559), left_point_down = new Point(238, 894),
	// right_point_up = new Point(1353, 541), right_point_down = new Point(1642, 877);

	// public static Optional<Point> getSubstituteFingerPoint(Mat matHsv) {
	//
	// // 指の点の取得 (ここから)
	// Mat matHsvBlue = getBluePart(matHsv);
	// Mat matHsvRed = getRedPart(matHsv);
	//
	// // getFingerPoint
	// final Optional<List<double[]>> bluePoint = ImgProcessUtil.getCenterPointContrus(matHsvBlue),
	// redPoint = ImgProcessUtil.getCenterPointContrus(matHsvRed);
	//
	// if (!bluePoint.isPresent()) {
	// Main.debugLog("BluePoint is null (Cannot find the point).", LogLevel.DEBUG,
	// "getSubstituteFingerPoint");
	// return Optional.empty();
	// }
	// if (!redPoint.isPresent()) {
	// Main.debugLog("RedPoint is null (Cannot find the point).", LogLevel.DEBUG,
	// "getSubstituteFingerPoint");
	// return Optional.empty();
	// }
	// if (bluePoint.get().size() < 1 || redPoint.get().size() < 1) {
	// Main.debugLog("BluePoint or RedPoint is none (Cannot find the point).", LogLevel.DEBUG,
	// "getSubstituteFingerPoint");
	// return Optional.empty();
	// }
	//
	// // 扱いやすくするためにこうした。
	// final double[] bluePoint_ = bluePoint.get().get(0),
	// redPoint_ = redPoint.get().get(0); // 最大
	//
	// final double fingerDiff_y = bluePoint_[1] - redPoint_[1];
	//
	// return Optional.of(new Point(redPoint_[0], redPoint_[1] - (fingerDiff_y / 5)));
	// }

	public static Optional<Point> getSubstituteFingerPoint(Mat matHsv, Mat outputImage) {

		// 指の点の取得 (ここから)
		Mat matHsvBlue = getBluePart(matHsv);
		Mat matHsvRed = getRedPartDouble(matHsv);

		// getFingerPoint
		List<MatOfPoint> contoursBlue = new ArrayList<>(), contoursRed = new ArrayList<>();
		final Optional<List<double[]>> bluePoint = ImgProcessUtil.getCenterPointContrus(matHsvBlue, contoursBlue),
				redPoint = ImgProcessUtil.getCenterPointContrus(matHsvRed, contoursRed);

		if (!bluePoint.isPresent()) {
			Main.debugLog("BluePoint is null (Cannot find the point).", LogLevel.DEBUG,
					"getSubstituteFingerPoint");
			return Optional.empty();
		}
		if (!redPoint.isPresent()) {
			Main.debugLog("RedPoint is null (Cannot find the point).", LogLevel.DEBUG,
					"getSubstituteFingerPoint");
			return Optional.empty();
		}
		if (bluePoint.get().size() < 1 || redPoint.get().size() < 1) {
			Main.debugLog("BluePoint or RedPoint is none (Cannot find the point).", LogLevel.DEBUG,
					"getSubstituteFingerPoint");
			return Optional.empty();
		}

		Imgproc.drawContours(outputImage, contoursBlue,
				ImgProcessUtil.getSortedAreaNumber(contoursBlue).get(0).getKey(), new Scalar(255, 0, 0), 10);
		Imgproc.drawContours(outputImage, contoursRed,
				ImgProcessUtil.getSortedAreaNumber(contoursRed).get(0).getKey(), new Scalar(0, 0, 255), 10);

		Main.debugLog("RedPointSize: " + redPoint.get().size(), LogLevel.DEBUG, "getSubstituteFingerPoint");
		Main.debugLog("BluePointSize: " + bluePoint.get().size(), LogLevel.DEBUG, "getSubstituteFingerPoint");

		// 扱いやすくするためにこうした。
		final double[] bluePoint_ = bluePoint.get().get(0),
				redPoint_ = redPoint.get().get(0); // 最大

		final double fingerDiff_y = bluePoint_[1] - redPoint_[1];

		final double test = (redPoint_[1] - (fingerDiff_y / 5));

		Main.debugLog("RedPoint: " + redPoint_[0] + ", " + redPoint_[1], LogLevel.DEBUG, "getSubstituteFingerPoint");
		Main.debugLog("BluePoint: " + bluePoint_[0] + ", " + bluePoint_[1], LogLevel.DEBUG, "getSubstituteFingerPoint");

		Main.debugLog("X: " + redPoint_[0] + ", Y: " + test + ", diff: " + fingerDiff_y, LogLevel.DEBUG,
				"getSubstituteFingerPoint");

		return Optional.of(new Point(redPoint_[0], redPoint_[1] - (fingerDiff_y / 5)));
	}

	// public static Mat getRedPart_(Mat matHsv) {
	// Mat matRedPart = matHsv.clone();
	// Core.inRange(matRedPart, hsv_red_min, hsv_red_max, matRedPart);
	// return matRedPart;
	// }
	//
	// public static Mat getBluePart_(Mat matHsv) {
	// Mat matBluePart = matHsv.clone();
	// Core.inRange(matBluePart, hsv_blue_min, hsv_blue_max, matBluePart);
	// return matBluePart;
	// }

	public static Mat getRedPart(Mat matHsv) {
		Mat matRedPart = matHsv.clone();
		Core.inRange(matRedPart, minHsvRed, maxHsvRed, matRedPart);
		return matRedPart;
	}

	public static Mat getRedPartDouble(Mat matHsv) {
		Mat matRedPart0 = matHsv.clone();
		Mat matRedPart1 = matHsv.clone();

		Core.inRange(matRedPart0, minHsvRed_0, maxHsvRed_0, matRedPart0);
		Core.inRange(matRedPart1, minHsvRed_1, maxHsvRed_1, matRedPart1);

		Core.add(matRedPart0, matRedPart1, matRedPart0);
		return matRedPart0;
	}

	public static Mat getBluePart(Mat matHsv) {
		Mat matBluePart = matHsv.clone();
		Core.inRange(matBluePart, minHsvBlue, maxHsvBlue, matBluePart);
		return matBluePart;
	}
}
