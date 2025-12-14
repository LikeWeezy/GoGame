package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import com.example.server.memorydata.datatypes.dtos.response.JoinGameResponseDTO;
import org.springframework.http.ResponseEntity;

public class Game {
    private final String gameId;

    private final int boardSize;

    private int[][] boardState;

    private int turn;

    private GameStatus status;

    private final int creatingPlayerId;

    public Game(String gameId, int boardSize, int[][] boardState, int turn, GameStatus status, int creatingPlayerId) {
        this.gameId = gameId;
        this.boardSize = boardSize;
        this.boardState = boardState;
        this.turn = turn;
        this.status = status;
        this.creatingPlayerId = creatingPlayerId;
    }

    public String getGameId() {
        return this.gameId;
    }
    public int getBoardSize() {
        return this.boardSize;
    }
    public int getBoardStateAt(int x, int y) {
        return this.boardState[x][y];
    }
    public int getTurn() {
        return this.turn;
    }
    public GameStatus getStatus() {
        return this.status;
    }
    public int getCreatingPlayerId() {
        return this.creatingPlayerId;
    }

    public ResponseEntity<?> join() {
        return switch (this.status) {
            case CREATING -> {
                this.status = GameStatus.PLAYING;
                yield ResponseEntity.ok(new JoinGameResponseDTO(this.getCreatingPlayerId() == 1 ? 2 : 1));
            }
            case PLAYING -> ResponseEntity.badRequest().body(new ErrorDTO("Can't join: Gra już się rozpoczęła"));
            case FINISHED -> ResponseEntity.badRequest().body(new ErrorDTO("Can't join: Gra już się zakończyła"));
            default -> ResponseEntity.internalServerError().body(new ErrorDTO("Unhandled status case"));
        };
    }

    public boolean placePiece(int x, int y, int id) {
        if (this.getBoardStateAt(x, y) != 0)
            return false;

        this.boardState[x][y] = id;
        ++this.turn;
        return true;
    }
}
