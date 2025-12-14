package com.example.server.memorydata.datatypes.dtos.response;

import com.example.server.memorydata.datatypes.Game;

public class GetGameDataResponseDTOFactory {
    public static GetGameDataResponseDTO fromGame(Game game) {
        int[][] deepClone = new int[game.getBoardSize()][game.getBoardSize()];
        for(int i = 0; i < game.getBoardSize(); ++i) {
            for(int j = 0; j < game.getBoardSize(); ++j) {
                deepClone[i][j] = game.getBoardStateAt(i, j);
            }
        }
        return new GetGameDataResponseDTO(
                game.getGameId(),
                game.getBoardSize(),
                deepClone,
                game.getTurn(),
                game.getCapturedBlack(),
                game.getCapturedWhite(),
                game.getStatus().toString()
        );
    }
}
