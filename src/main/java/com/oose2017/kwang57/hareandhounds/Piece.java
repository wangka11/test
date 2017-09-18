package com.oose2017.kwang57.hareandhounds;

public class Piece {

    private String gameId, playerId, pieceType;
    private int x, y;

    public Piece(String gameId, String playerId, String pieceType, int x, int y) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.pieceType = pieceType;
        this.x = x;
        this.y = y;
    }

	public String getPieceType() {
		return pieceType;
	}

    
    @Override
    public String toString() {
        return "Piece {" +
                "gameId='" + gameId + '\'' +
                ", playerId='" + playerId + '\'' +
                ", pieceType='" + pieceType + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
