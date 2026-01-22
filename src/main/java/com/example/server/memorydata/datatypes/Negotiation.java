package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.NegotiateDTO;

/**
 * A player's proposition for the scoring of a game.
 * @param livingWhite Number of living white stones
 * @param deadWhite Number of dead white stones
 * @param whiteTerritory Number of fields in the white player's territory
 * @param livingBlack Number of living black stones
 * @param deadBlack Number of dead black stones
 * @param blackTerritory Number of fields in the white player's territory
 */
public record Negotiation(int livingWhite, int deadWhite, int whiteTerritory, int livingBlack, int deadBlack, int blackTerritory) {

    /**
     * Construct a Negotiation instance from a NegotiateDTO instance.
     * @param negotiateDTO Request DTO containing data about a player's proposition
     * @return A new Negotiation instance with its fields equal to those in the NegotiateDTO (without the player's ID).
     */
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

    /**
     * Compare two instances of Negotiation.
     * @param first The first Negotiation instance
     * @param second The second Negotiation instance
     * @return True if all corresponding fields of the two instances are equal. False otherwise.
     */
    public static boolean areNegotiationsEqual(Negotiation first, Negotiation second) {
        return first.livingWhite() == second.livingWhite() &&
               first.deadWhite() == second.deadWhite() &&
               first.whiteTerritory() == second.whiteTerritory() &&
               first.livingBlack() == second.livingBlack() &&
               first.deadBlack() == second.deadBlack() &&
               first.blackTerritory() == second.blackTerritory();
    }
}
