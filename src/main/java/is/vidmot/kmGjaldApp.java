package is.vidmot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


/** ...
 *
 * Klassi sem hleður upp forritið og sýnir það í glugga með tilheyrandi scene.
 * Glugginn er fastur að stærð (960 x 520) og ekki hægt að breyta stærðinni.
 *
*/
public class kmGjaldApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(kmGjaldApp.class.getResource("kmGjald-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960, 520);
        stage.setTitle("Reiknivél fyrir Kílometrargjald");
        stage.setScene(scene);
        stage.setResizable(false); // Það er ekki hægt að stilla stærðinni.
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
