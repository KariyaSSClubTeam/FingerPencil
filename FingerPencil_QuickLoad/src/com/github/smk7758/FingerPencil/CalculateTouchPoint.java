package com.github.smk7758.FingerPencil;

import java.util.AbstractMap;
import java.util.Map.Entry;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Size;

import com.github.smk7758.FingerPencil.Main.LogLevel;

public class CalculateTouchPoint {
	private Mat cameraMatrix;
	private MatOfDouble distortionCoefficients;
	final double focus_p;
	final double focus_m;

	public CalculateTouchPoint(CameraParameter cameraParameter) {
		cameraMatrix = cameraParameter.cameraMatrix;
		distortionCoefficients = cameraParameter.distortionCoefficients_;

		double[] tmp_f_x = new double[cameraMatrix.channels()], tmp_f_y = new double[cameraMatrix.channels()];
		cameraMatrix.get(0, 0, tmp_f_x);
		cameraMatrix.get(1, 1, tmp_f_y);
		double f_x = tmp_f_x[0], f_y = tmp_f_y[0];
		focus_p = (f_x + f_y) / 2;
		focus_m = 0.0076;



	}

	/**
	 * 指先の点の空間座標を変換して、接地判定を行う。
	 */
	public Entry<Point, Point> process(Entry<Point, Point> fingerPoint, Mat rotationVector, Mat translationVector,
			double objectLength, Mat outputPoint) {

		Main.debugLog("A-Matrix: " + cameraMatrix.dump(), LogLevel.DEBUG, "process | CalculatePoint");

		Mat rotationVector_= new Mat(3, 1, rotationVector.type());
		rotationVector_ = rotationVector;
		Mat rotationMatrix = new Mat(3, 3, CvType.CV_64FC1);
		Calib3d.Rodrigues(rotationVector_, rotationMatrix);
		Main.debugLog("R: " + rotationMatrix.dump(), LogLevel.DEBUG, "process | CalculatePoint");

		Mat translationVectork = new Mat(3, 1, translationVector.type());
		translationVectork = translationVector;
		// Mat translationVector_ =
		// convertVerticalTranslationVectorHorizontal(translationVectork);
		Mat translationVector_ = translationVectork.t();
		Mat translationVectorVertical = convertVerticalTranslationVectorHorizontal(translationVectork);
		Main.debugLog("t_: " + translationVector_.dump(), LogLevel.DEBUG, "process | CalculatePoint");
		Main.debugLog("tk: " + translationVectork.dump(), LogLevel.DEBUG, "process | CalculatePoint");
		Main.debugLog("tv: " + translationVectorVertical.dump(), LogLevel.DEBUG, "process | CalculatePoint");

		Main.debugLog("A': " + fingerPoint.getKey(), LogLevel.DEBUG, "process | CalculatePoint");
		Main.debugLog("B': " + fingerPoint.getValue(), LogLevel.DEBUG, "process | CalculatePoint");
		Mat vectorA_r = new Mat(new Size(1, 3), CvType.CV_64FC1);
		vectorA_r.put(0, 0, new double[] { fingerPoint.getKey().x, fingerPoint.getKey().y, 1 });

		Main.debugLog("vectorA_r: " + vectorA_r.dump(), LogLevel.DEBUG, "process | CalculatePoint");
		Mat vectorB_r = new Mat(new Size(1, 3), CvType.CV_64FC1);
		vectorB_r.put(0, 0, new double[] { fingerPoint.getValue().x, fingerPoint.getValue().y, 1 });
		Main.debugLog("vectorB_r: " + vectorB_r.dump(), LogLevel.DEBUG, "process | CalculatePoint");

		return process_(vectorA_r, vectorB_r, rotationMatrix, translationVectorVertical, objectLength, outputPoint);
	}

