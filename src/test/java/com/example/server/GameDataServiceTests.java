package com.example.server;

import com.example.server.memorydata.GameDataService;
import com.example.server.memorydata.datatypes.MovesToSave;
import com.example.server.memorydata.datatypes.dtos.request.*;
import com.example.server.memorydata.datatypes.dtos.response.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static com.example.server.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/** Tests for the GameDataService class. */
public class GameDataServiceTests {
    /** Test whether GameDataService properly creates new games. */
    @Test
    public void testCreatingNewGames() {
        GameDataService gds = new GameDataService();
        CreateNewGameDTO cng1 = new CreateNewGameDTO(19, "BLACK");
        CreateNewGameResponseDTO createNewGameResponse1 = gds.createNewGame(cng1, 0);
        CreateNewGameResponseDTO goodCreateNewGameResponse1 = new CreateNewGameResponseDTO("0", 1);
        GetGameDataResponseDTO getGameDataResponse1 = gds.getStateOfBoard("0");
        GetGameDataResponseDTO goodGetGameDataResponse1 = new GetGameDataResponseDTO("0", 19, createEmptyBoard(19), 1, 0, 0, "CREATING", 0);

        assertEquals(goodCreateNewGameResponse1, createNewGameResponse1);
        assertTwoGetGameDataResponsesEqual(goodGetGameDataResponse1, getGameDataResponse1);

        CreateNewGameDTO cng2 = new CreateNewGameDTO(9, "WHITE");
        CreateNewGameResponseDTO createNewGameResponse2 = gds.createNewGame(cng2, 1);
        CreateNewGameResponseDTO goodCreateNewGameResponse2 = new CreateNewGameResponseDTO("1", 2);
        GetGameDataResponseDTO getGameDataResponse2 = gds.getStateOfBoard("1");
        GetGameDataResponseDTO goodGetGameDataResponse2 = new GetGameDataResponseDTO("1", 9, createEmptyBoard(9), 1, 0, 0, "CREATING", 0);

        assertEquals(goodCreateNewGameResponse2, createNewGameResponse2);
        assertTwoGetGameDataResponsesEqual(goodGetGameDataResponse2, getGameDataResponse2);
    }

    /** Test whether joining games works or fails properly. */
    @Test
    public void testJoiningGames() {
        GameDataService gds = new GameDataService();

        // BLACK creates a game, WHITE tries to join. Should succeed.
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        ResponseEntity<?> joinGameResponse1 = gds.joinGame("0");
        assertInstanceOf(JoinGameResponseDTO.class, joinGameResponse1.getBody());

        JoinGameResponseDTO castedJoinGameResponse1 = (JoinGameResponseDTO)joinGameResponse1.getBody();
        int actualPlayerId1 = castedJoinGameResponse1.playerId();
        assertEquals(2, actualPlayerId1);
        assertEquals("PLAYING", gds.getStateOfBoard("0").status());

        // WHITE creates a game, BLACK tries to join. Should succeed.
        gds.createNewGame(new CreateNewGameDTO(13, "WHITE"), 1);
        ResponseEntity<?> joinGameResponse2 = gds.joinGame("1");
        assertInstanceOf(JoinGameResponseDTO.class, joinGameResponse2.getBody());

        JoinGameResponseDTO castedJoinGameResponse2 = (JoinGameResponseDTO)joinGameResponse2.getBody();
        int actualPlayerId2 = castedJoinGameResponse2.playerId();
        assertEquals(1, actualPlayerId2);
        assertEquals("PLAYING", gds.getStateOfBoard("1").status());

        // Someone tries to join a started game. Should fail.
        ResponseEntity<?> joinGameResponse3 = gds.joinGame("0");
        assertInstanceOf(ErrorDTO.class, joinGameResponse3.getBody());

        // Someone tries to join a not-existing game. Should fail.
        ResponseEntity<?> joinGameResponse4 = gds.joinGame("2");
        assertInstanceOf(ErrorDTO.class, joinGameResponse4.getBody());
    }

