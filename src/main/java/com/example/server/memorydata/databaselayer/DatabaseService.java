package com.example.server.memorydata.databaselayer;

import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.response.GetGameHistoryResponseDTO;
import com.example.server.memorydata.datatypes.dtos.response.PlayerMoveSubDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseService {
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;

    public DatabaseService(GameRepository gameRepository, MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
    }

    public void saveGame(String id, int boardSize) {
        DBGame dbGame = new DBGame();
        dbGame.setId(id);
        dbGame.setWinnerId(0);
        dbGame.setBoardSize(boardSize);
        gameRepository.save(dbGame);
    }

    public void saveMove(String gameId, MakeMoveDTO mm) {
        DBMove dbMove = new DBMove();
        DBGame dbGame = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Missing game"));
        dbMove.setGame(dbGame);
        dbMove.setPlayerId(mm.playerId());
        dbMove.setMoveType(mm.moveType());
        dbMove.setX(mm.x());
        dbMove.setY(mm.y());
        moveRepository.save(dbMove);
    }

    @Transactional(readOnly = true)
    public GetGameHistoryResponseDTO getGameHistory(String gameId) {
        DBGame dbGame = gameRepository.findById(gameId).orElseThrow(
                () -> new IllegalArgumentException("Missing game history")
        );
        return new GetGameHistoryResponseDTO(
                dbGame.getBoardSize(),
                dbGame.getWinnerId(),
                dbGame.getMoves().stream().map(PlayerMoveSubDTO::fromDBMove).toList()
        );
    }
}
