package com.example.clientfx;

import com.example.client.HttpClientHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class MainMenuView {

    private final ExecutorService ioPool;
    private final Consumer<GameSession> onSessionReady;

    private final VBox root = new VBox(12);

    private final TextField serverUrlField = new TextField("http://localhost:8080");

    private final TextField boardSizeField = new TextField("19");
    private final ChoiceBox<String> colorChoice = new ChoiceBox<>();

    private final TextField joinGameIdField = new TextField();

    private final TextField replayGameIdField = new TextField();

    private final Label statusLabel = new Label("");

    private final Button createBtn = new Button("Nowa gra");

    private final Button createBotBtn = new Button("Nowa gra z botem");

    private final Button joinBtn = new Button("Dołącz do gry");
    private final Button replayBtn = new Button("Odtwórz grę");

    public MainMenuView(ExecutorService ioPool, Consumer<GameSession> onSessionReady) {
        this.ioPool = ioPool;
        this.onSessionReady = onSessionReady;

        colorChoice.getItems().addAll("BLACK", "WHITE");
        colorChoice.setValue("BLACK");

        joinGameIdField.setPromptText("Wpisz gameId (np. 0, 1, 2...)");
        replayGameIdField.setPromptText("Wpisz gameId (np. 0, 1, 2...)");

        buildLayout();
        wireActions();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildLayout() {
        root.setPadding(new Insets(18));

        Label title = new Label("GO GAME (JavaFX Client)");
        title.setFont(Font.font(22));

        // sekcja serwera
        VBox serverBox = new VBox(8,
                new Label("Adres serwera:"),
                serverUrlField
        );

        // sekcja tworzenia gry
        GridPane createGrid = new GridPane();
        createGrid.setHgap(10);
        createGrid.setVgap(10);

        createGrid.add(new Label("Rozmiar planszy:"), 0, 0);
        createGrid.add(boardSizeField, 1, 0);

        createGrid.add(new Label("Twój kolor:"), 0, 1);
        createGrid.add(colorChoice, 1, 1);

        VBox createBox = new VBox(10,
            new Label("Nowa gra"),
            createGrid,
            new HBox(10, createBtn, createBotBtn)
        );

        createBox.setPadding(new Insets(12));
        createBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8;");

        // sekcja dolaczania
        VBox joinBox = new VBox(10,
                new Label("Dołącz do gry"),
                joinGameIdField,
                joinBtn
        );
        joinBox.setPadding(new Insets(12));
        joinBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8;");

        // sekcja odtwarzania
        VBox replayBox = new VBox(
                10,
                new Label("Odtwórz grę"),
                replayGameIdField,
                replayBtn
        );
        replayBox.setPadding(new Insets(12));
        replayBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8;");

        statusLabel.setStyle("-fx-text-fill: #b00020;");

        root.getChildren().addAll(title, serverBox, createBox, joinBox, replayBox, statusLabel);
    }

    private void wireActions() {
        createBtn.setOnAction(e -> createGame());
        createBotBtn.setOnAction(e -> createGameWithBot());
        joinBtn.setOnAction(e -> joinGame());
        replayBtn.setOnAction(e -> replayGame());
    }


    private void setBusy(boolean busy) {
        createBtn.setDisable(busy);
        createBotBtn.setDisable(busy);
        joinBtn.setDisable(busy);
    }


    private void createGame() {
        String baseUrl = serverUrlField.getText().trim();
        String color = colorChoice.getValue();
        int size;

        try {
            size = Integer.parseInt(boardSizeField.getText().trim());
            if (size < 2 || size > 50) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            statusLabel.setText("Błąd: rozmiar planszy musi być liczbą (sensownie: 9/13/19).");
            return;
        }

        statusLabel.setText("");
        setBusy(true);

        ioPool.submit(() -> {
            try {
                HttpClientHelper helper = new HttpClientHelper(baseUrl);
                HttpClientHelper.GameInitResponse resp = helper.createGame(size, color);

                GameSession session = new GameSession(helper, baseUrl, resp.gameId, resp.playerId, false, null);

                Platform.runLater(() -> {
                    setBusy(false);
                    onSessionReady.accept(session);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setBusy(false);
                    statusLabel.setText("Nie udało się utworzyć gry: " + ex.getMessage());
                });
            }
        });
    }

    private void createGameWithBot() {
        String baseUrl = serverUrlField.getText().trim();
        String color = colorChoice.getValue();
        int size;

        try {
            size = Integer.parseInt(boardSizeField.getText().trim());
            if (size < 2 || size > 50) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            statusLabel.setText("Błąd: rozmiar planszy musi być liczbą (sensownie: 9/13/19).");
            return;
        }

        statusLabel.setText("");
        setBusy(true);

        ioPool.submit(() -> {
            try {
                HttpClientHelper helper = new HttpClientHelper(baseUrl);

                HttpClientHelper.GameInitResponse resp = helper.createGame(size, color);

                helper.joinBot(resp.gameId);

                GameSession session = new GameSession(helper, baseUrl, resp.gameId, resp.playerId);

                Platform.runLater(() -> {
                    setBusy(false);
                    onSessionReady.accept(session);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setBusy(false);
                    statusLabel.setText("Nie udało się utworzyć gry z botem: " + ex.getMessage());
                });
            }
        });
    }


    private void joinGame() {
        String baseUrl = serverUrlField.getText().trim();
        String gameId = joinGameIdField.getText().trim();

        if (gameId.isEmpty()) {
            statusLabel.setText("Błąd: wpisz gameId.");
            return;
        }

        statusLabel.setText("");
        setBusy(true);

        ioPool.submit(() -> {
            try {
                HttpClientHelper helper = new HttpClientHelper(baseUrl);
                HttpClientHelper.JoinGameResponse resp = helper.joinGame(gameId);

                GameSession session = new GameSession(helper, baseUrl, gameId, resp.playerId, false, null);

                Platform.runLater(() -> {
                    setBusy(false);
                    onSessionReady.accept(session);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setBusy(false);
                    statusLabel.setText("Nie udało się dołączyć: " + ex.getMessage());
                });
            }
        });
    }

    private void replayGame() {
        String baseUrl = serverUrlField.getText().trim();
        String gameId = replayGameIdField.getText().trim();

        if (gameId.isEmpty()) {
            statusLabel.setText("Błąd: wpisz gameId.");
            return;
        }

        statusLabel.setText("");
        setBusy(true);

        ioPool.submit(() -> {
            try {
                HttpClientHelper helper = new HttpClientHelper(baseUrl);
                HttpClientHelper.ReplayGameResponse replayResp = helper.replayGame(gameId);
                HttpClientHelper.GameInitResponse createResp = helper.createGame(replayResp.boardSize, "BLACK");
                HttpClientHelper.JoinGameResponse joinResp = helper.joinGame(createResp.gameId);

                GameSession session = new GameSession(helper, baseUrl, createResp.gameId, 1, true, replayResp);

                Platform.runLater(() -> {
                    setBusy(false);
                    onSessionReady.accept(session);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setBusy(false);
                    statusLabel.setText("Nie udało się odtworzyć: " + ex.getMessage());
                });
            }
        });
    }

}