	/**
	 * 指先の点の空間座標を変換して、接地判定を行う。(数値計算向け)
	 *
	 * @param vectorA_ 指先の点 (Z=f)
	 */
	public Entry<Point, Point> process_(Mat vectorA_r, Mat vectorB_r, Mat rotationMatrix, Mat translationVectorVertical,
			double objectLength, Mat outputPoint) {
		Mat pointA_ = new Mat(new Size(1, 3), vectorA_r.type());
		Core.gemm(cameraMatrix.inv(), vectorA_r, 1, new Mat(), 1, pointA_);
//		Mat I0 = new Mat(new Size(3,4),pointA_.type());
//		I0.put(0, 0, new double[] {1,0,0,0});
//		I0.put(1, 0, new double[] {0,1,0,0});
//		I0.put(2, 0, new double[] {0,0,1,0});
//		Mat A_ = new Mat(new Size(1, 3),pointA_.type());
//		A_ = pointA_.mul(Mat.ones(pointA_.size(), pointA_.type()),focus_m);
//		Core.gemm(I0.inv(),pointA_,1,new Mat(),1,A_);
		Mat vectorA_ = new Mat(new Size(1, 3), vectorA_r.type());

//		double l  = vectorA_.get(0, 0)[0];
//		double c = vectorA_.get(1, 0)[0];
//		System.out.println(l);
//		System.out.println(c);
//		vectorA_.put(0, 0, new double[] {pointA_.get(0, 0)[0],pointA_.get(1, 0)[0],focus_m});
		vectorA_ = pointA_.mul(Mat.ones(pointA_.size(), pointA_.type()), focus_m);
//		vectorA_ = convertVerticalTranslationVectorHorizontal(vectorA_);
//		Main.debugLog("I0:"+I0.dump(),LogLevel.DEBUG,"process | CalculatePoint");
		Main.debugLog("pointA_: " + pointA_.dump(), LogLevel.DEBUG, "process | CalculatePoint");
//		Main.debugLog("A_: " + A_.dump(), LogLevel.DEBUG, "process | CalculatePoint");
		Main.debugLog("vectorA_: " + vectorA_.dump(), LogLevel.DEBUG, "process | CalculatePoint");

		Mat pointB_ = new Mat(new Size(1, 3), vectorB_r.type());
		Core.gemm(cameraMatrix.inv(), vectorB_r, 1, new Mat(), 1, pointB_);
//		Mat I0 = new Mat(new Size(3,4),pointA_.type());
//		Mat B_ = new Mat(new Size(1, 3),pointB_.type());
//		B_ = pointB_.mul(Mat.ones(pointB_.size(), CvType.CV_64FC1),focus_m);
//		Core.gemm(I0.inv(),pointA_,1,new Mat(),1,B_);
		Mat vectorB_  = new Mat(new Size(1, 3), vectorB_r.type());
//		vectorB_.put(0, 0, new double[] {pointB_.get(0, 0)[0],pointB_.get(1, 0)[0],focus_m});
		vectorB_ = pointB_.mul(Mat.ones(pointB_.size(), pointB_.type()), focus_m);
//		vectorB_ = convertVerticalTranslationVectorHorizontal(vectorB_);
		Main.debugLog("pointB_: " + pointB_.dump(), LogLevel.DEBUG, "process | CalculatePoint");
//		Main.debugLog("B_: " + B_.dump(), LogLevel.DEBUG, "process | CalculatePoint");
		Main.debugLog("vectorB_: " + vectorB_.dump(), LogLevel.DEBUG, "process | CalculatePoint");



		// create Nw
		Mat vectorNw = new Mat(new Size(1, 3), CvType.CV_64FC1);
		vectorNw.put(0, 0, new double[] { 0, 1, 0 });

		// Nw → N
		Mat vectorN = new Mat();

		// Nwベクトルの座標軸的な角度の変換 N = R*Nw


		convertToWorldCoordinate(vectorNw, rotationMatrix, new Mat(), vectorN);
		double Nsize = vectorN.get(0, 0)[0]*vectorN.get(0, 0)[0]+vectorN.get(1, 0)[0]*vectorN.get(1, 0)[0]+vectorN.get(2, 0)[0]*vectorN.get(2, 0)[0];

		Main.debugLog("vectorN: " + vectorN.dump(), LogLevel.DEBUG, "process | CalculatePoint");
		Main.debugLog("Nsize: " + Math.sqrt(Nsize), LogLevel.DEBUG, "process | CalculatePoint");

		// A' → A の係数を求める
		final double k_0 = vectorN.dot(translationVectorVertical);
		final double k_1 = vectorA_.dot(vectorN);
//		final double k = vectorN.dot(translationVectorVertical) / vectorA_.dot(vectorN);
		final double k = k_0/k_1;

		Main.debugLog("k_0:  " + k_0 + ", k_1: " + k_1 + ", k: " + k, LogLevel.DEBUG, "process | CalculatePoint");

		// A = k * A'
		Mat vectorA = vectorA_.mul(Mat.ones(vectorA_.size(), vectorA_.type()), k);

		Main.debugLog("A: " + vectorA.dump(), LogLevel.INFO, "process | CalculatePoint");

		Mat vectorAw = new Mat(new Size(1, 3), CvType.CV_64FC1);
		CalculateTouchPoint.unconvertToWorldCoordinate__(vectorA, rotationMatrix, translationVectorVertical, vectorAw);
		Main.debugLog("Aw: " + vectorAw.dump(), LogLevel.INFO, "process | CalculatePoint"); // TODO

		Mat vectorBw = new Mat();
		Mat vectorNw_l = vectorNw.mul(Mat.ones(vectorNw.size(), vectorNw.type()), objectLength);
		Core.add(vectorAw, vectorNw_l, vectorBw);

		Main.debugLog("vecrotNw_l: " + vectorNw_l.dump(), LogLevel.DEBUG, "process | CalculatePoint");

		Main.debugLog("Bw: " + vectorBw.dump(), LogLevel.INFO, "process | CalculatePoint");

		Mat pointsDst = projectPoints(rotationMatrix, translationVectorVertical, vectorAw, vectorBw);
//		Mat pointsDst = new Mat();
//		Mat Rt = ;
//		Mat PMatrix = Core.gemm(cameraMatrix, , 1, new Mat(), 1, PMatrix);;

		Mat vectorB_projected = new Mat(new Size(1, 3), CvType.CV_64FC1);
		Mat vectorB_origin = new Mat(new Size(1, 3),vectorB_r.type());
		vectorB_origin.put(0, 0, new double[] { vectorB_r.get(0, 0)[0],vectorB_r.get(1, 0)[0],focus_m});
		vectorB_projected.put(0, 0, new double[] { pointsDst.get(1, 0)[0], pointsDst.get(1, 0)[1], focus_m });

		Main.debugLog("vectorB_origin: " + vectorB_origin.dump(), LogLevel.INFO, "process | CalculatePoint");
		Main.debugLog("vectorB_project: " + vectorB_projected.dump(), LogLevel.INFO, "process | CalculatePoint");

		Mat differenceB = new Mat();
		// B'点と投影し直したB点の比較
		Core.subtract(vectorB_projected, vectorB_origin, differenceB);
		double differenceBsize = differenceB.get(0, 0)[0]*differenceB.get(0, 0)[0]+differenceB.get(1, 0)[0]*differenceB.get(1, 0)[0]+differenceB.get(2, 0)[0]*differenceB.get(2, 0)[0];

		Main.debugLog("differenceB: " + differenceB.dump(), LogLevel.INFO, "process | CalculatePoint");
//		Main.debugLog("differenceB: " + differenceB.get(0, 0)[0], LogLevel.DEBUG, "process | CalculatePoint");
//		Main.debugLog("differenceB: " + differenceB.get(1, 0)[0], LogLevel.DEBUG, "process | CalculatePoint");
//		Main.debugLog("differenceB: " + differenceB.get(2, 0)[0], LogLevel.DEBUG, "process | CalculatePoint");
		Main.debugLog("differenceBsize: " + Math.sqrt(differenceBsize), LogLevel.INFO, "process | CalculatePoint");

		Entry<Point, Point> fingerPointProjected = new AbstractMap.SimpleEntry<Point, Point>(
				new Point(pointsDst.get(0, 0)), new Point(pointsDst.get(1, 0)));

		Main.debugLog("vectorA_projected: " + fingerPointProjected.getKey()
				+ ", vectorB_projected: " + fingerPointProjected.getValue(), LogLevel.INFO, "process | CalculatePoint");

		return fingerPointProjected;
	}

