package scraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javafx.stage.Stage;

public class Scraper {
	
	private StringBuffer buf;
	private boolean excludeJS;
	private boolean excludeCSS;
	private Controller controller;
	
	public Scraper(Stage stage, Controller controller) {
		this.controller = controller;
		this.excludeJS = this.excludeCSS = true;
		buf = new StringBuffer();
	}
	
	private void removeElement(String tag) {
		int startPos = buf.indexOf("<"+tag);
		int endPos = (buf.indexOf("</"+tag+">") >= 0) ? (buf.indexOf("</"+tag+">")+("</"+tag+">").length()) : buf.length();
		while(startPos >= 0) {
			buf.delete(startPos, endPos);
			startPos = buf.indexOf("<"+tag);
			endPos = (buf.indexOf("</"+tag+">") >= 0) ? (buf.indexOf("</"+tag+">")+("</"+tag+">").length()) : buf.length();
		}
	}
	
	public StringBuffer getHTML(String url) throws IOException, InterruptedException {
		String curl = "bash -c 'curl -L " + url + "' > cmdout.txt";
		Runtime run = Runtime.getRuntime();
		Process pr;
		pr = run.exec(curl);
		pr.waitFor();
		BufferedReader buffer = new BufferedReader(new FileReader("cmdout.txt"));
		String line;
		StringBuffer totalOut = new StringBuffer();
		while((line = buffer.readLine()) != null)
			totalOut.append(line);
		buffer.close(); //close connection to file so delete will work
		
		return totalOut;
	}
	
	private StringBuffer parseTag(String tag) {
		StringBuffer tempTotal = new StringBuffer(buf); //temporary buffer w/ everything (that will be manipulated)
		StringBuffer out = new StringBuffer(); //output buffer
		int startPos = tempTotal.indexOf("<"+tag);
		int endPos = (tempTotal.indexOf("</"+tag+">") >= 0) ? (tempTotal.indexOf("</"+tag+">")+("</"+tag+">").length()) : tempTotal.length();
		while(startPos >= 0) {
			out.append(tempTotal.substring(startPos, endPos)+"\n");
			tempTotal.delete(0, endPos);
			startPos = tempTotal.indexOf("<"+tag);
			endPos = (tempTotal.indexOf("</"+tag+">") >= 0) ? (tempTotal.indexOf("</"+tag+">")+("</"+tag+">").length()) : tempTotal.length();
		}
		return out;
	}
	
	public void retrieve(String url) throws IOException, InterruptedException {
		//Get HTML
		buf = getHTML(url);
		
		//output JS to JS textbox 
		if(!this.excludeJS) {
			StringBuffer jsOut = parseTag("script");
			controller.getJSTextArea().setText(jsOut.toString());
		}
		removeElement("script");
		
		//output CSS to CSS textbox
		if(!this.excludeCSS) {
			StringBuffer cssOut = parseTag("style");
			controller.getCSSTextArea().setText(cssOut.toString());
		}
		removeElement("style");
		
		buf = new StringBuffer(buf.toString().replaceAll(">[\\s]{2}", ">\n"));
		controller.getHTMLTextArea().setText(buf.toString());
		File tempOutput = new File("cmdout.txt");
		if(tempOutput.exists())
			tempOutput.delete();
	}
	
}