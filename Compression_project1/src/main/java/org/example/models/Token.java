package org.example.models;

public class Token {
    private final int offset;
    private final int length;
    private final int symbol;

    public Token(int offset, int length, int symbol) {
        this.offset = offset;
        this.length = length;
        this.symbol = symbol;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public int getSymbol() {
        return symbol;
    }
}
