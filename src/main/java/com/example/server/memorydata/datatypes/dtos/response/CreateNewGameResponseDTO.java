package com.example.server.memorydata.datatypes.dtos.response;

/**
 * Response DTO containing the ID of the created game and the ID of the creating player.
 * @param gameId ID of the created game
 * @param playerId ID given to the creating player
 */
public record CreateNewGameResponseDTO(String gameId, int playerId) {}
