package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import org.springframework.http.ResponseEntity;

/**
 * Enum representing possible types of moves. Forwards the action of making
 * a move to the appropriate game, if it's appropriate to do so.
 */
public enum MoveType {
    /** Place a stone on the board. */
    PLACE {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            if (mm.playerId() != game.getTurn()) {
                return ResponseEntity.badRequest().body(new ErrorDTO("To nie Twoja tura!"));
            }
            boolean success = game.placePiece(mm.x(), mm.y(), mm.playerId());
            if (!success) return ResponseEntity.badRequest().body(new ErrorDTO("Nielegalny ruch"));
            else return ResponseEntity.ok().build();
        }
    },
    /** Don't place any stones and skip your turn. */
    PASS {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            if (mm.playerId() != game.getTurn()) {
                return ResponseEntity.badRequest().body(new ErrorDTO("To nie Twoja tura!"));
            }
            game.pass();
            return ResponseEntity.ok().build();
        }
    },
    /** Surrender - let the opponent win automatically */
    SURRENDER {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            if (mm.playerId() != game.getTurn()) {
                return ResponseEntity.badRequest().body(new ErrorDTO("To nie Twoja tura!"));
            }
            game.surrender();
            return ResponseEntity.ok().build();
        }
    },
    /** Unknown move type - something likely went wrong with parsing a string to MoveType */
    UNKNOWN {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Nieobsłużony typ ruchu: " + mm.moveType()));
        }
    };

    /**
     * Forwards the action of making a move to the appropriate game, if it's the requesting player's turn.
     * @param game The game in which the move is supposed to be made
     * @param mm Request DTO containing data about the move
     * @return Response DTO that is empty or contains error information.
     */
    public abstract ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm);

    /**
     * Get the appropriate MoveType value from a String that encodes a move type.
     * @param str String specifying a move type
     * @return A MoveType value that corresponds to the string.
     */
    public static MoveType fromString(String str) {
        return switch(str) {
            case "PLACE" -> PLACE;
            case "PASS" -> PASS;
            case "SURRENDER" -> SURRENDER;
            default -> UNKNOWN;
        };
    }
}
