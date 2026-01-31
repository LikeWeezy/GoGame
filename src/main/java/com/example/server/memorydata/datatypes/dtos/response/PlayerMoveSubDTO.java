package com.example.server.memorydata.datatypes.dtos.response;

import com.example.server.memorydata.databaselayer.DBMove;

public record PlayerMoveSubDTO(int playerId, String moveType, int x, int y) {
    public static PlayerMoveSubDTO fromDBMove(DBMove dbMove) {
        return new PlayerMoveSubDTO(dbMove.getPlayerId(), dbMove.getMoveType(), dbMove.getX(), dbMove.getY());
    }
}
