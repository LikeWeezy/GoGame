package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.GetNegotiationDetailsDTO;
import com.example.server.memorydata.datatypes.dtos.request.NegotiateDTO;
import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import com.example.server.memorydata.datatypes.dtos.response.GetNegotiationDetailsResponseDTO;
import com.example.server.memorydata.datatypes.dtos.response.JoinGameResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * A game between two players, with all the state needed to represent it.
 */
public class Game {
    /** ID of the game. */
    private final String gameId;
    /** Length of the side of the game's square board. */
    private final int boardSize;
    /**
     * A 2D boardSize x boardSize int array representing the board.
     * The value of 0 denotes an empty field, 1 denotes a black stone, and 2 denotes a white stone.
     */
    private int[][] boardState;
    /** Represents the state of the board from before the last move. Needed to check for ko. */
    private int[][] previousBoardState;
    /** Denotes which player's turn it is. 1 for black's turn, 2 for white's turn. */
    private int turn;
    /** Number of black stones captured by white. */
    private int capturedBlack;
    /** Number of white stones captured by black. */
    private int capturedWhite;
    /** Denotes the current stage of the game. */
    private GameStatus status;
    /** ID of the player that sent the CreateNewGame request that resulted in this game being created. */
    private final int creatingPlayerId;
    /**
     * True is the last action taken by a player was passing.
     * False otherwise (i.e. after a stone is placed, or after a paused game is resumed).
     */
    private boolean justPassed;
    /** ID of the player that won the game. 0 if the game is not over yet. */
    private int winnerId;
    /** Current scoring proposition from the white player. */
    private Negotiation whitesNegotiation;
    /** Current scoring proposition from the black player. */
    private Negotiation blacksNegotiation;

    /**
     * Directly takes given values and assigns them to the new game's fields.
     * Doesn't validate - expects the caller to give valid values.
     * @param gameId ID of the game
     * @param boardSize Length of the side of the game's square board
     * @param boardState A 2D boardSize x boardSize int array representing the board.
     * The value of 0 denotes an empty field, 1 denotes a black stone, and 2 denotes a white stone.
     * @param previousBoardState Represents the state of the board from before the last move. Needed to check for ko.
     * @param turn Denotes which player's turn it is. 1 for black's turn, 2 for white's turn
     * @param capturedBlack Number of black stones captured by white
     * @param capturedWhite Number of white stones captured by black
     * @param status Denotes the current stage of the game
     * @param creatingPlayerId ID of the player that sent the CreateNewGame request
     * that resulted in this game being created
     * @param justPassed True is the last action taken by a player was passing.
     * False otherwise (i.e. after a stone is placed, or after a paused game is resumed).
     * @param winnerId ID of the player that won the game. 0 if the game is not over yet.
     * @param whitesNegotiation Current scoring proposition from the white player
     * @param blacksNegotiation Current scoring proposition from the black player
     */
    public Game(String gameId, int boardSize, int[][] boardState, int[][] previousBoardState, int turn,
                int capturedBlack, int capturedWhite, GameStatus status, int creatingPlayerId, boolean justPassed,
                int winnerId, Negotiation whitesNegotiation, Negotiation blacksNegotiation) {
        this.gameId = gameId;
        this.boardSize = boardSize;
        this.boardState = boardState;
        this.previousBoardState = previousBoardState;
        this.turn = turn;
        this.capturedBlack = capturedBlack;
        this.capturedWhite = capturedWhite;
        this.status = status;
        this.creatingPlayerId = creatingPlayerId;
        this.justPassed = justPassed;
        this.winnerId = winnerId;
        this.whitesNegotiation = whitesNegotiation;
        this.blacksNegotiation = blacksNegotiation;
    }

    /**
     * Getter of gameId.
     * @return gameId
     */
    public String getGameId() { return this.gameId; }

    /**
     * Getter of boardSize.
     * @return boardSize
     */
    public int getBoardSize() { return this.boardSize; }
    /**
     * Getter of turn.
     * @return turn
     */
    public int getTurn() { return this.turn; }
    /**
     * Getter of capturedBlack.
     * @return capturedBlack
     */
    public int getCapturedBlack() { return this.capturedBlack; }
    /**
     * Getter of capturedWhite.
     * @return capturedWhite
     */
    public int getCapturedWhite() { return this.capturedWhite; }
    /**
     * Getter of status.
     * @return status
     */
    public GameStatus getStatus() { return this.status; }
    /**
     * Getter of creatingPlayerId.
     * @return creatingPlayerId
     */
    public int getCreatingPlayerId() { return this.creatingPlayerId; }
    /**
     * Getter of winnerId.
     * @return winnerId
     */
    public int getWinnerId() { return this.winnerId; }

