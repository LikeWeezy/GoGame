package com.example.server.memorydata;

import com.example.server.memorydata.datatypes.Game;
import com.example.server.memorydata.datatypes.GameFactory;
import com.example.server.memorydata.datatypes.dtos.request.CreateNewGameDTO;
import com.example.server.memorydata.datatypes.dtos.request.GetNegotiationDetailsDTO;
import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.request.NegotiateDTO;
import com.example.server.memorydata.datatypes.dtos.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The service responsible for storing every game's data, creating new games,
 * and forwarding user actions to those games.
 */
@Service
public class GameDataService {
    /** Map for storing games. Indexed by games' IDs */
    private final Map<String, Game> games = new HashMap<>();
    /**
     * Factory for producing new games with IDs that start with the given value
     * and automatically increment for each new one.
     */
    private final GameFactory gameFactory = new GameFactory(0);

    /**
     * Get data about the specified game.
     * @param gameId ID of the game
     * @return Response DTO containing the game's data.
     */
    public GetGameDataResponseDTO getStateOfBoard(String gameId) {
        if (!this.games.containsKey(gameId)) return null;
        return GetGameDataResponseDTOFactory.fromGame(this.games.get(gameId));
    }

    /**
     * Create a new game.
     * @param cng Request DTO containing the new game's parameters
     * @return Response DTO with the new game's ID and the ID of the requesting player.
     */
    public CreateNewGameResponseDTO createNewGame(CreateNewGameDTO cng) {
        Game newGame = this.gameFactory.fromCreateNewGameRequest(cng);
        this.games.put(newGame.getGameId(), newGame);
        return new CreateNewGameResponseDTO(newGame.getGameId(), newGame.getCreatingPlayerId()); 
    }

    /**
     * Join an existing game. The game must have the CREATING status.
     * @param gameId ID of the game
     * @return Response DTO that is empty or contains error information.
     */
    public ResponseEntity<?> joinGame(String gameId) {
        if(!this.games.containsKey(gameId)) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Brak gry o tym ID"));
        }
        return this.games.get(gameId).join();
    }

    /**
     * Make a move in a game.
     * @param gameId ID of the game
     * @param mm Request DTO containing data about the move
     * @return Response DTO that is empty or contains error information.
     */
    public ResponseEntity<?> makeMove(String gameId, MakeMoveDTO mm) {
        Game game = this.games.getOrDefault(gameId, null);
        if(game == null) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Brak gry o tym ID"));
        }
        return game.getStatus().handleMakeMove(game, mm);
    }

    /**
     * Propose the scoring of a game. The game must be in the PAUSED state.
     * When propositions of both players are equal, the game ends.
     * @param gameId ID of the game
     * @param negotiateDTO Request DTO containing the proposed values and the submitting player's ID
     * @return Response DTO that is empty or contains error information.
     */
    public ResponseEntity<?> negotiate(String gameId, NegotiateDTO negotiateDTO) {
        Game game = this.games.getOrDefault(gameId, null);
        if(game == null) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Brak gry o tym ID"));
        }
        return game.getStatus().handleNegotiation(game, negotiateDTO);
    }

    /**
     * Resume a paused game. The game must be in the PAUSED state. This ends current negotiations.
     * @param gameId ID of the game
     * @return Response DTO that is empty or contains error information.
     */
    public ResponseEntity<?> resume(String gameId) {
        Game game = this.games.getOrDefault(gameId, null);
        if(game == null) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Brak gry o tym ID"));
        }
        return game.getStatus().handleResuming(game);
    }

    /**
     * Get the scoring proposition of one's opponent.
     * @param gameId ID of the game
     * @param gnd Request DTO containing the ID of the asking player
     * @return Response DTO with the proposition of the asking player's opponent.
     * The fields are all equal to -1 if the opponent hasn't made a proposition since
     * the beginning of the current negotiation session.
     */
    public GetNegotiationDetailsResponseDTO getNegotiationDetails(String gameId, GetNegotiationDetailsDTO gnd) {
        Game game = this.games.getOrDefault(gameId, null);
        if(game == null) return GetNegotiationDetailsResponseDTO.empty();
        else return game.getNegotiationDetails(gnd);
    }
}