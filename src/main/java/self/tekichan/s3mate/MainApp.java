package self.tekichan.s3mate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/ui/main.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 520);
        scene.getStylesheets().add(
                MainApp.class.getResource("/ui/style.css").toExternalForm()
        );

        stage.setTitle("S3Mate");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
