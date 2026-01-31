package com.example.server.memorydata.databaselayer;

import jakarta.persistence.*;

@Entity
@Table(name = "moves")
public class DBMove {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "game_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_move_game")
    )
    private DBGame game;

    @Column(name = "player_id")
    private Integer player_id;

    @Column(name = "move_type")
    private String move_type;

    @Column(name = "x")
    private Integer x;

    @Column(name = "y")
    private Integer y;

    public void setGame(DBGame dbGame) {
        this.game = dbGame;
    }
    public void setPlayerId(int playerId) {
        this.player_id = playerId;
    }
    public void setMoveType(String moveType) {
        this.move_type = moveType;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getPlayerId() {
        return player_id;
    }
    public String getMoveType() {
        return move_type;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
