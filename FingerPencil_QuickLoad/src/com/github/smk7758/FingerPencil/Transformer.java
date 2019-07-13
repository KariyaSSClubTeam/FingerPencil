package com.github.smk7758.FingerPencil;

import java.util.Optional;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Transformer {
	public static Mat transformPoint(Point fingerPoint, Mat homographyMatrix, Size size) {
		// 透視変換が行われる画像(src)の生成
		Mat srcPerspectImage = Mat.zeros(size, CvType.CV_16SC1);

		// 透視変換が行われる画像に点を加える
		Imgproc.circle(srcPerspectImage, fingerPoint, 2, new Scalar(255), -1, 4, 0);

		return transformImage(srcPerspectImage, homographyMatrix, size);
	}

	public static Mat transformImage(Mat srcPerspectImage, Mat homographyMatrix, Size size) {
		// 透視変換の結果を出力する画像の生成
		Mat perspectedImage = new Mat(srcPerspectImage.size(), CvType.CV_16SC1);

		// 透視変換の実行
		Imgproc.warpPerspective(srcPerspectImage, perspectedImage, homographyMatrix, perspectedImage.size(),
				Imgproc.INTER_LINEAR);

		// トリミング処理
//		final Rect rect = new Rect(5, 5, 600, 600);
//		perspectedImage = new Mat(perspectedImage, rect);

		// 回転処理
		// final Mat matrix_ = Imgproc.getRotationMatrix2D(new Point(300, 300), -90.0, 1.0);
		// Imgproc.warpAffine(perspectedMat, perspectedMat, matrix_, new Size(600, 600));

		perspectedImage.convertTo(perspectedImage, CvType.CV_8UC1);

		return perspectedImage;
	}

	/**
	 * 台形4点の取得ができていないと実行できない。 透視変換行列の取得
	 *
	 * @return 透視変換行列, ただしMarkerを取得出来なかった時、nullを返す。
	 */
	public static Optional<Mat> getHomographyMatrix(ListMap<Point, Integer> detectMarkerPoints) {
		// 透視変換基底の前(変換前の基準4点)

		final double[] srcPoint = new double[8];
		for (int i = 0; i < 4; i++) {
			if (!detectMarkerPoints.containsValue(i)) {
				System.out.println(i);
				return Optional.empty(); // 以前のやつを使う。
			}
			srcPoint[2 * i] = detectMarkerPoints.getKey(i).x;
			srcPoint[2 * i + 1] = detectMarkerPoints.getKey(i).y;
		}

		Mat srcPointMat = new Mat(4, 2, CvType.CV_32F);
		srcPointMat.put(0, 0, srcPoint);

		// 透視変換基底の後(変換後の基準4点の生成)
		final double[] dstPoint = { 5, 5, 600, 5, 5, 600, 600, 600 }; // UP_Left, UP_Right, DOWN_Left, DOWN_Right
		Mat dstPointMat = new Mat(4, 2, CvType.CV_32F);
		dstPointMat.put(0, 0, dstPoint);

		// 透視変換基底行列の取得
		Mat mapMatrix = Imgproc.getPerspectiveTransform(srcPointMat, dstPointMat);

		// TODO
		if (mapMatrix == null) System.err.println("matMatrix is null @getHomographyMatrix");

		return Optional.ofNullable(mapMatrix);
	}
}
