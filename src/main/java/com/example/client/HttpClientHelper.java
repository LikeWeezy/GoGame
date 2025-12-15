package com.example.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class HttpClientHelper {

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public HttpClientHelper() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        // zakladamy ze serwer dziala lokalnie na porcie 8080
        this.baseUrl = "http://localhost:8080"; 
    }

    // TWORZENIE GRY
     public GameInitResponse createGame(int size, String color) throws IOException, InterruptedException {
        String url = baseUrl + "/api/new_game";
        
        // tworzenie obiektu żądania
        CreateGameRequest requestBody = new CreateGameRequest(size, color);
        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Błąd tworzenia gry: " + response.body());
        }

        return mapper.readValue(response.body(), GameInitResponse.class);
    }
    
    /* 
    // createGame MOCK
    public GameInitResponse createGame(int size, String color) {
        GameInitResponse fake = new GameInitResponse();
        fake.gameId = "TEST-123";
        fake.playerId = 1;
        fake.status = "WAITING";
        return fake;
    } 
    */

    // DOLACZANIE DO GRY
    public JoinGameResponse joinGame(String gameId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/game/" + gameId + "/join";
        
        // Puste body, bo join nie wymaga danych wejściowych w JSON wg protokołu
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody()) 
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Nie udało się dołączyć: " + response.body());
        }

        return mapper.readValue(response.body(), JoinGameResponse.class);
    }
     
    // POBIERANIE STATUSU
    public GameStatusResponse getStatus(String gameId) throws IOException, InterruptedException {
        String url = baseUrl + "/api/game/" + gameId + "/status";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Błąd pobierania statusu: " + response.body());
        }

        return mapper.readValue(response.body(), GameStatusResponse.class);
    }
    
    /*   
    // getStatus MOCK
    public GameStatusResponse getStatus(String gameId) {
        System.out.println("(SYMULACJA) Pobieranie statusu gry z serwera...");
        
        GameStatusResponse fakeResponse = new GameStatusResponse();
        fakeResponse.gameId = gameId;
        fakeResponse.boardSize = 19;
        fakeResponse.status = "PLAYING";
        fakeResponse.turn = 1; // Tura Czarnego
        fakeResponse.capturedBlack = 2;
        fakeResponse.capturedWhite = 5;

        
        fakeResponse.boardState = new int[19][19];
        
        fakeResponse.boardState[3][3] = 1; // Czarny na D4
        fakeResponse.boardState[10][10] = 2; // Biały na K11
        fakeResponse.boardState[0][0] = 1; // Czarny w rogu A1
        fakeResponse.boardState[18][18] = 2; // Biały w rogu S19

        return fakeResponse;
    }  
    */

    // WYSYLANIE RUCHU
    public void sendMove(String gameId, int playerId, String moveType, int x, int y) throws IOException, InterruptedException {
        String url = baseUrl + "/api/game/" + gameId + "/move";

        MoveRequest moveBody = new MoveRequest(playerId, moveType, x, y);
        String jsonBody = mapper.writeValueAsString(moveBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            // rzucamy wyjatek jeśli ruch jest niepoprawny (np pole zajete)
            // Dzieki temu w ConsoleUI mozna zlapac ten wyjatek i wyswietlic komunikat
            throw new RuntimeException(response.body()); 
        }
    }

    // klasy DTO (Data Transfer Objects)
    // mapowanie json -> java

    // do wysyłania żądania o nowa grę
    public static class CreateGameRequest {
        public int boardSize;
        public String requestedColor;

        public CreateGameRequest(int boardSize, String requestedColor) {
            this.boardSize = boardSize;
            this.requestedColor = requestedColor;
        }
    }

    // do odbierania odpowiedzi po utworzeniu/dołączeniu (gameId, playerId)
    public static class GameInitResponse {
        public String gameId;
        public int playerId; // 1 lub 2
        // pusty konstruktor potrzebny dla Jacksona
        public GameInitResponse() {} 
    }

    // do odbierania danych po dołączeniu do gry
    public static class JoinGameResponse {
        public int playerId; // 1 lub 2
        public JoinGameResponse() {}
    }

    // do odbierania pełnego stanu gry
    public static class GameStatusResponse {
        public String gameId;
        public int boardSize;
        public int[][] boardState; // Tablica 2D [0,1,2]
        public int turn;           // Czyja tura (1 lub 2)
        public int capturedBlack;
        public int capturedWhite;
        public String status;      // PLAYING, FINISHED
        
        public GameStatusResponse() {}
    }

    // Do wysylania ruchu
    public static class MoveRequest {
        public int playerId;
        public String moveType; // "PLACE", "PASS", "SURRENDER"
        public int x;
        public int y;

        public MoveRequest(int playerId, String moveType, int x, int y) {
            this.playerId = playerId;
            this.moveType = moveType;
            this.x = x;
            this.y = y;
        }
    }
}