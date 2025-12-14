package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import com.example.server.memorydata.datatypes.dtos.response.JoinGameResponseDTO;
import org.springframework.http.ResponseEntity;

public class Game {
    private final String gameId;

    private final int boardSize;

    private int[][] boardState;

    private int turn;

    private int capturedBlack;

    private int capturedWhite;

    private GameStatus status;

    private final int creatingPlayerId;

    public Game(String gameId, int boardSize, int[][] boardState, int turn, int capturedBlack, int capturedWhite, GameStatus status, int creatingPlayerId) {
        this.gameId = gameId;
        this.boardSize = boardSize;
        this.boardState = boardState;
        this.turn = turn;
        this.capturedBlack = capturedBlack;
        this.capturedWhite = capturedWhite;
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
        return this.boardState[y][x];
    }
    private void setBoardStateAt(int x, int y, int value) {
        this.boardState[y][x] = value;
    }
    public int getTurn() {
        return this.turn;
    }
    public int getCapturedBlack() {
        return this.capturedBlack;
    }
    public int getCapturedWhite() {
        return this.capturedWhite;
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

    public int calculateLiberties(int x, int y) {
        int liberties = 0;
        int id = this.getBoardStateAt(x, y);
        if (id == 0) // Empty field, do nothing
            return -1;

        boolean isAtLeftBorder = x == 0;
        boolean isThereEnemyOnLeft = !isAtLeftBorder && !(
                this.getBoardStateAt(x - 1, y) == 0 ||
                this.getBoardStateAt(x - 1, y) == id
        );
        boolean isAtTopBorder = y == 0;
        boolean isThereEnemyOnTop = !isAtTopBorder && !(
                this.getBoardStateAt(x, y - 1) == 0 ||
                this.getBoardStateAt(x, y - 1) == id
        );
        boolean isAtRightBorder = x == this.getBoardSize() - 1;
        boolean isThereEnemyOnRight = !isAtRightBorder && !(
                this.getBoardStateAt(x + 1, y) == 0 ||
                this.getBoardStateAt(x + 1, y) == id
        );
        boolean isAtBottomBorder = y == this.getBoardSize() - 1;
        boolean isThereEnemyOnBottom = !isAtBottomBorder && !(
                this.getBoardStateAt(x, y + 1) == 0 ||
                this.getBoardStateAt(x, y + 1) == id
        );
        if (!isAtLeftBorder && !isThereEnemyOnLeft) ++liberties;
        if (!isAtTopBorder && !isThereEnemyOnTop) ++liberties;
        if (!isAtRightBorder && !isThereEnemyOnRight) ++liberties;
        if (!isAtBottomBorder && !isThereEnemyOnBottom) ++liberties;

        return liberties;
    }

    private void chokePiece(int x, int y) {
        if (this.getBoardStateAt(x, y) == 1) ++this.capturedBlack;
        else ++this.capturedWhite;
        this.setBoardStateAt(x, y, 0);
    }

    public boolean placePiece(int x, int y, int id) {
        if (this.getBoardStateAt(x, y) != 0)
            return false;

        this.setBoardStateAt(x, y, id);

        if (x > 0 && this.calculateLiberties(x - 1, y) == 0)
            chokePiece(x - 1, y);
        if (y > 0 && this.calculateLiberties(x, y - 1) == 0)
            chokePiece(x, y - 1);
        if (x < this.getBoardSize() - 1 && this.calculateLiberties(x + 1, y) == 0)
            chokePiece(x + 1, y);
        if (y > this.getBoardSize() - 1 && this.calculateLiberties(x, y + 1) == 0)
            chokePiece(x, y + 1);

        ++this.turn;
        return true;
    }
}
