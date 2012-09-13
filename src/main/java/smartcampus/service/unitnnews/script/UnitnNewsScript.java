package smartcampus.service.unitnnews.script;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
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

	private static final String OPERAUNITN_PREFIX = "http://www.operauni.tn.it";

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

	public static NewsEntry extractOperaContent(Document doc) throws XPathExpressionException, ParserConfigurationException, UnsupportedEncodingException, DOMException, TransformerException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//div[@class=\"titleBlock\"]/div[@class=\"left\"]");
		String title = (String) expr.evaluate(doc, XPathConstants.STRING);
		xpath = xPathfactory.newXPath();
		expr = xpath.compile("//div[@class=\"txtGen\"]");
		Element content = (Element) expr.evaluate(doc, XPathConstants.NODE);

		String xmlString = cleanElement(content);

		NewsEntry.Builder result = NewsEntry.newBuilder();
		result.setTitle(title);
		result.setContent(xmlString);
		result.setSource("Opera Universitaria");

		return result.build();
	}

	// /////////////////////

	public static List<String> extractUnitnLinks(Document doc) throws XPathExpressionException, ParserConfigurationException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//div[@class=\"views-field-title\"]/span/a");

		ArrayList<String> result = new ArrayList<String>();
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			String link = ((Element) nodes.item(i)).getAttribute("href");
			result.add(link);
		}

		return result;
	}

	public static NewsEntry extractUnitnContent(Document doc, String source) throws XPathExpressionException, ParserConfigurationException, UnsupportedEncodingException, DOMException, TransformerException {
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
			entry.setTitle("");
			entry.setContent(xmlString);
			entry.setSource("Cisca");

			result.add(entry.build());
		}

		return result;
		
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
