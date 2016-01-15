package com.kaisquare.kainode.tester.jobs;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.kaisquare.kaisync.utils.AppLogger;


public class XMLBuilder {
	
	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	public static DocumentBuilder builder;
	public static Document document;
	private static Element root;
 
	public XMLBuilder(){
		try {
			builder = factory.newDocumentBuilder();
			document = builder.newDocument();
			root = document.createElement("testresults");
			document.appendChild(root);
			
		} catch (ParserConfigurationException e) {
			AppLogger.e("", "FAILED TO START BUILDER INSTANCE");
			e.printStackTrace();
		}
	}
	
	public void writeToRoot(Element child){
		root.appendChild(child);
	}
	public Element createChildElement(String name){
		Element child = document.createElement(name);
		
		return child;
	}
	public void writeElements(Element parent, Element child){
		
		parent.appendChild(child);
		
	}
	
	public Element writeAttributes(Element element, HashMap<String, String> attributes){
		
		for(String key : attributes.keySet()){
			element.setAttribute(key, attributes.get(key));
		}
		return element;
		
	}
	
	public Element writeContent (Element element, String[] text){
		String content = "";
		if(element.getTextContent() != null){
			content = element.getTextContent();
		}
		
		if(text.length > 1){
			for(int i = 0; i < text.length; i ++){
				
				if(i == text.length-1){
					content += text[i];
				}else{
					content += text[i] + ", ";
				}
				
			}
			element.setTextContent(content);
		}else{
			element.setTextContent(text[0]);
		}
		
		return element;
	}
	public Element writeCommands (Element element, String[] text){
		String content = "";
		if(element.getTextContent() != null){
			content = element.getTextContent();
		}
		
		if(text.length > 1){
			for(int i = 0; i < text.length; i ++){
				
				if(i == text.length-1){
					content += text[i];
				}else{
					content += text[i] + " ";
				}
				
			}
			element.setTextContent(content);
		}else{
			element.setTextContent(text[0]);
		}
		
		return element;
	}

	public boolean saveXML(Document document, String path){
		TransformerFactory factory = TransformerFactory.newInstance();
		boolean result = true;
		try {
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			Source src = new DOMSource(document);
			Result dest = new StreamResult(new File(path));
			transformer.transform(src, dest);
			
		}
		catch(TransformerConfigurationException e){
			result = false;
		}
		catch(TransformerException e){
			result = false;
		}
		return result;
	}
	
//	private Element create(String name, String content, Document document, String[] attribute){
//		Element itemElement = document.createElement(name);
//		if(attribute[0] != null){
//			itemElement.setAttribute(attribute[0], attribute[1]);
//		}
//		if(content != null){
//			itemElement.setTextContent(content);
//		}
//		return itemElement;
//	}
	
	public void convertXML(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy mm:hh:ss");
		Date dateFinish = new Date();
		
		try {

		    TransformerFactory tFactory = TransformerFactory.newInstance();

		    Transformer transformer =
		      tFactory.newTransformer
		         (new javax.xml.transform.stream.StreamSource
		            ("testXSL.xsl"));

		    transformer.transform
		      (new javax.xml.transform.stream.StreamSource
		            ("testXML.xml"),
		       new javax.xml.transform.stream.StreamResult
		            ( new FileOutputStream(dateFormat.format(dateFinish) + ".html")));
		    }
		  catch (Exception e) {
		    e.printStackTrace( );
		    }
	}

}
