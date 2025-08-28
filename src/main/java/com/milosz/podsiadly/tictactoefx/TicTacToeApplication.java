package com.milosz.podsiadly.tictactoefx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TicTacToeApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                TicTacToeApplication.class.getResource("tictactoe-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 420, 520);
        scene.getStylesheets().add(
                TicTacToeApplication.class.getResource("app.css").toExternalForm()
        );
        stage.setTitle("TicTacToe – JavaFX");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
