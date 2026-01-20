package com.example.server.memorydata.datatypes.dtos.response;

import com.example.server.memorydata.datatypes.Game;

public class GetGameDataResponseDTOFactory {
    public static GetGameDataResponseDTO fromGame(Game game) {
        int[][] deepClone = game.deepCloneBoardState();
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
