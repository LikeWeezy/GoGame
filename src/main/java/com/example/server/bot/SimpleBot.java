package com.example.server.bot;

import com.example.server.memorydata.datatypes.Game;
import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
plan
 - jeśli plansza pusta: gra w centrum
 - inaczej: wybiera puste pola "blisko kamieni", preferując konflikt
 - zawsze gra legalne ruchy (używa placePiece), a gdy nie znajdzie: PASS
*/
public final class SimpleBot {
    private static final Random RNG = new Random();

    private SimpleBot() {}

    public static MakeMoveDTO playOneMove(Game game, int playerId) {
        int size = game.getBoardSize();
        int[][] board = game.deepCloneBoardState();

        // 1) pusta plansza -> srodek
        if (isEmpty(board)) {
            int c = size / 2;
            if (game.placePiece(c, c, playerId)) return new MakeMoveDTO(playerId, "PLACE", c, c);
        }

        // 2) kandydaci oceniani prosta heurystyka
        List<int[]> candidates = new ArrayList<>(); // {score,x,y}
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board[x][y] == 0) {
                    int score = neighborhoodScore(board, x, y, playerId);
                    candidates.add(new int[]{score, x, y});
                }
            }
        }

        // troche losowosci, ale dalej "najlepsze" ida pierwsze
        Collections.shuffle(candidates, RNG);
        candidates.sort((a, b) -> Integer.compare(b[0], a[0]));

        for (int[] c : candidates) {
            if (game.placePiece(c[1], c[2], playerId)) return new MakeMoveDTO(playerId, "PLACE", c[1], c[2]);
        }

        // 3) jak nic nie działa -> PASS
        game.pass();
        return new MakeMoveDTO(playerId, "PASS", -1, -1);
    }

    private static boolean isEmpty(int[][] board) {
        for (int[] row : board) {
            for (int v : row) {
                if (v != 0) return false;
            }
        }
        return true;
    }

    private static int neighborhoodScore(int[][] board, int x, int y, int playerId) {
        int opponent = (playerId == 1) ? 2 : 1;
        int score = 0;

        score += neighborValue(board, x - 1, y, opponent);
        score += neighborValue(board, x + 1, y, opponent);
        score += neighborValue(board, x, y - 1, opponent);
        score += neighborValue(board, x, y + 1, opponent);

        // lekka preferencja centrum w remisach
        int cx = board.length / 2;
        int cy = board.length / 2;
        score -= (Math.abs(x - cx) + Math.abs(y - cy)) / 4;

        return score;
    }

    // 0 gdy poza plansza/puste, inaczej:
    // sasiad-przeciwnik = 3, dowolny kamien = 1
    private static int neighborValue(int[][] board, int x, int y, int opponent) {
        if (x < 0 || y < 0 || x >= board.length || y >= board.length) return 0;
        int v = board[x][y];
        if (v == 0) return 0;
        return (v == opponent) ? 3 : 1;
    }
}
