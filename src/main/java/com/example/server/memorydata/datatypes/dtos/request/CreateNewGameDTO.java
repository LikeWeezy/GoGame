package com.example.server.memorydata.datatypes.dtos.request;

/**
 * Request DTO containing parameters needed to create a new game.
 * @param boardSize Length of the side of the game's square board
 * @param requestedColor The color to assign to the requesting player
 */
public record CreateNewGameDTO(int boardSize, String requestedColor) {}
