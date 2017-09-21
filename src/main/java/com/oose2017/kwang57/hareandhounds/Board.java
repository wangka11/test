package com.oose2017.kwang57.hareandhounds;

import java.util.*;

public class Board {
	private String gameId;
	private int[][] board = new int[3][5];
	private String pieceType;
	public static int HOUND = 1;
	public static int HARE = 2;

	private Map<MapKey, Map<String, Integer>> stallingMonitor = new Hashtable<>();

    private String state = "WAITING_FOR_SECOND_PLAYER";
    static final String TURN_HOUND = "TURN_HOUND";
    static final String TURN_HARE = "TURN_HARE";
    static final String WIN_HARE_E = "WIN_HARE_BY_ESCAPE";
    static final String WIN_HARE_S = "WIN_HARE_BY_STALLING";
    static final String WIN_HOUND = "WIN_HOUND";

    private int[] hound1 = new int[] {1, 0};
    private int[] hound2 = new int[] {0, 1};
    private int[] hound3 = new int[] {1, 2};
    private int[] hare = new int[] {4, 1};

    private Hashtable<String, Integer> stallings = new Hashtable<>();

	public Board(String gameId, String pieceType) {
        this.gameId = gameId;
        this.pieceType = pieceType;
		board[1][0] = HOUND;
		board[0][1] = HOUND;
		board[2][1] = HOUND;
		board[1][4] = HARE;
		board[0][0] = -1;
		board[0][4] = -1;
		board[2][0] = -1;
		board[2][4] = -1;
		Map<String, Integer> houndMonitor = new Hashtable<>();
        Map<String, Integer> hareMonitor = new Hashtable<>();
		houndMonitor.put("hound", 1);
		hareMonitor.put("hare", 1);
        MapKey key1 = new MapKey(new int[]{1, 0});
        MapKey key2 = new MapKey(new int[]{0, 1});
        MapKey key3 = new MapKey(new int[]{1, 2});
        MapKey key4 = new MapKey(new int[]{4, 1});
		stallingMonitor.put(key1, houndMonitor);
        stallingMonitor.put(key2, houndMonitor);
        stallingMonitor.put(key3, houndMonitor);
        stallingMonitor.put(key4, hareMonitor);

	}

    public Board(String gameId, String pieceType, int[] hound1, int[] hound2,
                 int[] hound3, int[] hare) {
        this.gameId = gameId;
        this.pieceType = pieceType;
        board[hound1[1]][hound1[0]] = HOUND;
        board[hound2[1]][hound2[0]] = HOUND;
        board[hound3[1]][hound3[0]] = HOUND;
        board[hare[1]][hare[0]] = HARE;
        this.hound1 = hound1;
        this.hound2 = hound2;
        this.hound3 = hound3;
        this.hare = hare;
        board[0][0] = -1;
        board[0][4] = -1;
        board[2][0] = -1;
        board[2][4] = -1;

    }

	public Map<Integer, List<int[]>> getPieces(){
	    Map<Integer, List<int[]>> result = new Hashtable<>();
	    List<int[]> hounds = new ArrayList<>();
	    for (int i = 0; i < 3; i++){
	        for (int j = 0; j < 5; j++){
	            if (board[i][j] == HOUND){
                    hounds.add(new int[] {j, i});
                } else if (board[i][j] == HARE){
	                result.put(HARE, Arrays.asList(new int[]{j, i}));
                }
            }
        }
        result.put(HOUND, hounds);
        return result;
    }


	public int[][] getBoard() {
		return board;
	}

	public String getGameId(){
		return gameId;
	}

	public String getState() { return state; }

	public String opponentType() {
		return pieceType.equals("HOUND") ? "HARE" : "HOUND";
	}

    public void move(int fromX, int fromY, int toX, int toY){
        if (state.equals("WAITING_FOR_SECOND_PLAYER")){
            state = TURN_HOUND;
        } else{
            String pieceName = "";
            MapKey newPos = new MapKey(new int[] {toX, toY});
            if (state.equals(TURN_HARE)){
                pieceName = "hare";
                state = TURN_HOUND;
                board[toY][toX] = HARE;
                this.hare = new int[] {toX, toY}.clone();

            } else if (state.equals(TURN_HOUND)){
                pieceName = "hound";
                state = TURN_HARE;
                board[toY][toX] = HOUND;
                if (Arrays.equals(this.hound1, new int[]{fromX, fromY})){
                    this.hound1 = new int[]{toX, toY}.clone();
                } else if (Arrays.equals(this.hound2, new int[]{fromX, fromY})){
                    this.hound2 = new int[]{toX, toY}.clone();
                } else if (Arrays.equals(this.hound3, new int[]{fromX, fromY})){
                    this.hound3 = new int[]{toX, toY}.clone();
                }
            }

            if (!pieceName.equals("")){
                Map<String, Integer> temp = new Hashtable<>();
                if (stallingMonitor.get(newPos) != null){
                    temp = stallingMonitor.get(newPos);
                    if (temp.containsKey(pieceName)){
                        temp.put(pieceName, temp.get(pieceName) + 1);
                    } else {
                        temp.put(pieceName, 1);
                    }
                } else{
                    temp.put(pieceName, 1);
                }
                stallingMonitor.put(newPos, temp);
            }
            board[fromY][fromX] = 0;

            String key = Arrays.deepToString(getBoard());
            if(stallings.containsKey(key)){
                stallings.put(key, stallings.get(key)+1);
            } else {
                stallings.put(key, 1);
            }
        }
    }

    public Hashtable<String, Integer> getStallings() {return this.stallings;}

    public Map<MapKey, Map<String, Integer>> getStalling() {return stallingMonitor;}

    public int[] getHound1() {return this.hound1;}

    public int[] getHound2() {return this.hound2;}

    public int[] getHound3() {return this.hound3;}

    public int[] getHare() {return this.hare;}

    public void setWin(String state){ this.state = state; }

	public String getPieceType() {
		return pieceType;
	}

}
