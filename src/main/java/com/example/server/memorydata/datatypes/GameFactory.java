package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.CreateNewGameDTO;

public class GameFactory {
    private int idAutoincrement;

    public GameFactory() {
        this.idAutoincrement = 0;
    }

    public Game fromCreateNewGameRequest(CreateNewGameDTO cng) {
        int boardSize = cng.boardSize();
        int[][] emptyBoard = new int[boardSize][boardSize];
        for(int i = 0; i < boardSize; ++i) {
            for(int j = 0; j < boardSize; ++j) {
                emptyBoard[i][j] = 0;
            }
        }
        Game newGame = new Game(
                idAutoincrement + "",
                cng.boardSize(),
                emptyBoard,
                1,
                GameStatus.CREATING,
                cng.requestedColor().equals("BLACK") ? 1 : 2
        );
        ++idAutoincrement;
        return newGame;
    }
}
