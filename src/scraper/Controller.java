package scraper;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



public class Controller implements Initializable {

	@FXML private BorderPane pane;
	@FXML private VBox userStuff;
	@FXML private HBox requestBox;
	@FXML private HBox options;
	@FXML private Label header;
	@FXML private Button getHTML;
	@FXML private CheckBox JSBox;
	@FXML private CheckBox CSSBox;
	@FXML private Button save;
	@FXML private TextArea htmlOutput;
	@FXML private TextArea cssOutput;
	@FXML private TextArea jsOutput;
	@FXML private TextField url;
	@FXML private volatile Label errorMsg;
	@FXML private volatile Label loadingMsg;
	private Scraper scraper;
	private Stage stage;
	
	/**
	 * Create new thread for retrieving data so UI isn't disrupted
	 */
	public void retrieveThread() {
		Runnable runScrape = () -> {
			try {
				Platform.runLater(() -> { //enables non-FX application thread to update UI elements
					setLoadingTxt("Getting data...");
					setErrorTxt("");
				});
				scraper.retrieve(url.getText());
			} catch (Exception e) {
				e.printStackTrace();
				Platform.runLater(() -> setErrorTxt("Error: could not retrieve data"));
			}
			Platform.runLater(() -> setLoadingTxt(""));
		};
		Thread t = new Thread(runScrape);
		t.start();
	}
	
	private void setLoadingTxt(String text) {
		loadingMsg.setText(text);
	}
	
	public void setErrorTxt(String text) {
		errorMsg.setText(text);
	}
	
	private void initOptions() {
		JSBox.setSelected(true);
		CSSBox.setSelected(true);
		
		//toggle option boxes on click
		JSBox.setOnAction(evt -> {
			jsOutput.setDisable(JSBox.isSelected());
			scraper.toggleExcludeJS();
			if(JSBox.isSelected())
				jsOutput.setText("");
		});
		CSSBox.setOnAction(evt -> {
			cssOutput.setDisable(CSSBox.isSelected());
			scraper.toggleExcludeCSS();
			if(CSSBox.isSelected())
				cssOutput.setText("");
		});
		save.setOnAction(evt -> errorMsg.setText(scraper.saveFile()));
		url.setOnKeyReleased(evt -> {
			if(evt.getCode().equals(KeyCode.ENTER)) 
				retrieveThread();
		});
	}
	
	public TextArea getHTMLTextArea() {
		return htmlOutput;
	}
	
	public TextArea getCSSTextArea() {
		return cssOutput;
	}
	
	public TextArea getJSTextArea() {
		return jsOutput;
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
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		scraper = new Scraper(stage, this);
		initOptions();
		initOutput();
		loadingMsg.setAlignment(Pos.CENTER_RIGHT);
		url.setPromptText("URL to scrape");
		requestBox.setMargin(url, new Insets(5));
		pane.setMargin(pane.getTop(), new Insets(10));
		userStuff.setMargin(options, new Insets(10));
	}
	
}
