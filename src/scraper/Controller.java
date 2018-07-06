package scraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Controller implements Initializable {

	@FXML private BorderPane pane;
	@FXML private VBox userStuff;
	@FXML private HBox requestBox;
	@FXML private HBox options;
	@FXML private Label header;
	@FXML private Button getHTML;
	@FXML private CheckBox JSBox;
	@FXML private CheckBox CSSBox;
	@FXML private TextArea htmlOutput;
	@FXML private TextArea cssOutput;
	@FXML private TextArea jsOutput;
	@FXML private TextField url;
	private boolean excludeJS;
	private boolean excludeCSS;
	
	public void removeElement(StringBuffer buf, String tag) {
		int startPos = buf.indexOf("<"+tag);
		int endPos = (buf.indexOf("</"+tag+">") >= 0) ? (buf.indexOf("</"+tag+">")+("</"+tag+">").length()) : buf.length();
		while(startPos >= 0) {
			buf.delete(startPos, endPos);
			startPos = buf.indexOf("<"+tag);
			endPos = (buf.indexOf("</"+tag+">") >= 0) ? (buf.indexOf("</"+tag+">")+("</"+tag+">").length()) : buf.length();
		}
	}
	
	public void retrieve() throws IOException, InterruptedException {
		//Get HTML
		String curl = "bash -c 'curl -L " + url.getText() + "' > cmdout.txt";
		Runtime run = Runtime.getRuntime();
		Process pr;
		pr = run.exec(curl);
		pr.waitFor();
		BufferedReader buf = new BufferedReader(new FileReader("cmdout.txt"));
		String line;
		StringBuffer totalOut = new StringBuffer();
		while((line = buf.readLine()) != null)
			totalOut.append(line);
		
		//output JS to JS textbox 
		if(!this.excludeJS) {
			StringBuffer tempTotal = new StringBuffer(totalOut);
			StringBuffer jsOut = new StringBuffer();
			int startPos = tempTotal.indexOf("<script");
			int endPos = (tempTotal.indexOf("</script>") >= 0) ? (tempTotal.indexOf("</script>")+("</script>").length()) : tempTotal.length();
			while(startPos >= 0) {
				jsOut.append(tempTotal.substring(startPos, endPos)+"\n");
				tempTotal.delete(0, endPos);
				startPos = tempTotal.indexOf("<script");
				endPos = (tempTotal.indexOf("/script>") >= 0) ? (tempTotal.indexOf("/script>")+("/script>").length()) : tempTotal.length();
			}
			jsOutput.setText(jsOut.toString());
		}
		removeElement(totalOut, "script");
		
		//output CSS to CSS textbox
		if(!this.excludeCSS) {
			StringBuffer tempTotal = new StringBuffer(totalOut);
			StringBuffer cssOut = new StringBuffer();
			int startPos = tempTotal.indexOf("<style");
			int endPos = (tempTotal.indexOf("</style>") >= 0) ? (tempTotal.indexOf("</style>")+("</style>").length()) : tempTotal.length();
			while(startPos >= 0) {
				cssOut.append(tempTotal.substring(startPos, endPos)+"\n");
				tempTotal.delete(0, endPos);
				startPos = tempTotal.indexOf("<style");
				endPos = (tempTotal.indexOf("/style>") >= 0) ? (tempTotal.indexOf("/style>")+("/style>").length()) : tempTotal.length();
			}
			cssOutput.setText(cssOut.toString());
		}
		removeElement(totalOut, "style");
		
		totalOut = new StringBuffer(totalOut.toString().replaceAll(">[\\s]{2}", ">\n"));
		htmlOutput.setText(totalOut.toString());
		//System.out.println(totalOut.toString());
		buf.close(); //close connection to file so delete will work
		File tempOutput = new File("cmdout.txt");
		if(tempOutput.exists())
			tempOutput.delete();
	}
	
	private void initOptions() {
		JSBox.setSelected(this.excludeJS = true);
		CSSBox.setSelected(this.excludeCSS = true);
		
		//toggle option boxes on click
		JSBox.setOnAction(evt -> {
			JSBox.setSelected(this.excludeJS = !this.excludeJS);
			jsOutput.setDisable(this.excludeJS);
			if(this.excludeJS)
				jsOutput.setText("");
		});
		CSSBox.setOnAction(evt -> {
			CSSBox.setSelected(this.excludeCSS = !this.excludeCSS);
			cssOutput.setDisable(this.excludeCSS);
			if(this.excludeCSS)
				cssOutput.setText("");
		});
		url.setOnKeyReleased(evt -> {
			if(evt.getCode().equals(KeyCode.ENTER))
				try {
					retrieve();
				} catch (Exception e) {
					e.printStackTrace();
				}
		});
	}
	
	private void initOutput() {
		htmlOutput.setEditable(false);
		htmlOutput.setPromptText("HTML output");
		htmlOutput.setWrapText(true);
		cssOutput.setEditable(false);
		cssOutput.setPromptText("CSS Output");
		cssOutput.setWrapText(true);
		jsOutput.setDisable(true);
		jsOutput.setEditable(false);
		jsOutput.setPromptText("JS Output");
		jsOutput.setWrapText(true);
		cssOutput.setDisable(true);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initOptions();
		initOutput();
		url.setPromptText("URL to scrape");
		requestBox.setMargin(url, new Insets(5));
		pane.setMargin(pane.getTop(), new Insets(10));
		userStuff.setMargin(options, new Insets(10));
	}
	
}
