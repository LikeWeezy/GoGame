package com.example.server.memorydata.datatypes.dtos.response;

/**
 * Response DTO containing a game's data.
 * @param gameId ID of the game
 * @param boardSize Length of the side of the game's square board
 * @param boardState A 2D boardSize x boardSize int array representing
 * the board. The value of 0 denotes an empty field, 1 denotes a black stone, and 2 denotes a white stone.
 * @param turn Denotes which player's turn it is. 1 for black's turn, 2 for white's turn.
 * @param capturedBlack Number of black stones captured by white
 * @param capturedWhite Number of white stones captured by black
 * @param status Denotes the current stage of the game
 * @param winnerId ID of the player that won the game. 0 if the game is not over yet.
 */
public record GetGameDataResponseDTO(String gameId, int boardSize, int[][] boardState, int turn, int capturedBlack, int capturedWhite, String status, int winnerId) {}
