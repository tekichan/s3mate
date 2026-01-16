package self.tekichan.s3mate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for JavaFX Application
 * <p>
 *     The application starts with MainApp which
 *     extends JavaFX Application abstract class,
 *     and loads fxml and css files for the GUI layout.
 * </p>
 */
public class MainApp extends Application {
    private static final int APP_WIDTH = 900;
    private static final int APP_WEIGHT = 520;
    private static final String MAIN_FXML_PATH = "/ui/main.fxml";
    private static final String MAIN_CSS_PATH = "/ui/style.css";
    private static final String APP_TITLE = "S3Mate";

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource(MAIN_FXML_PATH)
        );

        Scene scene = new Scene(loader.load(), APP_WIDTH, APP_WEIGHT);
        scene.getStylesheets().add(
                MainApp.class.getResource(MAIN_CSS_PATH).toExternalForm()
        );

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
