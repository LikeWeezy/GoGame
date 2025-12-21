package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import com.example.server.memorydata.datatypes.dtos.response.JoinGameResponseDTO;
import org.springframework.http.ResponseEntity;

public class Game {
    private final String gameId;
    private final int boardSize;
    private int[][] boardState;
    private int turn = 1; // Startujemy od 1 (Czarny)
    private int capturedBlack = 0;
    private int capturedWhite = 0;
    private GameStatus status;
    private final int creatingPlayerId;

    public Game(String gameId, int boardSize, int[][] boardState, int turn, int capturedBlack, int capturedWhite, GameStatus status, int creatingPlayerId) {
        this.gameId = gameId;
        this.boardSize = boardSize;
        this.boardState = boardState;
        this.turn = (turn == 0) ? 1 : turn;
        this.capturedBlack = capturedBlack;
        this.capturedWhite = capturedWhite;
        this.status = status;
        this.creatingPlayerId = creatingPlayerId;
    }

    
    public String getGameId() { return this.gameId; }
    public int getBoardSize() { return this.boardSize; }
    public int getTurn() { return this.turn; }
    public int getCapturedBlack() { return this.capturedBlack; }
    public int getCapturedWhite() { return this.capturedWhite; }
    public GameStatus getStatus() { return this.status; }
    public int getCreatingPlayerId() { return this.creatingPlayerId; } //

    public void nextTurn() {
        this.turn = (this.turn == 1) ? 2 : 1; 
    }

    public int getBoardStateAt(int x, int y) {
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return -1;
        return this.boardState[y][x];
    }

    public void surrender() {
        this.status = GameStatus.FINISHED;
    }

    private void setBoardStateAt(int x, int y, int value) {
        if (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
            this.boardState[y][x] = value;
        }
    }

    public boolean placePiece(int x, int y, int id) {
        if (getBoardStateAt(x, y) != 0) return false;
        
        setBoardStateAt(x, y, id);
        int opponentId = (id == 1) ? 2 : 1;

       
        if (x > 0 && getBoardStateAt(x-1, y) == opponentId && calculateLiberties(x-1, y) == 0) chokePiece(x-1, y);
        if (y > 0 && getBoardStateAt(x, y-1) == opponentId && calculateLiberties(x, y-1) == 0) chokePiece(x, y-1);
        if (x < boardSize - 1 && getBoardStateAt(x+1, y) == opponentId && calculateLiberties(x+1, y) == 0) chokePiece(x+1, y);
        if (y < boardSize - 1 && getBoardStateAt(x, y+1) == opponentId && calculateLiberties(x, y+1) == 0) chokePiece(x, y+1);

        return true;
    }

    public int calculateLiberties(int x, int y) {
        int id = getBoardStateAt(x, y);
        if (id <= 0) return -1;
        int liberties = 0;
        if (x > 0 && getBoardStateAt(x-1, y) == 0) liberties++;
        if (y > 0 && getBoardStateAt(x, y-1) == 0) liberties++;
        if (x < boardSize - 1 && getBoardStateAt(x+1, y) == 0) liberties++;
        if (y < boardSize - 1 && getBoardStateAt(x, y+1) == 0) liberties++;
        return liberties;
    }

    private void chokePiece(int x, int y) {
        int target = getBoardStateAt(x, y);
        if (target == 1) capturedBlack++;
        else if (target == 2) capturedWhite++;
        setBoardStateAt(x, y, 0);
    }

    public ResponseEntity<?> join() {
        if (this.status == GameStatus.CREATING) {
            this.status = GameStatus.PLAYING;
            return ResponseEntity.ok(new JoinGameResponseDTO(this.creatingPlayerId == 1 ? 2 : 1));
        }
        return ResponseEntity.badRequest().body(new ErrorDTO("Nie można dołączyć."));
    }
}