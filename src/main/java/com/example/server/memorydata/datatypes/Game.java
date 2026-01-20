package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import com.example.server.memorydata.datatypes.dtos.response.JoinGameResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.*;

public class Game {
    private final String gameId;
    private final int boardSize;
    private int[][] boardState;
    private int[][] previousBoardState;
    private int turn;
    private int capturedBlack;
    private int capturedWhite;
    private GameStatus status;
    private final int creatingPlayerId;
    private boolean justPassed;
    private int winnerId = 0;

    public Game(String gameId, int boardSize, int[][] boardState, int[][] previousBoardState, int turn, int capturedBlack, int capturedWhite, GameStatus status, int creatingPlayerId, boolean justPassed) {
        this.gameId = gameId;
        this.boardSize = boardSize;
        this.boardState = boardState;
        this.previousBoardState = previousBoardState;
        this.turn = (turn == 0) ? 1 : turn;
        this.capturedBlack = capturedBlack;
        this.capturedWhite = capturedWhite;
        this.status = status;
        this.creatingPlayerId = creatingPlayerId;
        this.justPassed = justPassed;
    }

    public String getGameId() { return this.gameId; }
    public int getBoardSize() { return this.boardSize; }
    public int getTurn() { return this.turn; }
    public int getCapturedBlack() { return this.capturedBlack; }
    public int getCapturedWhite() { return this.capturedWhite; }
    public GameStatus getStatus() { return this.status; }
    public int getCreatingPlayerId() { return this.creatingPlayerId; }
    public int getWinnerId() { return this.winnerId; }

    private void nextTurn() {
        this.turn = (this.turn == 1) ? 2 : 1; 
    }

    public void surrender() {
        this.status = GameStatus.FINISHED;
        this.winnerId = (this.turn == 1) ? 2 : 1;
    }

    public void pass() {
        if(this.justPassed) {
            this.status = GameStatus.FINISHED;
            this.winnerId = (this.capturedWhite > this.capturedBlack) ? 1 : 2;
        }
        else {
            this.justPassed = true;
            this.nextTurn();
        }
    }

    public boolean placePiece(int x, int y, int id) {
        if(this.boardState[x][y] != 0) return false;

        int[][] helperBoardCopy = this.deepCloneBoardState();
        helperBoardCopy[x][y] = id;
        boolean[][] checkTrackerBoard = createCheckTrackerBoard(this.getBoardSize(), this.getBoardSize());

        ChokedStonesCount adjacentCaptured = tryCaptureAdjacentEnemyStones(helperBoardCopy, checkTrackerBoard, x, y, id);

        int ownChokedStones = handleBoardPlace(helperBoardCopy, checkTrackerBoard, x, y).count();
        if(ownChokedStones > 0) return false;

        if(areTwoStateBoardsEqual(helperBoardCopy, this.previousBoardState)) return false;

        this.addPrisonerCounts(adjacentCaptured);

        for(int i = 0; i < this.getBoardSize(); ++i) {
            for(int j = 0; j < this.getBoardSize(); ++j) {
                if(!checkTrackerBoard[i][j]) {
                    ChokedStonesCount csc = handleBoardPlace(helperBoardCopy, checkTrackerBoard, j, i);
                    this.addPrisonerCounts(csc);
                }
            }
        }

        this.nextTurn();
        this.justPassed = false;
        this.previousBoardState = this.boardState;
        this.boardState = helperBoardCopy;
        return true;
    }

    private void addPrisonerCounts(ChokedStonesCount csc) {
        if(csc.idOfChokedStones() == 1) {
            this.capturedBlack += csc.count();
        }
        else if(csc.idOfChokedStones() == 2) {
            this.capturedWhite += csc.count();
        }
    }

    private static ChokedStonesCount tryCaptureAdjacentEnemyStones(int[][] boardState, boolean[][] checkTrackerBoard, int x, int y, int thisPlayersId) {
        int opponentId = thisPlayersId == 1 ? 2 : 1;
        int takenEnemyStones = 0;
        BoardPosition bp = new BoardPosition(x-1, y);
        if(bp.x() >= 0 && !checkTrackerBoard[bp.x()][bp.y()] && boardState[bp.x()][bp.y()] == opponentId) {
            takenEnemyStones += handleBoardPlace(boardState, checkTrackerBoard, bp.x(), bp.y()).count();
        }
        bp = new BoardPosition(x+1, y);
        if(bp.x() < boardState.length && !checkTrackerBoard[bp.x()][bp.y()] && boardState[bp.x()][bp.y()] == opponentId) {
            takenEnemyStones += handleBoardPlace(boardState, checkTrackerBoard, bp.x(), bp.y()).count();
        }
        bp = new BoardPosition(x, y-1);
        if(bp.y() >= 0 && !checkTrackerBoard[bp.x()][bp.y()] && boardState[bp.x()][bp.y()] == opponentId) {
            takenEnemyStones += handleBoardPlace(boardState, checkTrackerBoard, bp.x(), bp.y()).count();
        }
        bp = new BoardPosition(x, y+1);
        if(bp.y() < boardState.length && !checkTrackerBoard[bp.x()][bp.y()] && boardState[bp.x()][bp.y()] == opponentId) {
            takenEnemyStones += handleBoardPlace(boardState, checkTrackerBoard, bp.x(), bp.y()).count();
        }
        return new ChokedStonesCount(opponentId, takenEnemyStones);
    }

