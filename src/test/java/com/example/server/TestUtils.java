package com.example.server;

import com.example.server.memorydata.datatypes.dtos.response.GetGameDataResponseDTO;
import com.example.server.memorydata.datatypes.dtos.response.GetNegotiationDetailsResponseDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Various utilities needed for testing. */
public final class TestUtils {
    /** Construction of this class is not allowed. */
    private TestUtils() throws Exception {
        throw new Exception("TestUtils class cannot be constructed");
    }
    /**
     * Create an empty 2D int array for holding board state.
     * @param boardSize Size of the board to create - board will be boardSize x boardSize
     * @return A boardSize x boardSize 2D int array with all zeroes.
     */
    public static int[][] createEmptyBoard(int boardSize) {
        int[][] emptyBoard = new int[boardSize][boardSize];
        for(int i = 0; i < boardSize; ++i) {
            for(int j = 0; j < boardSize; ++j) {
                emptyBoard[i][j] = 0;
            }
        }
        return emptyBoard;
    }

    /**
     * Assert that two GetGameDataResponseDTOs have the same values in all corresponding fields.
     * @param first The first DTO
     * @param second The second DTO
     */
    public static void assertTwoGetGameDataResponsesEqual(GetGameDataResponseDTO first, GetGameDataResponseDTO second) {
        assertEquals(first.gameId(), second.gameId());
        assertEquals(first.boardSize(), second.boardSize());
        assertTwoStateBoardsEqual(first.boardState(), second.boardState());
        assertEquals(first.turn(), second.turn());
        assertEquals(first.capturedBlack(), second.capturedBlack());
        assertEquals(first.capturedWhite(), second.capturedWhite());
        assertEquals(first.status(), second.status());
        assertEquals(first.winnerId(), second.winnerId());
    }

    /**
     * Assert that two boards have the same values on all corresponding positions.
     * @param first The first board
     * @param second The second board
     */
    public static void assertTwoStateBoardsEqual(int[][] first, int[][] second) {
        assertEquals(first.length, second.length);
        assertEquals(first[0].length, second[0].length);
        for(int i = 0; i < first.length; ++i) {
            for(int j = 0; j < first[0].length; ++j) {
                assertEquals(first[i][j], second[i][j]);
            }
        }
    }

    /**
     * Create the GetNegotiationDetailsResponseDTO with all fields set to -1 - corresponds to the lack
     * of proposed scoring as of the time of making the request.
     * @return GetNegotiationDetailsResponseDTO with all fields set to -1.
     */
    public static GetNegotiationDetailsResponseDTO createEmptyNegotiationResponse() {
        return new GetNegotiationDetailsResponseDTO(-1, -1, -1, -1, -1, -1);
    }
}
