package com.example.server;

import com.example.server.memorydata.GameDataService;
import com.example.server.memorydata.databaselayer.DatabaseService;
import com.example.server.memorydata.datatypes.dtos.request.CreateNewGameDTO;
import com.example.server.memorydata.datatypes.dtos.request.GetNegotiationDetailsDTO;
import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.request.NegotiateDTO;
import com.example.server.memorydata.datatypes.dtos.response.CreateNewGameResponseDTO;
import com.example.server.memorydata.datatypes.dtos.response.GetGameDataResponseDTO;
import com.example.server.memorydata.datatypes.dtos.response.GetGameHistoryResponseDTO;
import com.example.server.memorydata.datatypes.dtos.response.GetNegotiationDetailsResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller that redirects incoming requests to appropriate functions
 * in the GameDataService.
 */
@CrossOrigin(origins = "*") // ROBLOX
@RestController
@RequestMapping("/api")
public class RouteController {
    /** Data service for storing and processing games. */
    private final GameDataService gameDataService;
    /** Service for communicating with the database. */
    private final DatabaseService databaseService;

    public RouteController(GameDataService gameDataService, DatabaseService databaseService) {
        this.gameDataService = gameDataService;
        this.databaseService = databaseService;
    }

    @GetMapping("/game/{gameId}/status")
    public GetGameDataResponseDTO getGameDetails(@PathVariable("gameId") String gameId) {
        return this.gameDataService.getStateOfBoard(gameId);
    }

    @PostMapping("/new_game")
    public CreateNewGameResponseDTO createNewGame(@RequestBody CreateNewGameDTO cng) {
        CreateNewGameResponseDTO cngr = this.gameDataService.createNewGame(cng);
        databaseService.saveGame(cngr.gameId(), cng.boardSize());
        return cngr;
    }

    @PostMapping("/game/{gameId}/join")
    public ResponseEntity<?> joinGame(@PathVariable("gameId") String gameId) {
        return this.gameDataService.joinGame(gameId);
    }

    @PostMapping("/game/{gameId}/move")
    public ResponseEntity<?> makeMove(@PathVariable("gameId") String gameId, @RequestBody MakeMoveDTO mm) {
        ResponseEntity<?> responseEntity = this.gameDataService.makeMove(gameId, mm);
        if(responseEntity.getStatusCode().value() == 200) {
            databaseService.saveMove(gameId, mm);
        }
        return responseEntity;
    }

    @PostMapping("/game/{gameId}/negotiate")
    public ResponseEntity<?> negotiate(@PathVariable("gameId") String gameId, @RequestBody NegotiateDTO negotiateDTO) {
        return this.gameDataService.negotiate(gameId, negotiateDTO);
    }

    @PostMapping("/game/{gameId}/resume")
    public ResponseEntity<?> resume(@PathVariable("gameId") String gameId) {
        return this.gameDataService.resume(gameId);
    }

    @PostMapping("/game/{gameId}/get_negotiation_details")
    public GetNegotiationDetailsResponseDTO getNegotiationDetails(@PathVariable("gameId") String gameId,
                                                                  @RequestBody GetNegotiationDetailsDTO gnd) {
        return this.gameDataService.getNegotiationDetails(gameId, gnd);
    }

    @GetMapping("/game/{gameId}/get_game_history")
    public GetGameHistoryResponseDTO getGameHistoryResponse(@PathVariable("gameId") String gameId) {
        return databaseService.getGameHistory(gameId);
    }
}