    /** Changes the turn field for the next player. */
    private void nextTurn() {
        this.turn = (this.turn == 1) ? 2 : 1; 
    }

    /** The player whose turn it is surrenders. The other player wins. */
    public void surrender() {
        this.status = GameStatus.FINISHED;
        this.winnerId = (this.turn == 1) ? 2 : 1;
    }

    /**
     * The player whose turn it is passes. If two passes happen back-to-back,
     * the game is paused and the players negotiate scoring.
     */
    public void pass() {
        if(this.justPassed) {
            this.status = GameStatus.PAUSED;
            this.justPassed = false;
        }
        else {
            this.justPassed = true;
        }
        this.nextTurn();
    }

    /**
     * Propose scoring.
     * @param negotiateDTO Request DTO containing the proposed values and the submitting player's ID
     * @return Response DTO that is empty or contains error information.
     */
    public ResponseEntity<?> submitNegotiation(NegotiateDTO negotiateDTO) {
        if(!(negotiateDTO.playerId() == 1 || negotiateDTO.playerId() == 2)) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Złe ID gracza w próbie dogadania"));
        }
        Negotiation negotiation = Negotiation.fromNegotiateDTO(negotiateDTO);
        if(negotiateDTO.playerId() == 1) {
            this.blacksNegotiation = negotiation;
        }
        else {
            this.whitesNegotiation = negotiation;
        }
        if(this.whitesNegotiation != null &&
                this.blacksNegotiation != null &&
                Negotiation.areNegotiationsEqual(this.whitesNegotiation, this.blacksNegotiation)) {
            this.chooseWinner();
        }
        return ResponseEntity.ok().build();
    }

    /** Resume a paused game. Remove current scoring propositions. */
    public void resume() {
        this.whitesNegotiation = null;
        this.blacksNegotiation = null;
        this.status = GameStatus.PLAYING;
    }

    /** End the game and set winnerId to the player with more points. */
    public void chooseWinner() {
        Negotiation negotiation = this.whitesNegotiation;
        int whitesPoints = negotiation.whiteTerritory() - negotiation.deadWhite() -  this.capturedWhite;
        int blacksPoints = negotiation.blackTerritory() - negotiation.deadBlack() - this.capturedBlack;
        this.winnerId = (blacksPoints > whitesPoints) ? 1 : 2;
        this.status = GameStatus.FINISHED;
    }

    /**
     * Try to place a stone on the board.
     * @param x The column on which to place the stone
     * @param y The row in which to place the stone
     * @param id ID of the placing player
     * @return Whether it was legal to make the move, and therefore whether it was made.
     */
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

    /**
     * Add the number of stones that were captured to capturedBlack or capturedWhite, based on ID in csc.
     * @param csc The number and ID of captured stones
     */
    private void addPrisonerCounts(ChokedStonesCount csc) {
        if(csc.idOfChokedStones() == 1) {
            this.capturedBlack += csc.count();
        }
        else if(csc.idOfChokedStones() == 2) {
            this.capturedWhite += csc.count();
        }
    }

    /**
     * After a stone has been placed, check whether there are any adjacent enemy stones
     * that can be captured. Capture them if there are.
     * @param boardState 2D array representing the board
     * @param checkTrackerBoard 2D array for keeping track which fields were already checked
     * @param x The column on which the stone was placed
     * @param y The row in which the stone was placed
     * @param thisPlayersId ID of placing player
     * @return Number and ID of any stones that were captured.
     */
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

    /**
     * Calculate the number of empty fields adjacent to the given position.
     * @param boardState 2D array representing the board
     * @param x The column of the checked position
     * @param y The row of the checked position
     * @return The number of empty fields adjacent to the given position.
     */
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

    /**
     * Join a game.
     * @return Response DTO with the ID of the joining player, or with an error message.
     */
    public ResponseEntity<?> join() {
        if (this.status == GameStatus.CREATING) {
            this.status = GameStatus.PLAYING;
            return ResponseEntity.ok(new JoinGameResponseDTO(this.creatingPlayerId == 1 ? 2 : 1));
        }
        return ResponseEntity.badRequest().body(new ErrorDTO("Nie można dołączyć."));
    }

    /**
     * Create a 2D array for keeping track which positions on the board have
     * been checked while evaluating the legality of a stone placement.
     * @param height The height of the board to be created
     * @param width The width of the board to be created
     * @return A 2D height x width boolean array with all falses.
     */
    private static boolean[][] createCheckTrackerBoard(int height, int width) {
        boolean[][] otpt = new boolean[height][width];
        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                otpt[i][j] = false;
            }
        }
        return otpt;
    }

    /**
     * Find the fields on the board that have the same value, and which are
     * connected, with one field being adjacent to another (like the filling bucket in the Paint program).
     * @param boardState 2D array representing the board
     * @param x The column of the position from which to start
     * @param y The row of the position from which to start
     * @param val The value to look for
     * @return A Set with board positions that are adjacent to each other and have the same value.
     */
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

    /**
     * Mark neighboring fields with the value of zero as checked in the checkTrackerBoard.
     * @param boardState 2D array representing the board
     * @param checkTrackerBoard 2D array for keeping track which fields were already checked
     * @param x The column of the position from which to start
     * @param y The row of the position from which to start
     */
    private static void checkEmptyFields(int[][] boardState, boolean[][] checkTrackerBoard, int x, int y) {
        Set<BoardPosition> emptyNeighbourhood = findNeighbourhoodWithGivenValue(boardState, x, y, 0);
        for(BoardPosition bp : emptyNeighbourhood) {
            checkTrackerBoard[bp.x()][bp.y()] = true;
        }
    }

    /**
     * Capture the stones in the neighborhood of (x, y) if they have no liberties.
     * @param boardState 2D array representing the board
     * @param checkTrackerBoard 2D array for keeping track which fields were already checked
     * @param x The column of the position whose neighborhood to evaluate
     * @param y The column of the position whose neighborhood to evaluate
     * @return Number and ID of stones that were captured, if any.
     */
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

    /**
     * See what should be done with a field on the board. Capture stones if they're surrounded.
     * Mark fields as checked in the checkTrackerBoard if they're empty.
     * @param boardState 2D array representing the board
     * @param checkTrackerBoard 2D array for keeping track which fields were already checked
     * @param x The column of the field to check
     * @param y The row of the field to check
     * @return Number and ID of stones that were captured, if any.
     */
    private static ChokedStonesCount handleBoardPlace(int[][] boardState, boolean[][] checkTrackerBoard, int x, int y) {
        if(boardState[x][y] == 0) {
            checkEmptyFields(boardState, checkTrackerBoard, x, y);
            return new ChokedStonesCount(0, 0);
        }
        else {
            return chokeStonesIfSurrounded(boardState, checkTrackerBoard, x, y);
        }
    }

    /**
     * Check whether a neighborhood of stones has any liberties.
     * @param boardState 2D array representing the board
     * @param group The neighborhood to check
     * @return True if the neighborhood has at least 1 liberty. False otherwise.
     */
    private static boolean doesGroupHaveLiberties(int[][] boardState, Set<BoardPosition> group) {
        for(BoardPosition bp : group) {
            if(calculateLiberties(boardState, bp.x(), bp.y()) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether two boards have the same values on all corresponding positions.
     * @param first The first board
     * @param second The second board
     * @return True if the two boards have the same values on all corresponding positions. False otherwise.
     */
    private static boolean areTwoStateBoardsEqual(int[][] first, int[][] second) {
        if(first.length != second.length || first[0].length != second[0].length) return false;
        for(int i = 0; i < first.length; ++i) {
            for(int j = 0; j < first[0].length; ++j) {
                if(first[i][j] != second[i][j]) return false;
            }
        }
        return true;
    }

    /**
     * Make an exact copy of the current board.
     * @return A copy of the current board.
     */
    public int[][] deepCloneBoardState() {
        int[][] otpt = new int[this.boardState.length][this.boardState[0].length];
        for(int i = 0; i < this.boardState.length; ++i) {
            System.arraycopy(this.boardState[i], 0, otpt[i], 0, this.boardState[0].length);
        }
        return otpt;
    }

    /**
     * Get the proposed scoring the one's opponent.
     * @param gnd Request DTO containing the asking player's ID
     * @return The opponent's proposed scoring.
     */
    public GetNegotiationDetailsResponseDTO getNegotiationDetails(GetNegotiationDetailsDTO gnd) {
        int opponentId = gnd.playerId() == 1 ? 2 : 1;
        if(opponentId == 1) {
            return GetNegotiationDetailsResponseDTO.from(this.blacksNegotiation);
        }
        else {
            return GetNegotiationDetailsResponseDTO.from(this.whitesNegotiation);
        }
    }
}