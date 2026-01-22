package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.CreateNewGameDTO;

/**
 * Class for creating new Game instances. Automatically assigns successively higher game IDs.
 */
public class GameFactory {
    /** Variable to keep track of the next game ID to assign. */
    private int idAutoincrement;

    /**
     * Construct a new factory that produces games with IDs starting at a specified integer.
     * @param startingGameId Number from which to start giving IDs to new games
     */
    public GameFactory(int startingGameId) {
        this.idAutoincrement = startingGameId;
    }

    /**
     * Create a new Game instance from a CreateNewGame request.
     * @param cng Request DTO containing data for a new game
     * @return Newly created Game instance.
     */
    public Game fromCreateNewGameRequest(CreateNewGameDTO cng) {
        int boardSize = cng.boardSize();
        int[][] emptyBoard = createEmptyBoard(boardSize);
        int[][] prevEmptyBoard = createEmptyBoard(boardSize);
        Game newGame = new Game(
                idAutoincrement + "",
                cng.boardSize(),
                emptyBoard,
                prevEmptyBoard,
                1,
                0,
                0,
                GameStatus.CREATING,
                cng.requestedColor().equals("BLACK") ? 1 : 2,
                false,
                0,
                null,
                null
        );
        ++idAutoincrement;
        return newGame;
    }

    /**
     * Create an empty 2D int array for holding board state.
     * @param boardSize Size of the board to create - board will be boardSize x boardSize
     * @return A boardSize x boardSize 2D int array with all zeroes.
     */
    private static int[][] createEmptyBoard(int boardSize) {
        int[][] emptyBoard = new int[boardSize][boardSize];
        for(int i = 0; i < boardSize; ++i) {
            for(int j = 0; j < boardSize; ++j) {
                emptyBoard[i][j] = 0;
            }
        }
        return emptyBoard;
    }
}
