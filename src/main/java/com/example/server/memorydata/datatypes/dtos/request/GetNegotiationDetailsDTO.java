package com.example.server.memorydata.datatypes.dtos.request;

/**
 * Request DTO containing the ID of the player that would like to know their opponent's scoring proposition.
 * @param playerId ID of the requesting player
 */
public record GetNegotiationDetailsDTO(int playerId) {}
