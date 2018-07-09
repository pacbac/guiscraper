package scraper;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException {
		primaryStage.setTitle("GUIScraper by Clayton Chu");
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("../resources/layout.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);
		scene.getStylesheets().add("resources/style.css");
		primaryStage.setScene(scene);
		primaryStage.show();
		Controller controller = loader.getController();
		controller.setStage(primaryStage);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
