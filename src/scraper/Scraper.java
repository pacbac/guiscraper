package scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Scraper extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GUI Scraper");
		Button btn = new Button();
		btn.setText("Retrieve HTML");
		btn.setOnAction(evt -> {
			String curl = "bash -c 'curl -L http://www.google.com'";
			Runtime run = Runtime.getRuntime();
			Process pr;
			try {
				pr = run.exec(curl);
				pr.waitFor();
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line;
				while((line = buf.readLine()) != null)
					System.out.println(line);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		StackPane root = new StackPane();
		Scene scene = new Scene(root, 400, 400);
		scene.getStylesheets().add("resources/style.css");
		primaryStage.setScene(scene);
		root.getChildren().add(btn);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
