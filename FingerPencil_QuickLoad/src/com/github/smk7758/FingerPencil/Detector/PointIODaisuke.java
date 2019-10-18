package com.github.smk7758.FingerPencil.Detector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opencv.core.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PointIODaisuke {
	public static List<Point> loadPoint(Path filePath){
		Document document = getDocument(filePath);
		Element root = document.getDocumentElement();

		NodeList rootNodeList = root.getChildNodes();
		List<Point> points = new ArrayList<>();
		for (int i = 0; i < rootNodeList.getLength(); i++) {
			if (rootNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				// if (rootNodeList.item(i).getNodeName().equals("DistortionCoefficients")) {
				// mats.put(rootNodeList.item(i).getNodeName() + "_",
				// getMatElementOfMatOfDouble(root, rootNodeList.item(i).getNodeName()));
				// }
				System.out.println(rootNodeList.item(i).getNodeName() + ", "
						+ getMatElement(root, rootNodeList.item(i).getNodeName()));
				points.add(getMatElement(root, rootNodeList.item(i).getNodeName()));
			}
		}
		return points;


	}

			private static Point getMatElement(Element root, String pointName) {
		NodeList pointDataRoot = root.getElementsByTagName(pointName);
		Element pointDataElement = (Element) pointDataRoot.item(0);
		Point point = new Point(0, 0);
//		final int x = Integer.valueOf(pointDataElement.getAttribute("{"));
//		final int y = Integer.valueOf(pointDataElement.getAttribute(","));
//		final int rows = Integer.valueOf(pointDataElement.getAttribute("rows"));
//		final int cols = Integer.valueOf(pointDataElement.getAttribute("cols"));
//		final int channels = Integer.valueOf(pointDataElement.getAttribute("channels"));
//		final int dims = Integer.valueOf(pointDataElement.getAttribute("dims")); // TODO
		String matDataString = pointDataElement.getTextContent();
		String pointDataString = pointDataElement.getTextContent();
		matDataString = matDataString.replaceAll("\\[", "").replaceAll("\\]", "").trim();
		pointDataString = pointDataString.replaceAll("\\{", "").replaceAll("\\}", "").trim();

		String[] pointDataSplitted = pointDataString.split(",");
//		String[] pointDataSplitted = matDataStringLines[row].split(",");
		for (int channel = 0; channel < 1; channel++) {
			Double pointData = Double.valueOf(pointDataSplitted[channel].trim());
			 double[] val = {pointData};
			point.set(val);

		}
		return point;


//		Mat mat = new Mat(rows, cols, CvType.CV_64FC(channels));
//		for (int row = 0; row < rows; row++) {
//			String[] matDataSplitted = matDataStringLines[row].split(",");
//
//			for (int col = 0; col < cols; col++) {
//				double[] matData = new double[channels];
//				for (int channel = 0; channel < channels; channel++) {
//					// System.out.println(matDataSplitted[col * channels + channel].trim());
//
//					matData[channel] = Double.valueOf(matDataSplitted[col * channels + channel].trim());
//
//					// System.out.println("matData[channel]: " + matData[channel]);
//				}
//
//				mat.put(row, col, matData);
//
//			}
//		}


	}

	private static Document getDocument(Path filePath) {
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(filePath.toString());
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return document;
	}

	public static void exportPoint(Map<String, Point> points, Path filePath) {
		try {
			Document document = getDocument();

			Element root = document.createElement("root");
			document.appendChild(root);

			for (Entry<String, Point> entryPoint : points.entrySet()) {
				final Element matData = setPointElement(document, entryPoint.getKey(), entryPoint.getValue());
				final Element pointData = setPointElement(document, entryPoint.getKey(), entryPoint.getValue());
				root.appendChild(pointData);
			}

			BufferedWriter bw = Files.newBufferedWriter(filePath);
			outputDocument(document, new StreamResult(bw));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static Element setPointElement(Document document, String pointName, Point point) {
		Element pointData = document.createElement(pointName);
//		pointData.setAttribute("rows", String.valueOf(mat.rows()));
//		pointData.setAttribute("cols", String.valueOf(mat.cols()));
//		pointData.setAttribute("channels", String.valueOf(mat.channels()));
//		pointData.setAttribute("dims", String.valueOf(mat.dims()));
//		pointData.setTextContent(mat.dump());
		pointData.setAttribute("x", String.valueOf(point.x));
		pointData.setAttribute("y", String.valueOf(point.y));
		pointData.setTextContent(point.toString());
		return pointData;
	}

	private static Document getDocument() {
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbuilder = null;
		try {
			docbuilder = dbfactory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		return docbuilder.newDocument();
	}

	private static void outputDocument(Document document, StreamResult streamResult) {
		try {
			TransformerFactory tfactory = TransformerFactory.newInstance();
			Transformer transformer = tfactory.newTransformer();
			// transformer.setOutputProperty("method", "html"); //�錾����
			transformer.setOutputProperty("indent", "yes"); // ���s�w��
			transformer.setOutputProperty("encoding", "SHIFT_JIS"); // encoding

			transformer.transform(new DOMSource(document), streamResult);
		} catch (TransformerConfigurationException ex) {
			ex.printStackTrace();
		} catch (TransformerException ex) {
			ex.printStackTrace();
		}
	}



}
