package com.example.server.memorydata.datatypes.dtos.response;

import java.util.List;

public record GetGameHistoryResponseDTO(int boardSize, int winnerId, List<PlayerMoveSubDTO> moves) {}
