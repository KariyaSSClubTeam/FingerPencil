package com.github.smk7758.FingerPencil.Detector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.github.smk7758.FingerPencil.ImgProcessUtil;

public class FingerDetector {
	public static Optional<Point> getFingerPoint(Mat firstMat, Mat mat) {
		List<MatOfPoint> contours = new ArrayList<>(); // 初期化

		Mat mat_hsv_diff = new Mat();

		Core.absdiff(firstMat, mat, mat_hsv_diff); // 差分を取る

		getSkinPart(contours, mat_hsv_diff);

		// 境界線を点としてとる

		// 最大面積をみつける
		final int contour_max_area = ImgProcessUtil.getLargestContoursArea(contours);

		if (contour_max_area < 0) {
			System.err.println("Cannot find finger point in countour_max_area.");
			return Optional.empty();
		}

		final MatOfPoint points = contours.get(contour_max_area);

		// ConvexHull
		MatOfInt hull = new MatOfInt();
		Imgproc.convexHull(points, hull, true);

		int[] hull_array = hull.toArray();
		// drawConvexHullPoints(points, hull, mat);

		// 最小の傾きの点の取得
		// System.out.println("最小の傾きの点の取得");
		int smallest = ImgProcessUtil.getSmallestInclinationNumber(points, hull_array);
		final Point fingerPoint = new Point(points.get(hull_array[smallest], 0));
		return Optional.of(fingerPoint);
	}

	private static Mat getSkinPart(List<MatOfPoint> contours, Mat mat_hsv) {
		Mat mat_skin = new Mat();
		final Scalar hsv_skin_min = new Scalar(0, 30, 60), hsv_skin_max = new Scalar(20, 150, 255);
		Core.inRange(mat_hsv, hsv_skin_min, hsv_skin_max, mat_skin); // 色範囲選択

		Imgproc.findContours(mat_skin, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

		return mat_skin;
	}
}
