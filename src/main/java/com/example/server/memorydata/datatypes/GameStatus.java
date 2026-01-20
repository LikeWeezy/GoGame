package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.request.NegotiateDTO;
import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import org.springframework.http.ResponseEntity;

public enum GameStatus {
    CREATING {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
        @Override
        public ResponseEntity<?> handleNegotiation(Game game, NegotiateDTO negotiation) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
        @Override
        public ResponseEntity<?> handleResuming(Game game) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
    },
    PLAYING {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            MoveType moveType = MoveType.fromString(mm.moveType());
            return moveType.handleMakeMove(game, mm);
        }
        @Override
        public ResponseEntity<?> handleNegotiation(Game game, NegotiateDTO negotiation) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w fazie dogadywania"));
        }
        @Override
        public ResponseEntity<?> handleResuming(Game game) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w fazie dogadywania"));
        }
    },
    PAUSED {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra jest w fazie dogadywania"));
        }
        @Override
        public ResponseEntity<?> handleNegotiation(Game game, NegotiateDTO negotiateDTO) {
            return game.submitNegotiation(negotiateDTO);
        }
        @Override
        public ResponseEntity<?> handleResuming(Game game) {
            game.resume();
            return ResponseEntity.ok().build();
        }
    },
    FINISHED {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
        @Override
        public ResponseEntity<?> handleNegotiation(Game game, NegotiateDTO negotiation) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
        @Override
        public ResponseEntity<?> handleResuming(Game game) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
    };

    public abstract ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm);
    public abstract ResponseEntity<?> handleNegotiation(Game game, NegotiateDTO negotiation);
    public abstract ResponseEntity<?> handleResuming(Game game);
}
