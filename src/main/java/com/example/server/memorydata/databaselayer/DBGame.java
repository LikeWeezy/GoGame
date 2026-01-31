package com.example.server.memorydata.databaselayer;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "games")
public class DBGame {
    @Id
    private String id;

    @Column(nullable = false)
    private Integer board_size;

    @Column(nullable = false)
    private Integer winner_id;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DBMove> moves;

    public void setId(String id) {
        this.id = id;
    }
    public void setBoardSize(int boardSize) {
        this.board_size = boardSize;
    }
    public void setWinnerId(int winnerId) {
        this.winner_id = winnerId;
    }

    public String getId() {
        return id;
    }
    public int getBoardSize() {
        return board_size;
    }
    public int getWinnerId() {
        return winner_id;
    }
    public List<DBMove> getMoves() {
        return moves;
    }
}