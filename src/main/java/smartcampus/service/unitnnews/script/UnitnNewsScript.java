/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package smartcampus.service.unitnnews.script;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.HtmlParser;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import smartcampus.service.unitnnews.data.message.Unitnnews.NewsEntry;

public class UnitnNewsScript {

	public static List<String> extractOperaLinks(Document doc) throws XPathExpressionException, ParserConfigurationException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//*[@id=\"content\"]/div[1]/ul/li/a[1]");

		ArrayList<String> result = new ArrayList<String>();
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			String link = ((Element) nodes.item(i)).getAttribute("href");
			result.add(link);
		}

		return result;
	}

	public static NewsEntry extractOperaContent(Document doc, String link) throws XPathExpressionException, ParserConfigurationException, UnsupportedEncodingException, DOMException, TransformerException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//div[@class=\"titleBlock\"]/div[@class=\"left\"]");
		String title = (String) expr.evaluate(doc, XPathConstants.STRING);
		xpath = xPathfactory.newXPath();
		expr = xpath.compile("//div[@class=\"txtGen\"]");
		Element content = (Element) expr.evaluate(doc, XPathConstants.NODE);

		String xmlString = cleanElement(content);

		// title = new String(title.getBytes("ISO-8859-1"), "UTF-8");
		// title = new String(title.getBytes(), "ISO-8859-1");

		NewsEntry.Builder result = NewsEntry.newBuilder();
		result.setTitle(title);
		result.setContent(xmlString);
		result.setSource("Opera Universitaria");
		result.setLink("http://www.operauni.tn.it"+link);

		return result.build();
	}

	// /////////////////////

	public static List<String> extractUnitnLinks(String s, String baseurl, String variableurl) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
		CleanerProperties props = new CleanerProperties();

		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		TagNode tagNode = new HtmlCleaner(props).clean(s);

		String ns = new PrettyXmlSerializer(props).getAsString(tagNode);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		UserAgentContext uacontext = new SimpleUserAgentContext();
		HtmlParser parser = new HtmlParser(uacontext, doc);
		parser.parse(new StringReader(ns));

		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//div[@class=\"views-field-title\"]/span/a");

		ArrayList<String> result = new ArrayList<String>();
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			String link = ((Element) nodes.item(i)).getAttribute("href");
			if (!link.startsWith("http://")) link = baseurl + link;
			result.add(link);
		}

		return result;
	}

	public static NewsEntry extractUnitnContent(Document doc, String source, String link) throws XPathExpressionException, ParserConfigurationException, UnsupportedEncodingException, DOMException, TransformerException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//*[@class=\"title\"]");
		String title = (String) expr.evaluate(doc, XPathConstants.STRING);
		xpath = xPathfactory.newXPath();
		expr = xpath.compile("//div[@class=\"node-inner\"]/div[@class=\"content\"]");
		Element content = (Element) expr.evaluate(doc, XPathConstants.NODE);

		String xmlString = cleanElement(content);

		NewsEntry.Builder result = NewsEntry.newBuilder();
		result.setTitle(title.trim());
		result.setContent(xmlString);
		result.setSource(source);
		result.setLink(link);
		
		return result.build();
	}

	public static List<NewsEntry> extractCiscaContent(Document doc) throws XPathExpressionException, ParserConfigurationException, UnsupportedEncodingException, DOMException, TransformerException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//table[@class=\"avviso\"]/tr/td");
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		List<NewsEntry> result = new ArrayList<NewsEntry>();

		for (int i = 0; i < nodes.getLength(); i++) {
			Element content = (Element) nodes.item(i);
			String xmlString = cleanElement(content);

			NewsEntry.Builder builder = NewsEntry.newBuilder();
			builder.setTitle("");
			builder.setContent(xmlString);
			builder.setSource("Cisca");

			result.add(builder.build());
		}

		return result;
	}

	public static List<NewsEntry> extractCiscaContent(String s) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {
		CleanerProperties props = new CleanerProperties();

		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);

		TagNode tagNode = new HtmlCleaner(props).clean(s);

		String ns = new PrettyXmlSerializer(props).getAsString(tagNode);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		UserAgentContext uacontext = new SimpleUserAgentContext();
		HtmlParser parser = new HtmlParser(uacontext, doc);
		parser.parse(new StringReader(ns));

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//table[@class=\"avviso\"]/tbody/tr/td");
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		List<NewsEntry> result = new ArrayList<NewsEntry>();

		System.out.println(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++) {
			Element content = (Element) nodes.item(i);
			String xmlString = cleanElement(content);

			NewsEntry.Builder entry = NewsEntry.newBuilder();

			int index = xmlString.indexOf(":");
			if (index == -1) {
				entry.setTitle(xmlString);
				entry.setContent("");
			} else {
				entry.setTitle(xmlString.substring(0, index));
				entry.setContent(xmlString.substring(index + 1).trim());
			}
			entry.setSource("Cisca");

			result.add(entry.build());
		}

		return result;

	}

	public static List<NewsEntry> extractDisiContent(Document doc) throws XPathExpressionException {
		List<NewsEntry> result = new ArrayList<NewsEntry>();

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		XPathExpression expr = xpath.compile("//div[@class=\"elem_news elem-even\"]");
		extractDisiContent(doc, expr, result);
		expr = xpath.compile("//div[@class=\"elem_news elem-odd\"]");
		extractDisiContent(doc, expr, result);

		return result;
	}

	private static void extractDisiContent(Document doc, XPathExpression expr, List<NewsEntry> result) throws XPathExpressionException {
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			NewsEntry.Builder builder = NewsEntry.newBuilder();
			builder.setSource("Disi");
			String date = "";
			String body = "";			
			for (int j = 0; j < element.getChildNodes().getLength(); j++) {
				if (element.getChildNodes().item(j) instanceof Element) {
					Element child = (Element) element.getChildNodes().item(j);
					if ("elem_title".equals(child.getAttribute("class"))) {
						builder.setTitle(child.getTextContent());
					} else if ("elem_body".equals(child.getAttribute("class"))) {
						body = child.getTextContent();
					} else if ("elem_date".equals(child.getAttribute("class"))) {
						date = child.getTextContent();
					}
				}
				builder.setContent(date + "\n" + body);
			}
			result.add(builder.build());
		}
	}

	private static String cleanElement(Element element) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult res = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(element);
		transformer.transform(source, res);
		String xmlString = res.getWriter().toString();

		xmlString = StringEscapeUtils.unescapeHtml(xmlString);

		xmlString = xmlString.replaceAll("<br/>", "\n");
		// xmlString = xmlString.replaceAll("<strong>", "+STRONG");
		// xmlString = xmlString.replaceAll("</strong>", "-STRONG");
		xmlString = xmlString.replaceAll("\\<.*?\\>", "");
		xmlString = xmlString.replaceAll("\r\n", "\n");
		xmlString = xmlString.replaceAll("[\n]+", " ");
		xmlString = xmlString.replaceAll("\\s+", " ");
		xmlString = xmlString.replaceAll("\\\"", "\"");
		xmlString = xmlString.trim();

		return xmlString;
	}

}