	public double BdiffSize_(Entry<Point, Point> fingerPoint, Mat rotationVector, Mat translationVector,
			double objectLength, Mat outputPoint) {

//		Main.debugLog("A-Matrix: " + cameraMatrix.dump(), LogLevel.DEBUG, "process | CalculatePoint");

		Mat rotationVector_= new Mat(3, 1, rotationVector.type());
		rotationVector_ = rotationVector;
		Mat rotationMatrix = new Mat(3, 3, CvType.CV_64FC1);
		Calib3d.Rodrigues(rotationVector_, rotationMatrix);

		Mat translationVectork = new Mat(3, 1, translationVector.type());
		translationVectork = translationVector;
		// Mat translationVector_ =
		// convertVerticalTranslationVectorHorizontal(translationVectork);
		Mat translationVector_ = translationVectork.t();
		Mat translationVectorVertical = convertVerticalTranslationVectorHorizontal(translationVectork);

		Mat vectorA_r = new Mat(new Size(1, 3), CvType.CV_64FC1);
		vectorA_r.put(0, 0, new double[] { fingerPoint.getKey().x, fingerPoint.getKey().y, 1 });


		Mat vectorB_r = new Mat(new Size(1, 3), CvType.CV_64FC1);
		vectorB_r.put(0, 0, new double[] { fingerPoint.getValue().x, fingerPoint.getValue().y, 1 });


		return BdiffSize(vectorA_r, vectorB_r, rotationMatrix, translationVectorVertical, objectLength, outputPoint);
	}

