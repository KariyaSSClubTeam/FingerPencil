package com.github.smk7758.FingerPencil.Detector;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.github.smk7758.FingerPencil.ImgProcessUtil;
import com.github.smk7758.FingerPencil.Main;
import com.github.smk7758.FingerPencil.Main.LogLevel;

public class SubstituteFingerDetector {
	// OpenCV => 0<H<180, 0<S<255, 0<V<255
	// GIMP => 0<H<360, 0<S<100, 0<V<100 ::==>> H'=H * 1/2, S'=S*2, V'=V*2
//	static final Scalar minHsvBlue = new Scalar(100, 160, 100), maxHsvBlue = new Scalar(135, 255, 255);
//	// 205, 83, 58,, not: 198, 18, 68,, 206, 17, 62,,
//	static final Scalar minHsvRed = new Scalar(170, 100, 65), maxHsvRed = new Scalar(180, 180, 180);
//	// 353, 62, 34
//	static final Scalar minHsvBlue_0 = new Scalar(0, 110, 65), maxHsvBlue_0 = new Scalar(10, 240, 240);
//	static final Scalar minHsvRed_1 = new Scalar(170, 110, 65), maxHsvRed_1 = new Scalar(180, 255, 255);
	// 356, 65, 40

//	 static final Scalar minHsvBlue = new Scalar(100, 80, 80), maxHsvBlue = new Scalar(135, 255, 255); // 学校のインク
//	 static final Scalar minHsvRed = new Scalar(160, 90, 50), maxHsvRed = new Scalar(180, 255, 200); // 学校のインク

//	static final Scalar minHsvBlue = new Scalar(86, 159, 212), maxHsvBlue = new Scalar(106, 213, 255);
//	// 205, 83, 58,, not: 198, 18, 68,, 206, 17, 62,,
//	static final Scalar minHsvRed = new Scalar(0, 95,170 ), maxHsvRed = new Scalar(16, 141, 213);
//	// 353, 62, 34
	 	static final Scalar minHsvBlue_0 = new Scalar(86, 212, 212), maxHsvBlue_0 = new Scalar(106, 255, 255);
	 	static final Scalar minHsvBlue = new Scalar(70, 30, 80), maxHsvBlue = new Scalar(100, 75, 120);//みどり
	 	static final Scalar minHsvRed =  new Scalar(125, 5, 60), maxHsvRed = new Scalar(175, 30, 95);//むらさき
//

	/**
	 * @param matHsv
	 * @param outputImage
	 * @return (Key, Value) = (FingerPoint, BluePoint)
	 */
	public static Optional<Entry<Point, Point>> getSubstituteFingerPoint(Mat matHsv, Mat outputImage) {

		// 指の点の取得 (ここから)
		Mat matHsvBlue = getBluePart(matHsv);
		Mat matHsvRed = getRedPart(matHsv);

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

		return Optional.of(new AbstractMap.SimpleEntry<Point, Point>(
				new Point(redPoint_[0], redPoint_[1] - (fingerDiff_y / 10)),
				new Point(bluePoint_[0], bluePoint_[1])));
	}

	public static double Diff(Mat matHsv) {
		Mat matHsvBlue = getBluePart(matHsv);
		Mat matHsvRed = getRedPart(matHsv);

		List<MatOfPoint> contoursBlue = new ArrayList<>(), contoursRed = new ArrayList<>();
		final Optional<List<double[]>> bluePoint = ImgProcessUtil.getCenterPointContrus(matHsvBlue, contoursBlue),
				redPoint = ImgProcessUtil.getCenterPointContrus(matHsvRed, contoursRed);


		if (!bluePoint.isPresent()) {
			Main.debugLog("BluePoint is null (Cannot find the point).", LogLevel.DEBUG,
					"getSubstituteFingerPoint");
			return 0;
		}
		if (!redPoint.isPresent()) {
			Main.debugLog("RedPoint is null (Cannot find the point).", LogLevel.DEBUG,
					"getSubstituteFingerPoint");
			return 0;
		}
		if (bluePoint.get().size() < 1 || redPoint.get().size() < 1) {
			Main.debugLog("BluePoint or RedPoint is none (Cannot find the point).", LogLevel.DEBUG,
					"getSubstituteFingerPoint");
			return 0;
		}
		final double[] bluePoint_ = bluePoint.get().get(0),
				redPoint_ = redPoint.get().get(0); // 最大

		final double fingerDiff_y = bluePoint_[1] - redPoint_[1];

		Main.debugLog("Diff:"+fingerDiff_y, LogLevel.DEBUG,"getSubstituteFingerPoint");

		return fingerDiff_y;


	}

//	public static Mat getRedPartDouble(Mat matHsv) {
//
////		Mat matRedPart1 = matHsv.clone();
//
//		Core.inRange(matBluePart0, minHsvBlue_0, maxHsvBlue_0, matBluePart0);
////		Core.inRange(matRedPart1, minHsvRed_1, maxHsvRed_1, matRedPart1);
//
////		Core.add(matRedPart0, matRedPart1, matRedPart0);
//		return matBluePart0;
//	}

	public static Mat getRedPart(Mat matHsv) {
		Mat matRedPart = matHsv.clone();
		Core.inRange(matRedPart, minHsvRed, maxHsvRed, matRedPart);
		return matRedPart;
	}

	public static Mat getBluePart(Mat matHsv) {
		Mat matBluePart = matHsv.clone();
		Mat matBluePart0 = matHsv.clone();
		Core.inRange(matBluePart, minHsvBlue, maxHsvBlue, matBluePart);
		Core.inRange(matBluePart0, minHsvBlue_0, maxHsvBlue_0, matBluePart0);
		Core.add(matBluePart, matBluePart0, matBluePart);


		return matBluePart;
	}
}
