package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.NegotiateDTO;

public record Negotiation(int livingWhite, int deadWhite, int whiteTerritory, int livingBlack, int deadBlack, int blackTerritory) {

    public static Negotiation fromNegotiateDTO(NegotiateDTO negotiateDTO) {
        return new Negotiation(
                negotiateDTO.livingWhite(),
                negotiateDTO.deadWhite(),
                negotiateDTO.whiteTerritory(),
                negotiateDTO.livingBlack(),
                negotiateDTO.deadBlack(),
                negotiateDTO.blackTerritory()
        );
    }

    public static boolean areNegotiationsEqual(Negotiation first, Negotiation second) {
        return first.livingWhite() == second.livingWhite() &&
               first.deadWhite() == second.deadWhite() &&
               first.whiteTerritory() == second.whiteTerritory() &&
               first.livingBlack() == second.livingBlack() &&
               first.deadBlack() == second.deadBlack() &&
               first.blackTerritory() == second.blackTerritory();
    }
}
