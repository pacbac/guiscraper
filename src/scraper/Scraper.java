package scraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Scraper {
	
	private StringBuffer buf;
	private boolean excludeJS;
	private boolean excludeCSS;
	private Controller controller;
	private Stage stage;
	
	public Scraper(Stage stage, Controller controller) {
		this.stage = stage;
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
		if(url.equals("")) {
			controller.setErrorTxt("Error: No URL");
		}
		
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
		//save HTML to scraper's buffer
		buf = getHTML(url);
		
		//output JS to JS textbox
		{
			if(!this.excludeJS) {
				StringBuffer jsOut = parseTag("script");
				controller.getJSTextArea().setText(jsOut.toString());
			}
			removeElement("script");
		}
		
		//output CSS to CSS textbox
		{
			if(!this.excludeCSS) {
				StringBuffer cssOut = parseTag("style");
				controller.getCSSTextArea().setText(cssOut.toString());
			}
			removeElement("style");
		}
		
		buf = new StringBuffer(buf.toString().replaceAll(">[\\s]{2}", ">\n"));
		controller.getHTMLTextArea().setText(buf.toString());
		File tempOutput = new File("cmdout.txt");
		if(tempOutput.exists()) //delete file after temporary storage use
			tempOutput.delete();
	}
	
	public String saveFile() {
			DirectoryChooser choose = new DirectoryChooser();
			File dir = choose.showDialog(stage);
			//File error
			if(dir == null || !dir.isDirectory())
				return dir == null ? "Directory not chosen" : "Error: not a directory";
			else if(controller.getHTMLTextArea().getText().equals("")) //TextArea error
				return "Error: No content to save";
				
			String[] dirFiles = dir.list((directory, name) -> name.matches("^scrape[0-9]+[.]txt"));
			String fileNameMax = dirFiles.length > 0 ? dirFiles[dirFiles.length - 1] : "scrape0.txt";
			String sp = System.lineSeparator();
			int nextIndex = Integer.parseInt(fileNameMax.substring(6, fileNameMax.indexOf('.'))) + 1;
			String saveText = "HTML:" + sp + controller.getHTMLTextArea().getText() 
					+ (controller.getCSSTextArea().getText().equals("") ? "" : (sp + "CSS:" + sp + controller.getCSSTextArea().getText()))
					+ (controller.getJSTextArea().getText().equals("") ? "" : (sp + "JS:" + sp + controller.getJSTextArea().getText()));
			try {
				FileWriter filewrite = new FileWriter(new File(dir, "scrape"+nextIndex+".txt"));
				filewrite.write(saveText);
				filewrite.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "Error: Could not write to file" + e;
			}
			return "";
	}
	
	public void toggleExcludeJS() {
		this.excludeJS = !this.excludeJS;
	}
	
	public void toggleExcludeCSS() {
		this.excludeCSS = !this.excludeCSS;
	}
	
}