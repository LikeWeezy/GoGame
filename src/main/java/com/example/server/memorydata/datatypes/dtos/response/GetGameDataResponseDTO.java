package com.example.server.memorydata.datatypes.dtos.response;

public record GetGameDataResponseDTO(String gameId, int boardSize, int[][] boardState, int turn, int capturedBlack, int capturedWhite, String status) {}
