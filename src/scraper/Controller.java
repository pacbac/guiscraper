package scraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

	@FXML private BorderPane pane;
	@FXML private HBox requestBox;
	@FXML private Label header;
	@FXML private Button getHTML;
	@FXML private CheckBox noJSBox;
	@FXML private TextArea htmlOutput;
	@FXML private TextField url;
	private boolean excludeJS;
	
	public void retrieveHTML() throws IOException, InterruptedException {
		String curl = "bash -c 'curl -L " + url.getText() + "' > cmdout.txt";
		Runtime run = Runtime.getRuntime();
		Process pr;
		pr = run.exec(curl);
		pr.waitFor();
		BufferedReader buf = new BufferedReader(new FileReader("cmdout.txt"));
		String line;
		StringBuffer totalOut = new StringBuffer();
		boolean append = true;
		while((line = buf.readLine()) != null)
			totalOut.append(line);
		
		int startScriptPos = totalOut.indexOf("<script");
		int endScriptPos = (totalOut.indexOf("</script>") >= 0) ? (totalOut.indexOf("</script>")+"</script>".length()) : totalOut.length();
		while(this.excludeJS && startScriptPos >= 0) {
			totalOut.delete(startScriptPos, endScriptPos);
			startScriptPos = totalOut.indexOf("<script");
			endScriptPos = (totalOut.indexOf("</script>") >= 0) ? (totalOut.indexOf("</script>")+"</script>".length()) : totalOut.length();
		}
		
		htmlOutput.setText(totalOut.toString());
		//System.out.println(totalOut.toString());
		buf.close(); //close connection to file so delete will work
		File tempOutput = new File("cmdout.txt");
		if(tempOutput.exists())
			tempOutput.delete();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.excludeJS = true;
		noJSBox.setSelected(this.excludeJS);
		url.setPromptText("URL to scrape");
		htmlOutput.setEditable(false);
		htmlOutput.setPromptText("HTML output");
		htmlOutput.setWrapText(true);
		requestBox.setMargin(url, new Insets(5));
		pane.setMargin(pane.getTop(), new Insets(10));
		
		noJSBox.setOnAction(evt -> {
			this.excludeJS = !this.excludeJS;
			noJSBox.setSelected(this.excludeJS);
		});
	}
	
}