	/**
	 * 指先の点の空間座標を変換して、接地判定を行う。(数値計算向け)
	 *
	 * @param vectorA_ 指先の点 (Z=f)
	 */
	public double BdiffSize(Mat vectorA_r, Mat vectorB_r, Mat rotationMatrix, Mat translationVectorVertical,
			double objectLength, Mat outputPoint) {
		Mat pointA_ = new Mat(new Size(1, 3), vectorA_r.type());
		Core.gemm(cameraMatrix.inv(), vectorA_r, 1, new Mat(), 1, pointA_);
//		Mat I0 = new Mat(new Size(3,4),pointA_.type());
//		I0.put(0, 0, new double[] {1,0,0,0});
//		I0.put(1, 0, new double[] {0,1,0,0});
//		I0.put(2, 0, new double[] {0,0,1,0});
//		Mat A_ = new Mat(new Size(1, 3),pointA_.type());
//		A_ = pointA_.mul(Mat.ones(pointA_.size(), pointA_.type()),focus_m);
//		Core.gemm(I0.inv(),pointA_,1,new Mat(),1,A_);
		Mat vectorA_ = new Mat(new Size(1, 3), vectorA_r.type());

//		double l  = vectorA_.get(0, 0)[0];
//		double c = vectorA_.get(1, 0)[0];
//		System.out.println(l);
//		System.out.println(c);
//		vectorA_.put(0, 0, new double[] {pointA_.get(0, 0)[0],pointA_.get(1, 0)[0],focus_m});
		vectorA_ = pointA_.mul(Mat.ones(pointA_.size(), pointA_.type()), focus_m);
//		vectorA_ = convertVerticalTranslationVectorHorizontal(vectorA_);
//		Main.debugLog("I0:"+I0.dump(),LogLevel.DEBUG,"process | CalculatePoint");


		Mat pointB_ = new Mat(new Size(1, 3), vectorB_r.type());
		Core.gemm(cameraMatrix.inv(), vectorB_r, 1, new Mat(), 1, pointB_);
//		Mat I0 = new Mat(new Size(3,4),pointA_.type());
//		Mat B_ = new Mat(new Size(1, 3),pointB_.type());
//		B_ = pointB_.mul(Mat.ones(pointB_.size(), CvType.CV_64FC1),focus_m);
//		Core.gemm(I0.inv(),pointA_,1,new Mat(),1,B_);
		Mat vectorB_  = new Mat(new Size(1, 3), vectorB_r.type());
//		vectorB_.put(0, 0, new double[] {pointB_.get(0, 0)[0],pointB_.get(1, 0)[0],focus_m});
		vectorB_ = pointB_.mul(Mat.ones(pointB_.size(), pointB_.type()), focus_m);
//		vectorB_ = convertVerticalTranslationVectorHorizontal(vectorB_);



		// create Nw
		Mat vectorNw = new Mat(new Size(1, 3), CvType.CV_64FC1);
		vectorNw.put(0, 0, new double[] { 0, 1, 0 });

		// Nw → N
		Mat vectorN = new Mat();

		// Nwベクトルの座標軸的な角度の変換 N = R*Nw


		convertToWorldCoordinate(vectorNw, rotationMatrix, new Mat(), vectorN);
		double Nsize = vectorN.get(0, 0)[0]*vectorN.get(0, 0)[0]+vectorN.get(1, 0)[0]*vectorN.get(1, 0)[0]+vectorN.get(2, 0)[0]*vectorN.get(2, 0)[0];


		// A' → A の係数を求める
		final double k_0 = vectorN.dot(translationVectorVertical);
		final double k_1 = vectorA_.dot(vectorN);
//		final double k = vectorN.dot(translationVectorVertical) / vectorA_.dot(vectorN);
		final double k = k_0/k_1;


		// A = k * A'
		Mat vectorA = vectorA_.mul(Mat.ones(vectorA_.size(), vectorA_.type()), k);

//		Main.debugLog("A: " + vectorA.dump(), LogLevel.INFO, "process | CalculatePoint");

		Mat vectorAw = new Mat(new Size(1, 3), CvType.CV_64FC1);
		CalculateTouchPoint.unconvertToWorldCoordinate__(vectorA, rotationMatrix, translationVectorVertical, vectorAw);

		Mat vectorBw = new Mat();
		Mat vectorNw_l = vectorNw.mul(Mat.ones(vectorNw.size(), vectorNw.type()), objectLength);
		Core.add(vectorAw, vectorNw_l, vectorBw);

		Mat pointsDst = projectPoints(rotationMatrix, translationVectorVertical, vectorAw, vectorBw);
//		Mat pointsDst = new Mat();
//		Mat Rt = ;
//		Mat PMatrix = Core.gemm(cameraMatrix, , 1, new Mat(), 1, PMatrix);;

		Mat vectorB_projected = new Mat(new Size(1, 3), CvType.CV_64FC1);
		Mat vectorB_origin = new Mat(new Size(1, 3),vectorB_r.type());
		vectorB_origin.put(0, 0, new double[] { vectorB_r.get(0, 0)[0],vectorB_r.get(1, 0)[0],focus_m});
		vectorB_projected.put(0, 0, new double[] { pointsDst.get(1, 0)[0], pointsDst.get(1, 0)[1], focus_m });

		Mat differenceB = new Mat();
		// B'点と投影し直したB点の比較
		Core.subtract(vectorB_projected, vectorB_origin, differenceB);
		double differenceBsize = differenceB.get(0, 0)[0]*differenceB.get(0, 0)[0]+differenceB.get(1, 0)[0]*differenceB.get(1, 0)[0]+differenceB.get(2, 0)[0]*differenceB.get(2, 0)[0];

		Entry<Point, Point> fingerPointProjected = new AbstractMap.SimpleEntry<Point, Point>(
				new Point(pointsDst.get(0, 0)), new Point(pointsDst.get(1, 0)));

		return Math.sqrt(differenceBsize);
	}


