package com.example.server.memorydata;

import com.example.server.memorydata.datatypes.Game;
import com.example.server.memorydata.datatypes.GameFactory;
import com.example.server.memorydata.datatypes.GameStatus;
import com.example.server.memorydata.datatypes.MoveType;
import com.example.server.memorydata.datatypes.dtos.request.CreateNewGameDTO;
import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameDataService {
    private final Map<String, Game> games = new HashMap<String, Game>();
    private final GameFactory gameFactory = new GameFactory();

    public GetGameDataResponseDTO getStateOfBoard(String gameId) {
        return GetGameDataResponseDTOFactory.fromGame(this.games.get(gameId));
    }

    public CreateNewGameResponseDTO createNewGame(CreateNewGameDTO cng) {
        Game newGame = this.gameFactory.fromCreateNewGameRequest(cng);
        this.games.put(newGame.getGameId(), newGame);
        int creatingPlayerId = newGame.getCreatingPlayerId();
        return new CreateNewGameResponseDTO(newGame.getGameId(), creatingPlayerId);
    }

    public ResponseEntity<?> joinGame(String gameId) {
        if(!this.games.containsKey(gameId)) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Can't join: Nie ma gry o takim ID"));
        }
        return this.games.get(gameId).join();
    }

    public ResponseEntity<?> makeMove(String gameId, MakeMoveDTO mm) {
        if (!this.games.containsKey(gameId))
            return ResponseEntity.badRequest().body(new ErrorDTO("Can't move: Nie ma gry o takim ID"));

        Game game = this.games.get(gameId);
        if (game.getStatus() == GameStatus.CREATING)
            return ResponseEntity.badRequest().body(new ErrorDTO("Can't move: Gra się jeszcze nie zaczęła"));
        else if (game.getStatus() == GameStatus.FINISHED)
            return ResponseEntity.badRequest().body(new ErrorDTO("Can't move: Gra się już skończyła"));

        if (!((mm.playerId() == 1 && game.getTurn() % 2 == 1) || (mm.playerId() == 2 && game.getTurn() % 2 == 0)))
            return ResponseEntity.badRequest().body(new ErrorDTO("Can't move: Teraz jest ruch nie tego gracza"));

        MoveType moveType = switch (mm.moveType()) {
            case "PLACE" -> MoveType.PLACE;
            case "PASS" -> MoveType.PASS;
            case "SURRENDER" -> MoveType.SURRENDER;
            default -> MoveType.UNKNOWN;
        };
        if (moveType == MoveType.UNKNOWN)
            return ResponseEntity.badRequest().body(new ErrorDTO("Can't move: Podano zły rodzaj ruchu"));

        switch (moveType) {
            case PLACE -> {
                int x = mm.x();
                int y = mm.y();
                boolean successPlacing = game.placePiece(x, y, mm.playerId());
                if (!successPlacing)
                    return ResponseEntity.badRequest().body(new ErrorDTO("Can't move: Pole jest zajęte"));

                return ResponseEntity.ok().build();
            }
            default -> {
                // TODO: Implement other moves
            }
        }
        return ResponseEntity.badRequest().body(new ErrorDTO("Got a move that couldn't be handled"));
    }
}
