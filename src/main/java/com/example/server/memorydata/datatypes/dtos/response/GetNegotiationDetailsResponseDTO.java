package com.example.server.memorydata.datatypes.dtos.response;

import com.example.server.memorydata.datatypes.Negotiation;

/**
 * Response DTO with the proposed scoring of the asking player's opponent.
 * @param livingWhite Proposed number of living white stones
 * @param deadWhite Proposed number of dead white stones
 * @param whiteTerritory Proposed number of fields in white's territory
 * @param livingBlack Proposed number of living white stones
 * @param deadBlack Proposed number of dead white stones
 * @param blackTerritory Proposed number of fields in white's territory
 */
public record GetNegotiationDetailsResponseDTO(int livingWhite, int deadWhite, int whiteTerritory, int livingBlack, int deadBlack, int blackTerritory) {
    /**
     * Create this DTO from a Negotiation instance
     * @param negotiate A player's proposed scoring
     * @return Response DTO with the proposed scoring as in the given Negotiation instance.
     */
    public static GetNegotiationDetailsResponseDTO from(Negotiation negotiate) {
        if(negotiate == null) return empty();
        return new GetNegotiationDetailsResponseDTO(
                negotiate.livingWhite(),
                negotiate.deadWhite(),
                negotiate.whiteTerritory(),
                negotiate.livingBlack(),
                negotiate.deadBlack(),
                negotiate.blackTerritory()
        );
    }

    /**
     * Create this DTO with all fields set to -1 - corresponds to the lack
     * of proposed scoring as of the time of making the request.
     * @return Response DTO with all fields set to -1.
     */
    public static GetNegotiationDetailsResponseDTO empty() {
        return new GetNegotiationDetailsResponseDTO(-1, -1, -1, -1, -1, -1);
    }
}
