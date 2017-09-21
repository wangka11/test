package com.oose2017.kwang57.hareandhounds;

public class BoardMessage {
    private String pieceType;
    private int x, y;

    public BoardMessage(String pieceType, int x, int y){
        this.pieceType = pieceType;
        this.x = x;
        this.y = y;
    }
}
