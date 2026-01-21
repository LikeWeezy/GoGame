package com.example.clientfx;

import com.example.client.HttpClientHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameView {

    private final GameSession session;
    private final ExecutorService ioPool;
    private final Runnable onBackToMenu;

    
    private final StackPane root = new StackPane();

    
    private final BorderPane main = new BorderPane();

    
    private final StackPane overlay = new StackPane();

    
    private final Label gameIdLabel = new Label();
    private final Label meLabel = new Label();
    private final Label turnLabel = new Label();
    private final Label statusLabel = new Label();
    private final Label capturedLabel = new Label();

    
    private final TextArea logArea = new TextArea();

   
    private final Button passBtn = new Button("PASS");
    private final Button surrenderBtn = new Button("SURRENDER");
    private final Button refreshBtn = new Button("Odśwież teraz");
    private final Button backBtn = new Button("Powrót do menu");

    // This player
    private final Label thisPlayersNegotiationLabel = new Label("Twoja propozycja:");

    private final Label thisPlayersLivingWhiteLabel = new Label("Żywe białe kamienie");
    private final TextArea thisPlayersLivingWhiteTextArea = new TextArea();
    private final Label thisPlayersDeadWhiteLabel = new Label("Martwe białe kamienie");
    private final TextArea thisPlayersDeadWhiteTextArea = new TextArea();
    private final Label thisPlayersWhiteTerritoryLabel = new Label("Terytorium białego");
    private final TextArea thisPlayersWhiteTerritoryTextArea = new TextArea();
    private final Label thisPlayersLivingBlackLabel = new Label("Żywe czarne kamienie");
    private final TextArea thisPlayersLivingBlackTextArea = new TextArea();
    private final Label thisPlayersDeadBlackLabel = new Label("Martwe czarne kamienie");
    private final TextArea thisPlayersDeadBlackTextArea = new TextArea();
    private final Label thisPlayersBlackTerritoryLabel = new Label("Terytorium czarnego");
    private final TextArea thisPlayersBlackTerritoryTextArea = new TextArea();

    private final Button submitNegotiationBtn = new Button("Wyślij propozycję");

    // Opponent
    private final Label opponentsNegotiationLabel = new Label("Propozycja przeciwnika:");

    private final Label opponentsLivingWhiteLabel = new Label("Żywe białe kamienie");
    private final TextArea opponentsLivingWhiteTextArea = new TextArea();
    private final Label opponentsDeadWhiteLabel = new Label("Martwe białe kamienie");
    private final TextArea opponentsDeadWhiteTextArea = new TextArea();
    private final Label opponentsWhiteTerritoryLabel = new Label("Terytorium białego");
    private final TextArea opponentsWhiteTerritoryTextArea = new TextArea();
    private final Label opponentsLivingBlackLabel = new Label("Żywe czarne kamienie");
    private final TextArea opponentsLivingBlackTextArea = new TextArea();
    private final Label opponentsDeadBlackLabel = new Label("Martwe czarne kamienie");
    private final TextArea opponentsDeadBlackTextArea = new TextArea();
    private final Label opponentsBlackTerritoryLabel = new Label("Terytorium czarnego");
    private final TextArea opponentsBlackTerritoryTextArea = new TextArea();

    private volatile boolean pollNegotiations = false;

    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();

    
    private GridPane boardGrid;
    private Circle[][] stones;

   
    private volatile HttpClientHelper.GameStatusResponse lastStatus;
    private volatile boolean finishedShown = false;

    public GameView(GameSession session, ExecutorService ioPool, Runnable onBackToMenu) {
        this.session = session;
        this.ioPool = ioPool;
        this.onBackToMenu = onBackToMenu;

        buildLayout();
        wireActions();

        
        root.getChildren().addAll(main, overlay);
        overlay.setVisible(false);
        overlay.setManaged(false);
    }

    public Parent getRoot() {
        return root;
    }

    public void startPolling() {
        refreshAsync();
        poller.scheduleAtFixedRate(this::refreshAsync, 1, 1, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        poller.shutdownNow();
    }

    private void buildLayout() {
        main.setPadding(new Insets(12));

        // top
        HBox top = new HBox(18);
        top.setPadding(new Insets(10));
        top.setAlignment(Pos.CENTER_LEFT);

        gameIdLabel.setText("gameId: " + session.gameId());
        meLabel.setText("ja: " + session.myColorName() + " (playerId=" + session.myPlayerId() + ")");
        turnLabel.setText("tura: ?");
        statusLabel.setText("status: ?");
        capturedLabel.setText("zbite: ?");

        top.getChildren().addAll(gameIdLabel, meLabel, turnLabel, statusLabel, capturedLabel);
        main.setTop(top);

        // right

        // This player
        setupNegotiationEntry(thisPlayersLivingWhiteTextArea, true);
        setupNegotiationEntry(thisPlayersDeadWhiteTextArea, true);
        setupNegotiationEntry(thisPlayersWhiteTerritoryTextArea, true);
        setupNegotiationEntry(thisPlayersLivingBlackTextArea, true);
        setupNegotiationEntry(thisPlayersDeadBlackTextArea, true);
        setupNegotiationEntry(thisPlayersBlackTerritoryTextArea, true);

        // Opponent
        setupNegotiationEntry(opponentsLivingWhiteTextArea, false);
        setupNegotiationEntry(opponentsDeadWhiteTextArea, false);
        setupNegotiationEntry(opponentsWhiteTerritoryTextArea, false);
        setupNegotiationEntry(opponentsLivingBlackTextArea, false);
        setupNegotiationEntry(opponentsDeadBlackTextArea, false);
        setupNegotiationEntry(opponentsBlackTerritoryTextArea, false);

        HBox negotiations = new HBox(2);

        VBox thisPlayersNegotiation = new VBox(8);

        thisPlayersNegotiation.getChildren().addAll(
                thisPlayersNegotiationLabel,
                thisPlayersLivingWhiteLabel, thisPlayersLivingWhiteTextArea,
                thisPlayersDeadWhiteLabel, thisPlayersDeadWhiteTextArea,
                thisPlayersWhiteTerritoryLabel, thisPlayersWhiteTerritoryTextArea,
                thisPlayersLivingBlackLabel, thisPlayersLivingBlackTextArea,
                thisPlayersDeadBlackLabel, thisPlayersDeadBlackTextArea,
                thisPlayersBlackTerritoryLabel, thisPlayersBlackTerritoryTextArea,
                submitNegotiationBtn
        );

        VBox opponentsNegotiation = new VBox(7);

        opponentsNegotiation.getChildren().addAll(
                opponentsNegotiationLabel,
                opponentsLivingWhiteLabel, opponentsLivingWhiteTextArea,
                opponentsDeadWhiteLabel, opponentsDeadWhiteTextArea,
                opponentsWhiteTerritoryLabel, opponentsWhiteTerritoryTextArea,
                opponentsLivingBlackLabel, opponentsLivingBlackTextArea,
                opponentsDeadBlackLabel, opponentsDeadBlackTextArea,
                opponentsBlackTerritoryLabel, opponentsBlackTerritoryTextArea
        );

        negotiations.getChildren().addAll(thisPlayersNegotiation, opponentsNegotiation);

        VBox right = new VBox(10);
        HBox actionButtons = new HBox(4);
        actionButtons.getChildren().addAll(passBtn, surrenderBtn, refreshBtn);
        right.setPadding(new Insets(10));
        right.getChildren().addAll(backBtn, new Separator(), actionButtons, new Separator(), negotiations);
        main.setRight(right);

        // bottom
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        main.setBottom(logArea);

        // center placeholder
        Label centerPlaceholder = new Label("Ładowanie planszy...");
        centerPlaceholder.setStyle("-fx-font-size: 16px;");
        StackPane center = new StackPane(centerPlaceholder);
        center.setPadding(new Insets(10));
        main.setCenter(center);

        log("Połączono. Odpytywanie: " + session.baseUrl());
    }

    private void wireActions() {
        refreshBtn.setOnAction(e -> refreshAsync());

        passBtn.setOnAction(e -> sendMoveAsync("PASS", -1, -1));
        surrenderBtn.setOnAction(e -> sendMoveAsync("SURRENDER", -1, -1));

        backBtn.setOnAction(e -> {
            stopPolling();
            onBackToMenu.run();
        });

        submitNegotiationBtn.setOnAction(e -> sendNegotiationAsync());
    }

    private void ensureBoardBuilt(int size) {
        if (boardGrid != null) return;

        boardGrid = new GridPane();
        boardGrid.setHgap(2);
        boardGrid.setVgap(2);
        boardGrid.setPadding(new Insets(10));

        stones = new Circle[size][size];

        // naglowki kolumn a b c ..
        for (int x = 0; x < size; x++) {
            Label lab = new Label(String.valueOf((char) ('A' + x)));
            lab.setMinWidth(28);
            lab.setAlignment(Pos.CENTER);
            boardGrid.add(lab, x + 1, 0);
        }

        // naglowki wierszy 1 2 ...
        for (int y = 0; y < size; y++) {
            Label lab = new Label(String.valueOf(y + 1));
            lab.setMinWidth(28);
            lab.setAlignment(Pos.CENTER_RIGHT);
            boardGrid.add(lab, 0, y + 1);
        }

        // pola planszy
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Rectangle bg = new Rectangle(28, 28);
                bg.setArcHeight(6);
                bg.setArcWidth(6);
                bg.setStyle("-fx-fill: #e6c98c; -fx-stroke: #8c6a2b;");

                Circle stone = new Circle(10);
                stone.setVisible(false);

                StackPane cell = new StackPane(bg, stone);
                cell.setMinSize(28, 28);

                final int fx = x;
                final int fy = y;

                cell.setOnMouseClicked(ev -> {
                    if (ev.getButton() != MouseButton.PRIMARY) return;
                    tryPlaceAt(fx, fy);
                });

                stones[x][y] = stone;
                boardGrid.add(cell, x + 1, y + 1);
            }
        }

        main.setCenter(boardGrid);
    }

    private void tryPlaceAt(int x, int y) {
        HttpClientHelper.GameStatusResponse st = lastStatus;
        if (st == null) {
            log("Jeszcze nie mam stanu gry.");
            return;
        }
        if (!"PLAYING".equals(st.status)) {
            log("Gra nie jest w stanie PLAYING.");
            return;
        }
        if (st.turn != session.myPlayerId()) {
            log("Nie Twoja tura.");
            return;
        }
        if (st.boardState[x][y] != 0) {
            log("Pole zajęte.");
            return;
        }

        sendMoveAsync("PLACE", x, y);
    }

    private void refreshAsync() {
        ioPool.submit(() -> {
            try {
                HttpClientHelper.GameStatusResponse st = session.client().getStatus(session.gameId());
                Platform.runLater(() -> applyStatus(st));
                if(pollNegotiations) {
                    HttpClientHelper.GetNegotiationDetailsRequest gnd =
                            new HttpClientHelper.GetNegotiationDetailsRequest(session.myPlayerId());
                    HttpClientHelper.GetNegotiationDetailsResponse neg =
                            session.client().askForNegotiation(session.gameId(), gnd);
                    System.out.println("ok2");
                    Platform.runLater(() -> applyEnemysNegotiationOffer(neg));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("Błąd odświeżania: " + ex.getMessage());
                    statusLabel.setText("status: (błąd połączenia)");
                });
            }
        });
    }

    private void sendMoveAsync(String type, int x, int y) {
        ioPool.submit(() -> {
            try {
                session.client().sendMove(session.gameId(), session.myPlayerId(), type, x, y);
                Platform.runLater(() -> log("Wysłano ruch: " + type + (type.equals("PLACE") ? (" (" + x + "," + y + ")") : "")));
                refreshAsync();
            } catch (Exception ex) {
                Platform.runLater(() -> log("Błąd ruchu: " + ex.getMessage()));
            }
        });
    }

    private void sendNegotiationAsync() {
        ioPool.submit(() -> {
            try {
                HttpClientHelper.NegotiateRequest nr = new HttpClientHelper.NegotiateRequest(
                        session.myPlayerId(),
                        Integer.parseInt(thisPlayersLivingWhiteTextArea.getText()),
                        Integer.parseInt(thisPlayersDeadWhiteTextArea.getText()),
                        Integer.parseInt(thisPlayersWhiteTerritoryTextArea.getText()),
                        Integer.parseInt(thisPlayersLivingBlackTextArea.getText()),
                        Integer.parseInt(thisPlayersDeadBlackTextArea.getText()),
                        Integer.parseInt(thisPlayersBlackTerritoryTextArea.getText())
                );
                session.client().sendNegotiation(session.gameId(), nr);
            }
            catch(NumberFormatException nfe) {
                Platform.runLater(() -> log("Zła wartość w propozycji: " + nfe.getMessage()));
            }
            catch(IOException | InterruptedException e) {
                Platform.runLater(() -> log("Błąd w wysyłaniu wiadomości: " + e.getMessage()));
            }
        });
    }

    private void applyStatus(HttpClientHelper.GameStatusResponse st) {
        this.lastStatus = st;

        ensureBoardBuilt(st.boardSize);

        turnLabel.setText("tura: " + (st.turn == 1 ? "BLACK" : "WHITE") + " (playerId=" + st.turn + ")");
        statusLabel.setText("status: " + st.status);
        capturedLabel.setText("zbite czarne=" + st.capturedBlack + " | zbite białe=" + st.capturedWhite);

        // serwer zwraca boardState[x][y]
        for (int y = 0; y < st.boardSize; y++) {
            for (int x = 0; x < st.boardSize; x++) {
                int v = st.boardState[x][y];
                Circle stone = stones[x][y];

                if (v == 0) {
                    stone.setVisible(false);
                } else {
                    stone.setVisible(true);
                    if (v == 1) {
                        stone.setStyle("-fx-fill: black;");
                    } else {
                        stone.setStyle("-fx-fill: white; -fx-stroke: black;");
                    }
                }
            }
        }

        if ("FINISHED".equals(st.status) && !finishedShown) {
            finishedShown = true;
            pollNegotiations = false;
            stopPolling();
            showFinishedOverlay(st);
        }
        else if("PAUSED".equals(st.status) && !pollNegotiations) {
            pollNegotiations = true;
        }
        else {
            pollNegotiations = false;
        }
    }


    private void applyEnemysNegotiationOffer(HttpClientHelper.GetNegotiationDetailsResponse gndres) {
        opponentsLivingWhiteTextArea.setText(gndres.livingWhite + "");
        opponentsDeadWhiteTextArea.setText(gndres.deadWhite + "");
        opponentsWhiteTerritoryTextArea.setText(gndres.whiteTerritory + "");
        opponentsLivingBlackTextArea.setText(gndres.livingBlack + "");
        opponentsDeadBlackTextArea.setText(gndres.deadBlack + "");
        opponentsBlackTerritoryTextArea.setText(gndres.blackTerritory + "");
    }

    
    private void showFinishedOverlay(HttpClientHelper.GameStatusResponse st) {
        overlay.getChildren().clear();

        
        Region dim = new Region();
        dim.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        dim.prefWidthProperty().bind(root.widthProperty());
        dim.prefHeightProperty().bind(root.heightProperty());

        
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setMaxWidth(420);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 16;
            -fx-border-radius: 16;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0.2, 0, 6);
        """);

        Label title = new Label("Gra zakończona");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        
        String winner = (st.winnerId == 1) ? "BLACK" : "WHITE";

        Label info = new Label(
                "Zbite białe: " + st.capturedWhite +
                        "\nŻyjące białe: " + thisPlayersLivingWhiteTextArea.getText() +
                        "\nMartwe białe: " + thisPlayersDeadWhiteTextArea.getText() +
                        "\nTerytorium białego: " + thisPlayersWhiteTerritoryTextArea.getText() +
                        "\n\nZbite czarne: " + st.capturedBlack +
                        "\nŻyjące czarne: " + thisPlayersLivingBlackTextArea.getText() +
                        "\nMartwe czarne: " + thisPlayersDeadBlackTextArea.getText() +
                        "\nTerytorium czarnego: " + thisPlayersBlackTerritoryTextArea.getText() +
                        "\n\n\nZwycięzca: " + winner
        );
        info.setStyle("-fx-font-size: 14px;");

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button back = new Button("Menu");
        Button close = new Button("Zamknij");

        back.setOnAction(e -> {
            overlay.setVisible(false);
            overlay.setManaged(false);
            onBackToMenu.run();
        });

        close.setOnAction(e -> {
            overlay.setVisible(false);
            overlay.setManaged(false);
        });

        buttons.getChildren().addAll(back, close);

        card.getChildren().addAll(title, info, new Separator(), buttons);

        overlay.getChildren().addAll(dim, card);
        StackPane.setAlignment(card, Pos.CENTER);

        overlay.setVisible(true);
        overlay.setManaged(true);
    }

    private void log(String msg) {
        logArea.appendText(msg + "\n");
    }

    private static void setupNegotiationEntry(TextArea ta, boolean editable) {
        ta.setEditable(editable);
        ta.setPrefRowCount(1);
        ta.setPrefColumnCount(2);
    }
}
