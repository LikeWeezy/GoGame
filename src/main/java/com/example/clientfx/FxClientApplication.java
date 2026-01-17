package com.example.clientfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FxClientApplication extends Application {

    private final ExecutorService ioPool = Executors.newCachedThreadPool();
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Go Game - JavaFX Client");
        showMainMenu();
        stage.show();
    }

    private void showMainMenu() {
        MainMenuView menu = new MainMenuView(ioPool, session -> showGame(session));
        Scene scene = new Scene(menu.getRoot(), 1000, 700);
        stage.setScene(scene);
    }

    private void showGame(GameSession session) {
        GameView gameView = new GameView(session, ioPool, this::showMainMenu);
        Scene scene = new Scene(gameView.getRoot(), 1100, 800);
        stage.setScene(scene);
        gameView.startPolling();
    }

    @Override
    public void stop() {
        ioPool.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
