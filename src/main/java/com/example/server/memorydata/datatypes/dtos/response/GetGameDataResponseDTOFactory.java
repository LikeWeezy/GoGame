package com.example.server.memorydata.datatypes.dtos.response;

import com.example.server.memorydata.datatypes.Game;

/**
 * Factory for creating DTOs with a game's data.
 */
public class GetGameDataResponseDTOFactory {
    /**
     * Create a response DTO with its fields corresponding to the game's data.
     * @param game Game from which to get data
     * @return Response DTO with the game's data.
     */
    public static GetGameDataResponseDTO fromGame(Game game) {
        int[][] deepClone = game.deepCloneBoardState();
        return new GetGameDataResponseDTO(
                game.getGameId(),
                game.getBoardSize(),
                deepClone,
                game.getTurn(),
                game.getCapturedBlack(),
                game.getCapturedWhite(),
                game.getStatus().toString(),
                game.getWinnerId()
        );
    }
}
