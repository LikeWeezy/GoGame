package com.example.clientfx;

import com.example.client.HttpClientHelper;

public record GameSession(HttpClientHelper client, String baseUrl, String gameId, int myPlayerId, boolean replay, HttpClientHelper.ReplayGameResponse history) {

    public String myColorName() {
        return myPlayerId == 1 ? "BLACK" : "WHITE";
    }
}
