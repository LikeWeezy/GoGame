package com.example.server.memorydata.datatypes.dtos.response;

/**
 * Response DTO containing a message about an error that occurred during the processing of a request.
 * @param message Message about the error
 */
public record ErrorDTO(String message) {}
