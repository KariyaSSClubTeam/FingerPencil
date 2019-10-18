package com.github.smk7758.FingerPencil.Detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;

public class PointIO {

	public static Point loadRedPoint(String filePath, int i) {
		Path path1 = Paths.get(filePath);
		String red[];
		String red_x;
		String red_y;
		double redPoint_x;
		double redPoint_y;
		List<String> redlist = new ArrayList<>();
		Point redpoint = new Point();
		try (BufferedReader br1 = Files.newBufferedReader(path1)) {
			for (int k = 1; k <= i; k++) {
					redlist.add(br1.readLine());
			}
			red = redlist.get(i - 1).split(",");
			if(red.length == 0) {
				return null;
			}
			red_x = red[0];
			red_y = red[1];
			redPoint_x = Double.parseDouble(red_x);
			redPoint_y = Double.parseDouble(red_y);
			redpoint.x = redPoint_x;
			redpoint.y = redPoint_y;
			br1.close();
			return redpoint;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Point loadBluePoint(String filePath, int i) {
		Path path2 = Paths.get(filePath);
		String blue[];
		String blue_x;
		String blue_y;
		double bluePoint_x;
		double bluePoint_y;
		List<String> bluelist = new ArrayList<>();
		Point bluepoint = new Point();
		try (BufferedReader br2 = Files.newBufferedReader(path2)) {
			for (int k = 1; k <= i; k++) {
				String x = br2.readLine();
				bluelist.add(x);
			}
			blue = bluelist.get(i - 1).split(",");
			if(blue.length == 0) {
				return null;
			}
			blue_x = blue[0];
			blue_y = blue[1];
			bluePoint_x = Double.parseDouble(blue_x);
			bluePoint_y = Double.parseDouble(blue_y);
			bluepoint.x = bluePoint_x;
			bluepoint.y = bluePoint_y;
			br2.close();
			return bluepoint;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
// private static Point getMatElement(Element root, String pointName) {
// NodeList pointDataRoot = root.getElementsByTagName(pointName);
// Element pointDataElement = (Element) pointDataRoot.item(0);
// Point point = new Point(0, 0);
//// final int x = Integer.valueOf(pointDataElement.getAttribute("{"));
//// final int y = Integer.valueOf(pointDataElement.getAttribute(","));
//// final int rows = Integer.valueOf(pointDataElement.getAttribute("rows"));
//// final int cols = Integer.valueOf(pointDataElement.getAttribute("cols"));
//// final int channels = Integer.valueOf(pointDataElement.getAttribute("channels"));
//// final int dims = Integer.valueOf(pointDataElement.getAttribute("dims")); // TODO
// String matDataString = pointDataElement.getTextContent();
// String pointDataString = pointDataElement.getTextContent();
// matDataString = matDataString.replaceAll("\\[", "").replaceAll("\\]", "").trim();
// pointDataString = pointDataString.replaceAll("\\{", "").replaceAll("\\}", "").trim();
//
// String[] pointDataSplitted = pointDataString.split(",");
//// String[] pointDataSplitted = matDataStringLines[row].split(",");
// for (int channel = 0; channel < 1; channel++) {
// Double pointData = Double.valueOf(pointDataSplitted[channel].trim());
// double[] val = {pointData};
// point.set(val);
//
// }
// return point;
//
//
////
// }
//
// private static Document getDocument(Path filePath) {
//
// Document document = null;
// try {
// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
// DocumentBuilder builder = factory.newDocumentBuilder();
// document = builder.parse(filePath.toString());
// } catch (ParserConfigurationException ex) {
// ex.printStackTrace();
// } catch (SAXException ex) {
// ex.printStackTrace();
// } catch (IOException ex) {
// ex.printStackTrace();
// }
// return document;
// }
//
// public static void exportPoint(Map<String, Point> points, Path filePath) {
// try {
// Document document = getDocument();
//
// Element root = document.createElement("root");
// document.appendChild(root);
//
// for (Entry<String, Point> entryPoint : points.entrySet()) {
// final Element matData = setPointElement(document, entryPoint.getKey(), entryPoint.getValue());
// final Element pointData = setPointElement(document, entryPoint.getKey(), entryPoint.getValue());
// root.appendChild(pointData);
// }
//
// BufferedWriter bw = Files.newBufferedWriter(filePath);
// outputDocument(document, new StreamResult(bw));
// } catch (IOException ex) {
// ex.printStackTrace();
// }
// }
//
// private static Element setPointElement(Document document, String pointName, Point point) {
// Element pointData = document.createElement(pointName);
//// pointData.setAttribute("rows", String.valueOf(mat.rows()));
//// pointData.setAttribute("cols", String.valueOf(mat.cols()));
//// pointData.setAttribute("channels", String.valueOf(mat.channels()));
//// pointData.setAttribute("dims", String.valueOf(mat.dims()));
//// pointData.setTextContent(mat.dump());
// pointData.setAttribute("x", String.valueOf(point.x));
// pointData.setAttribute("y", String.valueOf(point.y));
// pointData.setTextContent(point.toString());
// return pointData;
// }
//
// private static Document getDocument() {
// DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
// DocumentBuilder docbuilder = null;
// try {
// docbuilder = dbfactory.newDocumentBuilder();
// } catch (ParserConfigurationException ex) {
// ex.printStackTrace();
// }
// return docbuilder.newDocument();
// }
//
// private static void outputDocument(Document document, StreamResult streamResult) {
// try {
// TransformerFactory tfactory = TransformerFactory.newInstance();
// Transformer transformer = tfactory.newTransformer();
// // transformer.setOutputProperty("method", "html"); //�錾����
// transformer.setOutputProperty("indent", "yes"); // ���s�w��
// transformer.setOutputProperty("encoding", "SHIFT_JIS"); // encoding
//
// transformer.transform(new DOMSource(document), streamResult);
// } catch (TransformerConfigurationException ex) {
// ex.printStackTrace();
// } catch (TransformerException ex) {
// ex.printStackTrace();
// }
// }