	/**
	 * @param rotationMatrix
	 * @param translationVectorVertical
	 * @param vectorSrcVertical 縦ベクトルでワールド座標系な3次元のベクトル。
	 * @return Vector型みたいなものとして返される投影された点
	 */
	public Mat projectPointWithVector(Mat rotationMatrix, Mat translationVectorVertical, Mat vectorSrcVertical) {
		Mat pointsDst = projectPoint(rotationMatrix, translationVectorVertical, vectorSrcVertical);

		Mat vectorDst = new Mat(new Size(1, 2), vectorSrcVertical.type());
		vectorDst.put(0, 0, pointsDst.get(0, 0));
		return vectorDst;
	}

	/**
	 * @param rotationMatrix
	 * @param translationVectorVertical
	 * @param vectorSrcVertical 縦ベクトルでワールド座標系な3次元のベクトル。
	 * @return そのまま返却
	 */
	public Mat projectPoint(Mat rotationMatrix, Mat translationVectorVertical, Mat vectorSrcVertical) {
		Point3 pointSrc = new Point3(vectorSrcVertical.get(0, 0)[0], vectorSrcVertical.get(1, 0)[0],
				vectorSrcVertical.get(2, 0)[0]);
		MatOfPoint3f pointsSrc = new MatOfPoint3f(pointSrc);
		MatOfPoint2f pointsDst = new MatOfPoint2f();
		Calib3d.projectPoints(pointsSrc, rotationMatrix, translationVectorVertical,
				cameraMatrix, distortionCoefficients, pointsDst);
		return pointsDst;
	}

