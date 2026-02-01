package com.example.server.memorydata.databaselayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<DBGame, String> {}
