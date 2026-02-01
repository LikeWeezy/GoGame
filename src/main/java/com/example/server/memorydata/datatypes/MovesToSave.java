package com.example.server.memorydata.datatypes;

import com.example.server.memorydata.datatypes.dtos.request.MakeMoveDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public record MovesToSave(ResponseEntity<?> resp, List<MakeMoveDTO> moves) {}
