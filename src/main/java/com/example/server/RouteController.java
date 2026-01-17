package com.example.server;

import com.example.server.memorydata.GameDataService;
import com.example.server.memorydata.datatypes.dtos.request.CreateNewGameDTO;
import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.response.CreateNewGameResponseDTO;
import com.example.server.memorydata.datatypes.dtos.response.GetGameDataResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // ROBLOX
@RestController
@RequestMapping("/api")
public class RouteController {
    private final GameDataService gameDataService;

    public RouteController(GameDataService gameDataService) {
        this.gameDataService = gameDataService;
    }

    @GetMapping("/game/{gameId}/status")
    public GetGameDataResponseDTO getGameDetails(@PathVariable("gameId") String gameId) {
        return this.gameDataService.getStateOfBoard(gameId);
    }

    @PostMapping("/new_game")
    public CreateNewGameResponseDTO createNewGame(@RequestBody CreateNewGameDTO cng) {
        return this.gameDataService.createNewGame(cng);
    }

    @PostMapping("/game/{gameId}/join")
    public ResponseEntity<?> joinGame(@PathVariable("gameId") String gameId) {
        return this.gameDataService.joinGame(gameId);
    }

    @PostMapping("/game/{gameId}/move")
    public ResponseEntity<?> makeMove(@PathVariable("gameId") String gameId, @RequestBody MakeMoveDTO mm) {
        return this.gameDataService.makeMove(gameId, mm);
    }
}