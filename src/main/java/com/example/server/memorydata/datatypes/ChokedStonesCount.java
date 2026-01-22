package com.example.server.memorydata.datatypes;

/**
 * The number of stones in a captured neighborhood and their ID.
 * @param idOfChokedStones ID of the stones
 * @param count Number of the stones
 */
public record ChokedStonesCount(int idOfChokedStones, int count) {}
