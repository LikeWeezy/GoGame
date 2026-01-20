package com.example.server.memorydata;

import com.example.server.memorydata.datatypes.Game;
import com.example.server.memorydata.datatypes.GameFactory;
import com.example.server.memorydata.datatypes.dtos.request.CreateNewGameDTO;
import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameDataService {
    private final Map<String, Game> games = new HashMap<>();
    private final GameFactory gameFactory = new GameFactory(0);

    public GetGameDataResponseDTO getStateOfBoard(String gameId) {
        if (!this.games.containsKey(gameId)) return null;
        return GetGameDataResponseDTOFactory.fromGame(this.games.get(gameId));
    }

    public CreateNewGameResponseDTO createNewGame(CreateNewGameDTO cng) {
        Game newGame = this.gameFactory.fromCreateNewGameRequest(cng);
        this.games.put(newGame.getGameId(), newGame);
        return new CreateNewGameResponseDTO(newGame.getGameId(), newGame.getCreatingPlayerId()); 
    }

    public ResponseEntity<?> joinGame(String gameId) {
        if(!this.games.containsKey(gameId)) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Brak gry o tym ID"));
        }
        return this.games.get(gameId).join();
    }

    public ResponseEntity<?> makeMove(String gameId, MakeMoveDTO mm) {
        if (!this.games.containsKey(gameId))
            return ResponseEntity.badRequest().body(new ErrorDTO("Brak gry"));

        Game game = this.games.get(gameId);

        return game.getStatus().handleMakeMove(game, mm);
    }
}