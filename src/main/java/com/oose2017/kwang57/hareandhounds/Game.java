package com.oose2017.kwang57.hareandhounds;

public class Game {
	
	private int gameId, hound1_x, hound1_y, hound2_x,
            hound2_y, hound3_x, hound3_y, hare_x, hare_y;
	private String state, pieceType;

	public Game(int game_id, int hound1_x, int hound1_y, int hound2_x,
				int hound2_y, int hound3_x, int hound3_y, int hare_x, int hare_y,
				String state, String pieceType) {
		this.gameId = game_id;
		this.hound1_x = hound1_x;
		this.hound1_y = hound1_y;
		this.hound2_x = hound2_x;
		this.hound2_y = hound2_y;
		this.hound3_x = hound3_x;
		this.hound3_y = hound3_y;
		this.hare_x = hare_x;
		this.hare_y = hare_y;
		this.state = state;
		this.pieceType = pieceType;
	}

	public int[] getPositions(String piece){
	    if (piece.equals("hound1")){
	        return new int[] {this.hound1_x, this.hound1_y};
        } else if (piece.equals("hound2")){
	        return new int[] {this.hound2_x, this.hound2_y};
        } else if (piece.equals("hound3")){
            return new int[] {this.hound3_x, this.hound3_y};
        } return new int[] {this.hare_x, this.hare_y};
    }
	public int getGame_id() {
		return gameId;
	}


	public String getState() {
		return state;
	}


	public String getPieceType() {
		return pieceType;
	}
}
