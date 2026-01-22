package com.example.server.memorydata.datatypes.dtos.request;

/**
 * Request DTO containing data about a move being made.
 * @param playerId ID of the requesting player
 * @param moveType Type of move
 * @param x Column of the position on which to place a stone
 * @param y Row of the position on which to place a stone
 */
public record MakeMoveDTO(int playerId, String moveType, int x, int y) {}
