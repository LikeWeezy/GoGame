package com.example.client;

import com.example.client.HttpClientHelper.GameInitResponse;
import com.example.client.HttpClientHelper.GameStatusResponse;
import com.example.client.HttpClientHelper.JoinGameResponse;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleUI {

    private final HttpClientHelper client;
    private final Scanner scanner;
    
    private String currentGameId;
    private int myPlayerId;      // 1 = Czarny 2 = Biały
    private String myColorName;  // "BLACK" lub "WHITE"

    public ConsoleUI() {
        this.client = new HttpClientHelper();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== GO GAME CLIENT ===");
        while (true) {
            System.out.println("\nWYBIERZ OPCJĘ:");
            System.out.println("[1] Nowa Gra");
            System.out.println("[2] Dołącz do Gry");
            System.out.println("[3] Wyjdź");
            System.out.print("> ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        handleNewGame();
                        break;
                    case "2":
                        handleJoinGame();
                        break;
                    case "3":
                        System.out.println("Do widzenia!");
                        return;
                    default:
                        System.out.println("Nieznana opcja.");
                }
            } catch (Exception e) {
                System.out.println("BŁĄD: " + e.getMessage());
                e.printStackTrace(); // do debugowania
            }
        }
    }

    // TWORZENIE GRY
    private void handleNewGame() throws IOException, InterruptedException {
        System.out.print("Podaj rozmiar planszy (np. 19): ");
        int size = Integer.parseInt(scanner.nextLine());
        
        // domyslnie zakladamy ze tworca chce byc czarny (mozna to zmienic)
        GameInitResponse response = client.createGame(size, "BLACK");
        
        this.currentGameId = response.gameId;
        this.myPlayerId = response.playerId;
        this.myColorName = "BLACK";
        
        System.out.println("GRA UTWORZONA!");
        System.out.println("ID GRY: " + response.gameId + " (Podaj to koledze!)");
        System.out.println("Jesteś graczem: CZARNYM (Zaczynasz)");
        
        runGameLoop();
    }

    // DOLACZANIE
    private void handleJoinGame() throws IOException, InterruptedException {
        System.out.print("Podaj ID gry (np. ABC-123): ");
        String gameId = scanner.nextLine(); 
    
        JoinGameResponse response = client.joinGame(gameId);
        
        this.currentGameId = gameId; 
        this.myPlayerId = response.playerId;
        this.myColorName = "WHITE";
        
        System.out.println("DOŁĄCZONO DO GRY!");
        System.out.println("Jesteś graczem: BIAŁYM (Czekasz na ruch Czarnego)");
        
        runGameLoop();
    }

    // GLOWNA PETLA GRY
    private void runGameLoop() throws IOException, InterruptedException {
        boolean running = true;
        int lastTurn = -1; // nie odswieza bezsensu ekranu jak sie nic nie zmienilo

        while (running) {
            // pobierz aktualny stan z serwera
            GameStatusResponse status = client.getStatus(currentGameId);

            // jesli gra sie skonczyla
            if ("FINISHED".equals(status.status)) {
                printBoard(status.boardState, status.boardSize);
                System.out.println("KONIEC GRY! Wygrał: " + (status.capturedBlack > status.capturedWhite ? "CZARNY" : "BIAŁY")); // Uproszczenie
                running = false;
                continue;
            }

            // 2. rysuj plansze (tylko jesli zmienila się tura lub stan)
            if (status.turn != lastTurn) {
                printBoard(status.boardState, status.boardSize);
                System.out.println("--------------------------------");
                System.out.println("Jeńcy Czarnego: " + status.capturedBlack + " | Jeńcy Białego: " + status.capturedWhite);
                System.out.println("Status: " + status.status);
                lastTurn = status.turn;
            }

            // 3. sprawdz czy moja tura
            if (status.turn == myPlayerId) {
                System.out.println("\n>>> TWOJA TURA (" + myColorName + ") <<<");
                handleMyMove();
                lastTurn = -1; // wymus odswieżenie po moim ruchu
            } else {
                // czekamy... (mały sleep)
                System.out.print("."); // kropka, zeby widac bylo, że dziala
                Thread.sleep(1000); 
            }
        }
    }

    // OBSLUGA RUCHU
    private void handleMyMove() {
        while (true) {
            System.out.print("Twój ruch (np. A1, PASS, SURRENDER): ");
            String input = scanner.nextLine().toUpperCase().trim();

            try {
                if (input.equals("PASS")) {
                    client.sendMove(currentGameId, myPlayerId, "PASS", -1, -1);
                    break;
                } else if (input.equals("SURRENDER")) {
                    client.sendMove(currentGameId, myPlayerId, "SURRENDER", -1, -1);
                    break;
                } else {
                    // parsowanie wspolrzędnych np. "C5"
                    // zakladamy format: Litera (kolumna) + Liczba (wiersz)
                    if (input.length() < 2) continue;

                    char colChar = input.charAt(0); // 'A', 'B'...
                    int x = colChar - 'A'; // 'A' -> 0, 'B' -> 1...
                    
                    
                    int y = Integer.parseInt(input.substring(1)) - 1; 

                    client.sendMove(currentGameId, myPlayerId, "PLACE", x, y);
                    break; // udalo się wyslać, wychodzimy z petli pytania
                }
            } catch (Exception e) {
                // to wyłapie blad z HttpClientHelper (np. 400 Bad Request - Pole zajęte)
                System.out.println("BŁĄD RUCHU: " + e.getMessage());
                System.out.println("Spróbuj ponownie.");
            }
        }
    }

    // RYSOWANIE PLANSZY (ASCII)
    private void printBoard(int[][] board, int size) {
        System.out.println("\n   ");
        // naglowki kolumn (A B C...)
        System.out.print("   ");
        for (int i = 0; i < size; i++) {
            System.out.print((char)('A' + i) + " ");
        }
        System.out.println();

        for (int y = 0; y < size; y++) {
            // numer wiersza (np. 1, 2...)
            System.out.printf("%2d ", (y + 1));
            
            for (int x = 0; x < size; x++) {
                int val = board[x][y];
                
                String symbol = "."; // Puste
                if (val == 1) symbol = "X"; // Czarny
                if (val == 2) symbol = "O"; // Biały
                
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
    }
}