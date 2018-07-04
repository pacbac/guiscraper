package scraper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class Controller implements Initializable {

	@FXML BorderPane pane;
	@FXML HBox requestBox;
	@FXML Label header;
	@FXML Button getHTML;
	@FXML TextArea htmlOutput;
	@FXML TextField url;
	
	public void retrieveHTML() {
		String curl = "bash -c 'curl -L " + url.getText() + "'";
		Runtime run = Runtime.getRuntime();
		Process pr;
		try {
			pr = run.exec(curl);
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line;
			StringBuffer totalOut = new StringBuffer();
			while((line = buf.readLine()) != null)
				totalOut.append(line);
			htmlOutput.setText(totalOut.toString());
			//System.out.println(totalOut.toString());
		} catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		url.setPromptText("URL to scrape");
		htmlOutput.setEditable(false);
		htmlOutput.setPromptText("HTML output");
		htmlOutput.setWrapText(true);
		requestBox.setMargin(url, new Insets(5));
		pane.setMargin(pane.getTop(), new Insets(10));
	}
	
}
