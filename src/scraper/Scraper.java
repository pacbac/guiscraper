package scraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Scraper {
	
	private StringBuilder buf;
	private boolean excludeJS;
	private boolean excludeCSS;
	private Controller controller;
	private Stage stage;
	
	public Scraper(Stage stage, Controller controller) {
		this.stage = stage;
		this.controller = controller;
		this.excludeJS = this.excludeCSS = true;
		buf = new StringBuilder();
	}
	
	private void removeElement(String tag) {
		int startPos = buf.indexOf("<"+tag);
		int endPos = (buf.indexOf("</"+tag+">") >= 0) ? (buf.indexOf("</"+tag+">")+("</"+tag+">").length()) : buf.length();
		while(startPos >= 0 && endPos > startPos) {
			buf.delete(startPos, endPos);
			startPos = buf.indexOf("<"+tag);
			endPos = (buf.indexOf("</"+tag+">") >= 0) ? (buf.indexOf("</"+tag+">")+("</"+tag+">").length()) : buf.length();
		}
	}
	
	public StringBuilder getHTML(String url) throws IOException, InterruptedException {
		if(url.equals("")) {
			controller.setErrorTxt("Error: No URL");
			return new StringBuilder();
		}
		
		String curl = "bash -c 'curl -L " + url + "' > cmdout.txt";
		Runtime run = Runtime.getRuntime();
		Process pr;
		pr = run.exec(curl);
		pr.waitFor();
		BufferedReader buffer = new BufferedReader(new FileReader("cmdout.txt"));
		String line;
		StringBuilder totalOut = new StringBuilder();
		while((line = buffer.readLine()) != null)
			totalOut.append(line);
		buffer.close(); //close connection to file so delete will work
		
		return totalOut;
	}
	
	private StringBuilder parseTag(String tag) {
		StringBuilder out = new StringBuilder(); //output buffer
		Stack<Integer> trackNested = new Stack<Integer>(); //track nested elements with push/pop
		int startPos = buf.indexOf("<"+tag);
		int endPos = (buf.indexOf("</"+tag+">") >= 0) ? (buf.indexOf("</"+tag+">")+("</"+tag+">").length()) : buf.length();
		while(startPos >= 0) {
			if(startPos < endPos) {
				trackNested.push(startPos);
				startPos = buf.indexOf("<"+tag, startPos+("<"+tag).length());
			} else {
				while(!trackNested.isEmpty()) {
					out.append(buf.substring(trackNested.pop(), endPos)+"\n");
					endPos = (buf.indexOf("</"+tag+">", endPos) >= 0) ? (buf.indexOf("</"+tag+">", endPos)+("</"+tag+">").length()) : buf.length();
				}
				endPos = (buf.indexOf("</"+tag+">", startPos) >= 0) ? (buf.indexOf("</"+tag+">", startPos)+("</"+tag+">").length()) : buf.length();
			}
		}
		return out;
	}
	
	public void retrieve(String url) throws IOException, InterruptedException {
		//save HTML to scraper's buffer
		buf = getHTML(url);
		
		//output JS to JS textbox
		{
			if(!this.excludeJS) {
				StringBuilder jsOut = parseTag("script");
				controller.getJSTextArea().setText(jsOut.toString());
			}
			removeElement("script");
		}
		
		//output CSS to CSS textbox
		{
			if(!this.excludeCSS) {
				StringBuilder cssOut = parseTag("style");
				controller.getCSSTextArea().setText(cssOut.toString());
			}
			removeElement("style");
		}
		
		buf = new StringBuilder(buf.toString().replaceAll(">[\\s]{2}", ">\n"));
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
	
	public String search(String tag) {
		String[] tagList = tag.split(" ");
		StringBuilder result = new StringBuilder();
		for(String tagElem : tagList) {
			if(buf.indexOf("<"+tagElem+">") == -1 && buf.indexOf("<"+tagElem+" ") == -1) continue;
			StringBuilder tagBuf = parseTag(tagElem);
			result.append(tagBuf);
		}
		return result.toString();
	}
	
	public void toggleExcludeJS() {
		this.excludeJS = !this.excludeJS;
	}
	
	public void toggleExcludeCSS() {
		this.excludeCSS = !this.excludeCSS;
	}
	
}