    /** Test whether regular stone placing works. */
    @Test
    public void testPlacingStones() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(9, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PLACE", 4, 4),
                new MakeMoveDTO(2, "PLACE", 3, 4),
                new MakeMoveDTO(1, "PLACE", 1, 8),
                new MakeMoveDTO(2, "PLACE", 5, 5),
        };
        for(MakeMoveDTO mm : moves) {
            MovesToSave movesToSave = gds.makeMove("0", mm);
            ResponseEntity<?> makeMoveResponse = movesToSave.resp();
            assertEquals(200, makeMoveResponse.getStatusCode().value());
        }
        int[][] goodStateBoard = createEmptyBoard(9);
        goodStateBoard[4][4] = 1;
        goodStateBoard[3][4] = 2;
        goodStateBoard[1][8] = 1;
        goodStateBoard[5][5] = 2;
        assertTwoStateBoardsEqual(gds.getStateOfBoard("0").boardState(), goodStateBoard);
    }

    /** Test whether trying to place a stone on an occupied position results in an error, as it should. */
    @Test
    public void testPlacingStoneOnOccupiedPosition() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(13, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO move1 = new MakeMoveDTO(1, "PLACE", 3, 3);
        MakeMoveDTO move2 = new MakeMoveDTO(2, "PLACE", 3, 3);
        gds.makeMove("0", move1);
        MovesToSave movesToSave = gds.makeMove("0", move2);
        ResponseEntity<?> makeMoveResponse = movesToSave.resp();
        assertInstanceOf(ErrorDTO.class, makeMoveResponse.getBody());
    }

    /** Test whether trying to place a stone out of turn results in an error, as it should. */
    @Test
    public void testPlacingStoneOutOfTurn() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "WHITE"), 0);
        gds.joinGame("0");
        MakeMoveDTO move1 = new MakeMoveDTO(1, "PLACE", 3, 3);
        MakeMoveDTO move2 = new MakeMoveDTO(1, "PLACE", 7, 7);
        gds.makeMove("0", move1);
        MovesToSave movesToSave = gds.makeMove("0", move2);
        ResponseEntity<?> makeMoveResponse = movesToSave.resp();
        assertInstanceOf(ErrorDTO.class, makeMoveResponse.getBody());
    }

    /** Test whether surrounded stones get properly captured. */
    @Test
    public void testCapturingStones() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PLACE", 4, 4),
                new MakeMoveDTO(2, "PLACE", 3, 4),
                new MakeMoveDTO(1, "PLACE", 2, 4),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 3, 3),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 3, 5),
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        GetGameDataResponseDTO gameData = gds.getStateOfBoard("0");
        assertEquals(1, gameData.capturedWhite());
        assertEquals(0, gameData.boardState()[3][4]);
    }

    /** Test whether trying to place a stone that would be immediately captured results in an error, as it should. */
    @Test
    public void testPlacingStoneOnPositionWithNoLiberties() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PLACE", 4, 4),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 2, 4),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 3, 3),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 3, 5),
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        MovesToSave movesToSave = gds.makeMove("0", new MakeMoveDTO(2, "PLACE", 3, 4));
        ResponseEntity<?> makeMoveResponse = movesToSave.resp();
        assertInstanceOf(ErrorDTO.class, makeMoveResponse.getBody());
    }

    /**
     * Test whether trying to place a stone on a position with no liberties
     * is actually legal when that move would capture enemy stones.
     */
    @Test
    public void testKamikazeCapture() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PLACE", 3, 1),
                new MakeMoveDTO(2, "PLACE", 3, 2),
                new MakeMoveDTO(1, "PLACE", 4, 2),
                new MakeMoveDTO(2, "PLACE", 4, 3),
                new MakeMoveDTO(1, "PLACE", 5, 3),
                new MakeMoveDTO(2, "PLACE", 3, 4),
                new MakeMoveDTO(1, "PLACE", 4, 4),
                new MakeMoveDTO(2, "PLACE", 2, 3),
                new MakeMoveDTO(1, "PLACE", 3, 5),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 2, 4),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 1, 3),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 2, 2),
                new MakeMoveDTO(2, "PASS", -1, -1)
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        MovesToSave movesToSave = gds.makeMove("0", new MakeMoveDTO(1, "PLACE", 3, 3));
        ResponseEntity<?> makeMoveResponse = movesToSave.resp();
        assertEquals(200, makeMoveResponse.getStatusCode().value());
        GetGameDataResponseDTO gameData = gds.getStateOfBoard("0");
        assertEquals(4, gameData.capturedWhite());
    }

    /**
     * Test whether connected groups of stones get captured together
     * only when all of them, as a group, are surrounded.
     */
    @Test
    public void testGroupLiberties() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PLACE", 2, 2),
                new MakeMoveDTO(2, "PLACE", 3, 2),
                new MakeMoveDTO(1, "PLACE", 3, 1),
                new MakeMoveDTO(2, "PLACE", 4, 2),
                new MakeMoveDTO(1, "PLACE", 4, 1),
                new MakeMoveDTO(2, "PLACE", 5, 2),
                new MakeMoveDTO(1, "PLACE", 5, 1),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 3, 3),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 4, 3),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 5, 3),
                new MakeMoveDTO(2, "PASS", -1, -1)
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        /*
         * Now, two white stone have no empty fields next to them, but they
         * shouldn't be captured, because some of the stones that surround them
         * are also white. Their group as a whole is NOT surrounded.
         */
        GetGameDataResponseDTO gameData = gds.getStateOfBoard("0");
        assertEquals(0, gameData.capturedWhite());

        MovesToSave movesToSave = gds.makeMove("0", new MakeMoveDTO(1, "PLACE", 6, 2));
        ResponseEntity<?> makeMoveResponse = movesToSave.resp();
        /* Now they are indeed surrounded and should get captured. */
        assertEquals(200, makeMoveResponse.getStatusCode().value());
        gameData = gds.getStateOfBoard("0");
        assertEquals(3, gameData.capturedWhite());
    }

    /** Test whether back-to-back taking in a ko position is properly prevented. */
    @Test
    public void testKo() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PLACE", 2, 1),
                new MakeMoveDTO(2, "PLACE", 3, 1),
                new MakeMoveDTO(1, "PLACE", 1, 2),
                new MakeMoveDTO(2, "PLACE", 4, 2),
                new MakeMoveDTO(1, "PLACE", 2, 3),
                new MakeMoveDTO(2, "PLACE", 3, 3),
                new MakeMoveDTO(1, "PLACE", 3, 2)
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }

        /* Before any capturing */
        GetGameDataResponseDTO gameData = gds.getStateOfBoard("0");
        assertEquals(0, gameData.capturedBlack());
        assertEquals(0, gameData.capturedWhite());

        /* White captures and succeeds */
        MovesToSave movesToSave = gds.makeMove("0", new MakeMoveDTO(2, "PLACE", 2, 2));
        ResponseEntity<?> makeMoveResponse = movesToSave.resp();
        assertEquals(200, makeMoveResponse.getStatusCode().value());
        gameData = gds.getStateOfBoard("0");
        assertEquals(1, gameData.capturedBlack());

        /* Black tries to capture back but fails */
        movesToSave = gds.makeMove("0", new MakeMoveDTO(1, "PLACE", 3, 2));
        makeMoveResponse = movesToSave.resp();
        assertInstanceOf(ErrorDTO.class, makeMoveResponse.getBody());
        gameData = gds.getStateOfBoard("0");
        assertEquals(0, gameData.capturedWhite());
    }

    /** Test whether the game gets paused after both players pass back-to-back. */
    @Test
    public void testPausingAfterTwoBackToBackPasses() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PASS", -1, -1),
                new MakeMoveDTO(2, "PASS", -1, -1)
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        GetGameDataResponseDTO gameData = gds.getStateOfBoard("0");
        assertEquals("PAUSED", gameData.status());
    }

    /** Test whether resuming a game takes it back from the PAUSED status to the PLAYING status. */
    @Test
    public void testResuming() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PASS", -1, -1),
                new MakeMoveDTO(2, "PASS", -1, -1)
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        gds.resume("0");
        GetGameDataResponseDTO gameData = gds.getStateOfBoard("0");
        assertEquals("PLAYING", gameData.status());
    }

    /** Test whether the game properly ends and selects a winner after both players agree on a scoring. */
    @Test
    public void testNegotiationAgreement() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PLACE", 2, 2),
                new MakeMoveDTO(2, "PLACE", 3, 2),
                new MakeMoveDTO(1, "PLACE", 3, 1),
                new MakeMoveDTO(2, "PLACE", 4, 2),
                new MakeMoveDTO(1, "PLACE", 4, 1),
                new MakeMoveDTO(2, "PLACE", 5, 2),
                new MakeMoveDTO(1, "PLACE", 5, 1),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 3, 3),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 4, 3),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 5, 3),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PLACE", 6, 2),
                new MakeMoveDTO(2, "PASS", -1, -1),
                new MakeMoveDTO(1, "PASS", -1, -1)
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        /* 3 white stones got captured */

        NegotiateDTO blacksNegotiation = new NegotiateDTO(1, 10, 0, 10, 10, 2, 10);
        NegotiateDTO whitesNegotiation = new NegotiateDTO(2, 10, 0, 10, 10, 2, 10);
        gds.negotiate("0", blacksNegotiation);
        gds.negotiate("0", whitesNegotiation);

        /*
         * Black should win this game, because the score is territory - deadStones - capturedStones.
         * For black, it comes out as 10 - 2 - 0 = 8
         * For white, it comes out as 10 - 0 - 3 = 7
         */
        GetGameDataResponseDTO gameData = gds.getStateOfBoard("0");
        assertEquals("FINISHED", gameData.status());
        assertEquals(1, gameData.winnerId());
    }

    /** Test whether the game doesn't end, as it shouldn't, when the players propose different scoring. */
    @Test
    public void testNegotiationDisagreement() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PASS", -1, -1),
                new MakeMoveDTO(2, "PASS", -1, -1)
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        NegotiateDTO blacksNegotiation = new NegotiateDTO(1, 10, 0, 10, 10, 2, 10);
        NegotiateDTO whitesNegotiation = new NegotiateDTO(2, 10, 20, 10, 10, 2, 10);
        gds.negotiate("0", blacksNegotiation);
        gds.negotiate("0", whitesNegotiation);
        GetGameDataResponseDTO gameData = gds.getStateOfBoard("0");
        assertEquals("PAUSED", gameData.status());
    }

    /** Test whether GameDataService properly responds with one's opponent's proposed scoring. */
    @Test
    public void testGettingOpponentsNegotiation() {
        GameDataService gds = new GameDataService();
        gds.createNewGame(new CreateNewGameDTO(19, "BLACK"), 0);
        gds.joinGame("0");
        MakeMoveDTO[] moves = {
                new MakeMoveDTO(1, "PASS", -1, -1),
                new MakeMoveDTO(2, "PASS", -1, -1)
        };
        for(MakeMoveDTO mm : moves) {
            gds.makeMove("0", mm);
        }
        GetNegotiationDetailsDTO blacksRequest = new GetNegotiationDetailsDTO(1);

        /* White hasn't submitted any scoring propositions yet. The Response should be all -1s. */
        GetNegotiationDetailsResponseDTO responseWithWhitesNegotiation = gds.getNegotiationDetails("0", blacksRequest);
        assertEquals(createEmptyNegotiationResponse(), responseWithWhitesNegotiation);

        NegotiateDTO whitesNegotiation = new NegotiateDTO(2, 10, 0, 10, 10, 2, 10);
        gds.negotiate("0", whitesNegotiation);
        responseWithWhitesNegotiation = gds.getNegotiationDetails("0", blacksRequest);
        assertEquals(whitesNegotiation.livingWhite(), responseWithWhitesNegotiation.livingWhite());
        assertEquals(whitesNegotiation.deadWhite(), responseWithWhitesNegotiation.deadWhite());
        assertEquals(whitesNegotiation.whiteTerritory(), responseWithWhitesNegotiation.whiteTerritory());
        assertEquals(whitesNegotiation.livingBlack(), responseWithWhitesNegotiation.livingBlack());
        assertEquals(whitesNegotiation.deadBlack(), responseWithWhitesNegotiation.deadBlack());
        assertEquals(whitesNegotiation.blackTerritory(), responseWithWhitesNegotiation.blackTerritory());
    }
}