    public static int calculateLiberties(int[][] boardState, int x, int y) {
        int id = boardState[x][y];
        if (id <= 0) return -1;
        int liberties = 0;
        if (x > 0 && boardState[x-1][y] == 0) liberties++;
        if (y > 0 && boardState[x][y-1] == 0) liberties++;
        if (x < boardState[0].length - 1 && boardState[x+1][y] == 0) liberties++;
        if (y < boardState.length - 1 && boardState[x][y+1] == 0) liberties++;
        return liberties;
    }

    public ResponseEntity<?> join() {
        if (this.status == GameStatus.CREATING) {
            this.status = GameStatus.PLAYING;
            return ResponseEntity.ok(new JoinGameResponseDTO(this.creatingPlayerId == 1 ? 2 : 1));
        }
        return ResponseEntity.badRequest().body(new ErrorDTO("Nie można dołączyć."));
    }

    private static boolean[][] createCheckTrackerBoard(int height, int width) {
        boolean[][] otpt = new boolean[height][width];
        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                otpt[i][j] = false;
            }
        }
        return otpt;
    }

    private static Set<BoardPosition> findNeighbourhoodWithGivenValue(int[][] boardState, int x, int y, int val) {
        Set<BoardPosition> found = new HashSet<>();
        Stack<BoardPosition> toCheck = new Stack<>();

        toCheck.push(new BoardPosition(x, y));
        found.add(new BoardPosition(x, y));

        while(!toCheck.isEmpty()) {
            BoardPosition cur = toCheck.pop();
            BoardPosition bp;
            bp = new BoardPosition(cur.x() - 1, cur.y());
            if(cur.x() > 0 && !found.contains(bp) && boardState[bp.x()][bp.y()] == val) {
                found.add(bp);
                toCheck.push(bp);
            }
            bp = new BoardPosition(cur.x() + 1, cur.y());
            if(cur.x() < boardState.length-1 && !found.contains(bp) && boardState[bp.x()][bp.y()] == val) {
                found.add(bp);
                toCheck.push(bp);
            }
            bp = new BoardPosition(cur.x(), cur.y() - 1);
            if(cur.y() > 0 && !found.contains(bp) && boardState[bp.x()][bp.y()] == val) {
                found.add(bp);
                toCheck.push(bp);
            }
            bp = new BoardPosition(cur.x(), cur.y() + 1);
            if(cur.y() < boardState[0].length-1 && !found.contains(bp) && boardState[bp.x()][bp.y()] == val) {
                found.add(bp);
                toCheck.push(bp);
            }
        }
        return found;
    }

    private static void checkEmptyFields(int[][] boardState, boolean[][] checkTrackerBoard, int x, int y) {
        Set<BoardPosition> emptyNeighbourhood = findNeighbourhoodWithGivenValue(boardState, x, y, 0);
        for(BoardPosition bp : emptyNeighbourhood) {
            checkTrackerBoard[bp.x()][bp.y()] = true;
        }
    }

    private static ChokedStonesCount chokeStonesIfSurrounded(int[][] boardState, boolean[][] checkTrackerBoard, int x, int y) {
        int thisPlayersId = boardState[x][y];
        Set<BoardPosition> neighbourhood = findNeighbourhoodWithGivenValue(boardState, x, y, thisPlayersId);
        if(!doesGroupHaveLiberties(boardState, neighbourhood)) {
            int chokedStones = 0;
            for(BoardPosition bp : neighbourhood) {
                boardState[bp.x()][bp.y()] = 0;
                checkTrackerBoard[bp.x()][bp.y()] = true;
                ++chokedStones;
            }
            return new ChokedStonesCount(thisPlayersId, chokedStones);
        }
        else {
            for(BoardPosition bp : neighbourhood) {
                checkTrackerBoard[bp.x()][bp.y()] = true;
            }
            return new ChokedStonesCount(0, 0);
        }
    }

    private static ChokedStonesCount handleBoardPlace(int[][] boardState, boolean[][] checkTrackerBoard, int x, int y) {
        if(boardState[x][y] == 0) {
            checkEmptyFields(boardState, checkTrackerBoard, x, y);
            return new ChokedStonesCount(0, 0);
        }
        else {
            return chokeStonesIfSurrounded(boardState, checkTrackerBoard, x, y);
        }
    }

    private static boolean doesGroupHaveLiberties(int[][] boardState, Set<BoardPosition> group) {
        for(BoardPosition bp : group) {
            if(calculateLiberties(boardState, bp.x(), bp.y()) > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean areTwoStateBoardsEqual(int[][] first, int[][] second) {
        if(first.length != second.length || first[0].length != second[0].length) return false;
        for(int i = 0; i < first.length; ++i) {
            for(int j = 0; j < first[0].length; ++j) {
                if(first[i][j] != second[i][j]) return false;
            }
        }
        return true;
    }

    public int[][] deepCloneBoardState() {
        int[][] otpt = new int[this.boardState.length][this.boardState[0].length];
        for(int i = 0; i < this.boardState.length; ++i) {
            System.arraycopy(this.boardState[i], 0, otpt[i], 0, this.boardState[0].length);
        }
        return otpt;
    }
}