package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.CreateNewGameDTO;

public class GameFactory {
    private int idAutoincrement;

    public GameFactory(int startingGameId) {
        this.idAutoincrement = startingGameId;
    }

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