	public Entry<Point, Point> projectPointsWithPoints(Mat rotationMatrix, Mat translationVectorVertical,
			Mat vectorAw, Mat vectorBw) {
		Mat pointsDst = projectPoints(rotationMatrix, translationVectorVertical, vectorAw, vectorBw);

		Entry<Point, Point> fingerPointProjected = new AbstractMap.SimpleEntry<Point, Point>(
				new Point(pointsDst.get(0, 0)), new Point(pointsDst.get(1, 0)));

		return fingerPointProjected;
	}

	public Mat projectPoints(Mat rotationMatrix, Mat translationVectorVertical, Mat vectorAw, Mat vectorBw) {
		Point3 pointAw = new Point3(vectorAw.get(0, 0)[0], vectorAw.get(1, 0)[0], vectorAw.get(2, 0)[0]);
		Point3 pointBw = new Point3(vectorBw.get(0, 0)[0], vectorBw.get(1, 0)[0], vectorBw.get(2, 0)[0]);
		MatOfPoint3f pointsSrc = new MatOfPoint3f(pointAw, pointBw);
		MatOfPoint2f pointsDst = new MatOfPoint2f();
		Calib3d.projectPoints(pointsSrc, rotationMatrix, translationVectorVertical,
				cameraMatrix, distortionCoefficients, pointsDst);
		return pointsDst;
	}

	/**
	 * Word座標への変換をする cf: matDst = R*matSrc + t
	 */
	public static void convertToWorldCoordinate(Mat matSrc, Mat rotationMatrix, Mat translationVector, Mat matDst) {
		Core.gemm(rotationMatrix, matSrc, 1, translationVector, 1, matDst);
	}

