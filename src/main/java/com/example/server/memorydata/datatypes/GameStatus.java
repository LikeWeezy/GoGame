package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import org.springframework.http.ResponseEntity;

public enum GameStatus {
    CREATING {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
    },
    PLAYING {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            MoveType moveType = MoveType.fromString(mm.moveType());
            return moveType.handleMakeMove(game, mm);
        }
    },
    PAUSED {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
    },
    FINISHED {
        @Override
        public ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Gra nie jest w toku"));
        }
    };

    public abstract ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm);
}
