package com.milosz.podsiadly.tictactoefx;

public class Player {
    private final int number;
    private final char symbol;
    private final String name;

    public Player(int number, char symbol, String name) {
        this.number = number; this.symbol = symbol; this.name = name;
    }
    public int getNumber() { return number; }
    public char getSymbol() { return symbol; }
    public String getName() { return name; }
}
