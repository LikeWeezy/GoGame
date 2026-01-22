package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import com.example.server.memorydata.datatypes.dtos.request.NegotiateDTO;
import com.example.server.memorydata.datatypes.dtos.response.ErrorDTO;
import org.springframework.http.ResponseEntity;

/**
 * Enum representing possible states of a game. Forwards actions appropriately for each state.
 */
public enum GameStatus {
    /** The game has been created but is still waiting for the second player to join. */
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
    /** The second player has joined and now both players take turns making moves. */
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
    /** Both players made a pass back to back, and now they have to negotiate the scoring. */
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
    /** The game is over. */
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

    /**
     * Forward the action of making a move, if it's appropriate.
     * @param game The game in which the move is supposed to be made
     * @param mm Request DTO containing data about the move
     * @return Response DTO that is empty or contains error information.
     */
    public abstract ResponseEntity<?> handleMakeMove(Game game, MakeMoveDTO mm);

    /**
     * Forward the action of registering a player's scoring proposition.
     * @param game The game to which the proposition is supposed to be submitted
     * @param negotiation Request DTO containing the proposed values and the submitting player's ID
     * @return Response DTO that is empty or contains error information.
     */
    public abstract ResponseEntity<?> handleNegotiation(Game game, NegotiateDTO negotiation);

    /**
     * Forward the action of resuming a paused game.
     * @param game The game that is supposed to be resumed
     * @return Response DTO that is empty or contains error information.
     */
    public abstract ResponseEntity<?> handleResuming(Game game);
}
