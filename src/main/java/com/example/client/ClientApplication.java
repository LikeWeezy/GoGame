package com.example.client;

public class ClientApplication {

    public static void main(String[] args) {
        // tworzymy instancje interfejsu
        ConsoleUI ui = new ConsoleUI();
        
        // uruchamiamy petle gry
        ui.start();
    }
}