	/**
	 * @param matSrc
	 * @param rotationVector World→Cameraの回転行列
	 * @param translationVectorVertical 縦か横かわからん！
	 * @param matDst
	 */
	public static void unconvertToWorldCoordinate(Mat matSrc, Mat rotationVector, Mat translationVectorVertical,
			Mat matDst) {
		// Rodrigues行列の反転 (逆変換のため)
		Main.debugLog("rotationVector: " + rotationVector.dump(), LogLevel.INFO, "process | CalculatePoint");
//		Mat rotationVector_ = new Mat(new Size(1, 3),rotationVector.type());
//		rotationVector_ = rotationVector;
		Mat rotationVectorInv = rotationVector.mul(Mat.ones(rotationVector.size(), rotationVector.type()), -1);

		// 回転行列の逆行列をRodriges行列の反転したものから生成
		Mat rotationMatrixInv = new Mat(new Size(3, 3),rotationVector.type());
		Calib3d.Rodrigues(rotationVectorInv, rotationMatrixInv);

		Main.debugLog("R_inv: " + rotationMatrixInv.dump(), LogLevel.DEBUG, "convertToWorldCoordinate");

		unconvertToWorldCoordinateRotationInv(matSrc, rotationMatrixInv, translationVectorVertical, matDst);
	}

	public static void unconvertToWorldCoordinate_(Mat matSrc, Mat rotationMatrix, Mat translationVectorHorizontal,
			Mat matDst) {
		// 回転行列の逆行列は転置行列だと思うんですが。
		Mat rotationMatrix_ = new Mat(new Size(3, 3),rotationMatrix.type());
		rotationMatrix_ = rotationMatrix;
		Mat rotationMatrixInv = rotationMatrix_.inv(); // 逆行列だと思う

		Main.debugLog("R_inv: " + rotationMatrixInv.dump(), LogLevel.DEBUG, "convertToWorldCoordinate");

		unconvertToWorldCoordinateRotationInv(matSrc, rotationMatrixInv, translationVectorHorizontal, matDst);
	}

	public static void unconvertToWorldCoordinate__(Mat matSrc, Mat rotationMatrix, Mat translationVectorHorizontal,
			Mat matDst) {
		Mat rotationMatrixInv = rotationMatrix.t(); // 逆行列だと思う

		Main.debugLog("R_inv: " + rotationMatrixInv.dump(), LogLevel.DEBUG, "convertToWorldCoordinate");

		unconvertToWorldCoordinateRotationInv(matSrc, rotationMatrixInv, translationVectorHorizontal, matDst);
	}

	/**
	 * @param matSrc
	 * @param rotationMatrixInv Camera→World
	 * @param translationVectorVertical 縦か横かわからん！
	 * @param matDst
	 */
	public static void unconvertToWorldCoordinateRotationInv(Mat matSrc, Mat rotationMatrixInv,
			Mat translationVectorVertical, Mat matDst) {

		// dst = src - t
		Core.subtract(matSrc, translationVectorVertical, matDst);
		Main.debugLog("A-t: " + matDst.dump(), LogLevel.INFO, "process | CalculatePoint");

		// matDstは再利用物
		Core.gemm(rotationMatrixInv, matDst, 1, new Mat(), 0, matDst);
	}

	/**
	 * 横ベクトル(Horizontal)のtranslationVectorの縦ベクトル(Vertical)を返す。 転置ではあるが、なんか転置できないのでこれを作った。
	 *
	 * @param translationVectorHorizontal
	 * @return
	 */
	private static Mat convertVerticalTranslationVectorHorizontal(Mat translationVectorHorizontal) {
		if (translationVectorHorizontal.size().equals(new Size(1, 3))) {
			return translationVectorHorizontal;
		}

		Mat translationVectorVertical = new Mat(new Size(1, 3), CvType.CV_64FC1);
		for (int channel = 0; channel < translationVectorHorizontal.channels(); channel++) {
			translationVectorVertical.put(channel, 0, translationVectorHorizontal.get(0, 0)[channel]);
		}
		return translationVectorVertical;
	}

	/**
	 * 単位ベクトル(unit vector)を返します。
	 */
	public static Mat normalizeVector(Mat vector) {
		Mat dst = new Mat(vector.size(), vector.type());
		Core.normalize(vector, dst, 1.0, 0, Core.NORM_L2);
		return dst;
	}
}