package com.example.server.memorydata.datatypes.dtos.request;

/**
 * Request DTO containing details about a scoring proposition.
 * @param playerId ID of the proposing player
 * @param livingWhite Number of living white stones
 * @param deadWhite Number of dead white stones
 * @param whiteTerritory Number of fields in the white player's territory
 * @param livingBlack Number of living black stones
 * @param deadBlack Number of dead black stones
 * @param blackTerritory Number of fields in the white player's territory
 */
public record NegotiateDTO(int playerId, int livingWhite, int deadWhite, int whiteTerritory, int livingBlack, int deadBlack, int blackTerritory) {}
