package es.jmcenram.blockchain;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource("/view/login.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 300, 300);
        stage.setTitle("Proyecto Blockchain");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}