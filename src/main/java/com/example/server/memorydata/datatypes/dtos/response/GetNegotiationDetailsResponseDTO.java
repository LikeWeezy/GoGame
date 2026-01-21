package com.example.server.memorydata.datatypes.dtos.response;

import com.example.server.memorydata.datatypes.Negotiation;

public record GetNegotiationDetailsResponseDTO(int livingWhite, int deadWhite, int whiteTerritory, int livingBlack, int deadBlack, int blackTerritory) {
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

    public static GetNegotiationDetailsResponseDTO empty() {
        return new GetNegotiationDetailsResponseDTO(-1, -1, -1, -1, -1, -1);
    }
}
