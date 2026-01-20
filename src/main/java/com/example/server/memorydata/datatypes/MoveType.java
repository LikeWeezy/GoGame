package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import org.springframework.http.ResponseEntity;

public enum MoveType {
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
    UNKNOWN {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Nieobsłużony typ ruchu: " + mm.moveType()));
        }
    };

    public abstract ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm);

    public static MoveType fromString(String str) {
        return switch(str) {
            case "PLACE" -> PLACE;
            case "PASS" -> PASS;
            case "SURRENDER" -> SURRENDER;
            default -> UNKNOWN;
        };
    }
}
