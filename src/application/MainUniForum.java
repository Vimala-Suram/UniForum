package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainUniForum extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/edu/northeastern/uniforum/forum/view/forum.fxml")
            );

            Scene scene = new Scene(root, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("UniForum - Forum");
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
