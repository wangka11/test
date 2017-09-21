package com.oose2017.kwang57.hareandhounds;

import java.util.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import javax.sql.DataSource;

public class GameService {

    private static final String STATE_WAITING = "WAITING_FOR_SECOND_PLAYER";

    private static final String PLAYER_ONE = "1";

    private static final String PLAYER_TWO = "2";

    private static final int ZERO = 0;

    private static final int ONE = 1;

    private static final int TWO = 2;

    private static final int FOUR = 4;
	
    private List<Board> boards = new ArrayList<>();

    private Sql2o db;
	
    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    /**
     * Construct the model with a pre-defined datasource. The current implementation
     * also ensures that the DB schema is created if necessary.
     *
     * @param dataSource
     */
	public GameService(DataSource dataSource) throws GameServiceException {
		db = new Sql2o(dataSource);
		
        //Create the schema for the database if necessary. This allows this
        //program to mostly self-contained. But this is not always what you want;
        //sometimes you want to create the schema externally via a script.
        try (Connection conn = db.open()) {
//            String debug = "DROP TABLE games";
//            conn.createQuery(debug).executeUpdate();

            String sql = "CREATE TABLE IF NOT EXISTS games (gameId INTEGER PRIMARY KEY, " +
                         "state TEXT, pieceType TEXT, hound1_x INTEGER, hound1_y INTEGER, " +
                    "hound2_x INTEGER, hound2_y INTEGER, hound3_x INTEGER, hound3_y INTEGER, " +
                    "hare_x INTEGER, hare_y INTEGER)" ;
            
            conn.createQuery(sql).executeUpdate();

            String sql_query = "SELECT * FROM games" ;

            List<Game> games =  conn.createQuery(sql_query)
                .addColumnMapping("gameId", "gameId")
                .addColumnMapping("state", "state")
                .addColumnMapping("pieceType", "pieceType")
                .addColumnMapping("hound1_x", "hound1_x")
                .addColumnMapping("hound1_y", "hound1_y")
                .addColumnMapping("hound2_x", "hound2_x")
                .addColumnMapping("hound2_y", "hound2_y")
                .addColumnMapping("hound3_x", "hound3_x")
                .addColumnMapping("hound3_y", "hound3_y")
                .addColumnMapping("hare_x", "hare_x")
                .addColumnMapping("hare_y", "hare_y")
                .executeAndFetch(Game.class);

            for (int i=0; i<games.size(); i++){
            	Game game = games.get(i);
            	boards.add(new Board(Integer.toString( game.getGame_id() ), game.getPieceType(),
                        game.getPositions("hound1"), game.getPositions("hound2"),
            			game.getPositions("hound3"), game.getPositions("hare")));
            }
           
        } catch(Sql2oException ex) {
            logger.error("Failed to create schema at startup", ex);
            throw new GameServiceException("Failed to create schema at startup", ex);
        }	
	}


    public List<Piece> describeBoard(String gameId) throws ArrayIndexOutOfBoundsException, GameServiceIdException {
        if (!isValidId(gameId)){
            return null;
        }

//    	if(gameId == null || boards.get(Integer.parseInt(gameId)) == null) {
//    		logger.error("This id does not fetch to a board.");
//    		throw new GameServiceIdException("This id does not fetch to a board.", null);
//    	}

    	List<Piece> result = new ArrayList<>();

    	Map<Integer, List<int[]>> pieces = boards.get(Integer.parseInt(gameId)).getPieces();
    	for (int[] hound : pieces.get(Board.HOUND)){
    	    result.add(new Piece(gameId, null, "HOUND", hound[0], hound[1]));
        }
        int[] hare = pieces.get(Board.HARE).get(0);
        result.add(new Piece(gameId, null, "HARE", hare[0], hare[1]));

    	return result;
    }
    

    public StateMessage describeState(String gameId) throws ArrayIndexOutOfBoundsException, GameServiceIdException{
        if (!isValidId(gameId)){
            return null;
        }

    	return new StateMessage(boards.get(Integer.parseInt(gameId)).getState());
    }


	public NewGame startGame(String body) throws GameServiceException{

        int gameId = boards.size();
        String pieceType = new Gson().fromJson(body, Piece.class).getPieceType();
        Board board = new Board(Integer.toString(gameId), pieceType);

        if (pieceType == null || (!pieceType.equals("HOUND") && !pieceType.equals("HARE"))){
            throw new GameServiceException("GameService.createNewGame: Failed to create new entry", null);
        }

        
        try (Connection conn = db.open()) {
//            String debug = "DROP TABLE games";
//            conn.createQuery(debug).executeUpdate();
            String sql = "INSERT INTO games VALUES (:gameId , :state, :pieceType, :hound1_x, " +
                    ":hound1_y, :hound2_x, :hound2_y, :hound3_x, :hound3_y, :hare_x, :hare_y )";
            conn.createQuery(sql)
	       .addParameter("gameId", gameId)
	       .addParameter("state", STATE_WAITING)
	       .addParameter("pieceType", pieceType)
	       .addParameter("hound1_x", ONE)
	       .addParameter("hound1_y", ZERO)
	       .addParameter("hound2_x", ZERO)
	       .addParameter("hound2_y", ONE)
	       .addParameter("hound3_x", TWO)
	       .addParameter("hound3_y", ONE)
	       .addParameter("hare_x", ONE)
	       .addParameter("hare_y", FOUR)
	       .executeUpdate();
	    } catch(Sql2oException ex) {
	       logger.error("GameService.createNewGame: Failed to create new entry", ex);
	       throw new GameServiceException("GameService.createNewGame: Failed to create new entry", ex);
	    }
        boards.add(board);
        String tem = Integer.toString(gameId);
        return new NewGame(Integer.toString(gameId), PLAYER_ONE, pieceType);
	}

	public NewGame joinGame(String gameId) throws ArrayIndexOutOfBoundsException,
            GameServiceException, GameServiceIdException, GameServiceJoinException {
	    if(isValidId(gameId)) {
            Board board = boards.get(Integer.parseInt(gameId));
            String opponentType = board.opponentType();

            if (boards.get(Integer.parseInt(gameId)).getState() != ("WAITING_FOR_SECOND_PLAYER")) {
                logger.error("Second player already joined");
                throw new GameServiceJoinException("Second player already joined", null);
            }

            String sql = "UPDATE games SET state = :state WHERE gameId = :gameId ";
            try (Connection conn = db.open()) {
                board.move(0, 0, 0, 0);
                conn.createQuery(sql)
                        .addParameter("gameId", Integer.parseInt(gameId))
                        .addParameter("state", Board.TURN_HOUND)
                        .executeUpdate();

                NewGame returns = new NewGame(gameId, PLAYER_TWO, opponentType);
                return returns;
            } catch (Sql2oException ex) {
                logger.error(String.format("GameService.update: Failed to update database for id: %s", gameId), ex);
                throw new GameServiceException("", ex);
            }
        }
        return null;
	}

	public boolean isValidId(String gameId) throws ArrayIndexOutOfBoundsException, GameServiceIdException{
        try{
            if (Integer.parseInt(gameId) >= boards.size()){
                logger.error("INVALID_GAME_ID");
                throw new GameServiceIdException("Invalid game id", null);
            }
            boards.get(Integer.parseInt(gameId));
            return true;
        } catch(ArrayIndexOutOfBoundsException ex){
            logger.error("INVALID_GAME_ID");
            throw new GameServiceIdException("INVALID_GAME_ID", ex);
        } catch(NumberFormatException ex) {
            logger.error("INVALID_GAME_ID");
            throw new GameServiceIdException("INVALID_GAME_ID", ex);
        } catch(NullPointerException ex) {
            logger.error("INVALID_GAME_ID");
            throw new GameServiceIdException("INVALID_GAME_ID", ex);
        }
    }

    public MoveMessage play(String gameId, String body) throws GameServiceException,
            GameServiceIdException, GameServiceMoveException{

	    if (!isValidId(gameId)){
	        return null;
        }
        if(gameId == null || boards.get(Integer.parseInt(gameId)) == null) {
            logger.error("INVALID_GAME_ID");
            throw new GameServiceIdException("INVALID_GAME_ID", null);
        }

    	MovingData data = new Gson().fromJson(body, MovingData.class);

    	if (!data.getPlayerId().equals(PLAYER_ONE) && !data.getPlayerId().equals(PLAYER_TWO)){
            logger.error("INVALID_PLAYER_ID");
            throw new GameServiceIdException("INVALID_PLAYER_ID", null);
        }

        Board board = boards.get(Integer.parseInt(gameId));

        if (!isValidTurn(board, data)){
            logger.error("INCORRECT_TURN");
            throw new GameServiceMoveException("INCORRECT_TURN", new RuntimeException("INCORRECT_TURN"));
        } else if (!isIllegalMove(board, data)){
            logger.error("ILLEGAL_MOVE");
            throw new GameServiceMoveException("ILLEGAL_MOVE", null);
        } else {
            int fromX, fromY, toX, toY;
            fromX = data.getFromX();
            fromY = data.getFromY();
            toX = data.getToX();
            toY = data.getToY();
            board.move(fromX, fromY, toX, toY);

            check(board);

            try (Connection conn = db.open()){
                String sql = "SELECT hound1_x, hound1_y, hound2_x, hound2_y, hound3_x, hound3_y, hare_x, " +
                        "hare_y FROM games WHERE gameId = :gameId";
                Game game = conn.createQuery(sql).addParameter("gameId", gameId)
                        .addColumnMapping("hound1_x", "hound1_x")
                        .addColumnMapping("hound1_y", "hound1_y")
                        .addColumnMapping("hound2_x", "hound2_x")
                        .addColumnMapping("hound2_y", "hound2_y")
                        .addColumnMapping("hound3_x", "hound3_x")
                        .addColumnMapping("hound3_y", "hound3_y")
                        .addColumnMapping("hare_x", "hare_x")
                        .addColumnMapping("hare_y", "hare_y")
                        .executeAndFetchFirst(Game.class);

                String update = "";
                int[] froms = new int[] {fromX, fromY};
                if (Arrays.equals(game.getPositions("hare"), froms)) {
                    update = "UPDATE games SET state = :state, hare_x = :toX, hare_y = :toY" +
                            " WHERE gameId = :gameId";
                }
                for (int i = 1; i < 4; i++) {
                    int[] temp = game.getPositions("hound" + Integer.toString(i));
                    if (Arrays.equals(temp, froms)) {
                        update = "UPDATE games SET state = :state, hound" + Integer.toString(i) +
                                "_x = :toX, hound" + Integer.toString(i) + "_y = :toY " +
                                "WHERE gameId = :gameId";
                        break;
                    }
                }

                if (!update.equals("")){
                    conn.createQuery(update).addParameter("state", board.getState())
                            .addParameter("toX", toX)
                            .addParameter("toY", toY)
                            .addParameter("gameId", Integer.parseInt(gameId))
                            .executeUpdate();
                }
                return new MoveMessage(data.getPlayerId());
            }

        }
    }


    public boolean isValidTurn(Board board, MovingData data) {
	    String hound = board.getPieceType().equals("HOUND") ? PLAYER_ONE : PLAYER_TWO;
        int currentType = board.getBoard()[data.getFromY()][data.getFromX()];
        if (board.getState().equals(board.TURN_HOUND)){
            return currentType == Board.HOUND && hound.equals(data.getPlayerId());
        } else if (board.getState().equals(board.TURN_HARE)) {
            return currentType == Board.HARE && !hound.equals(data.getPlayerId());
        }
        return false;
    }


    public boolean isIllegalMove(Board board, MovingData data) {
        int fromX, fromY, toX, toY;
        fromX = data.getFromX();
        fromY = data.getFromY();
        toX = data.getToX();
        toY = data.getToY();

        if (board.getBoard()[fromY][fromX] == Board.HOUND && toX < fromX ){
            return false;
        }
        if (board.getBoard()[toY][toX] != 0){
            return false;
        }
        if (Math.abs(fromX - toX) > 1 || Math.abs(fromY - toY) > 1){
            return false;
        }
        if ((fromX == 2 && fromY == 0) || (fromX == 1 && fromY == 1)
                || (fromX == 2 && fromY == 2) || (fromX == 3 && fromY == 1)){
            if (Math.abs(fromX - toX) + Math.abs(fromY - toY) > 1){
                return false;
            }
        }
        return true;
    }


    public void check(Board board){
        int[] hound1 = board.getHound1();
        int[] hound2 = board.getHound2();
        int[] hound3 = board.getHound3();
        int[] hare = board.getHare();
        boolean hareTrapped = true;
        Map<MapKey, Map<String, Integer>> stalling = board.getStalling();
        boolean hareStalling= false;
        boolean houndStalling = false;
        for (int i=0; i<3; i++){
            for (int j=0; j<5; j++){
                MovingData data = new MovingData(board.getGameId(), "0", hare[0],hare[1], j, i);
                if (isIllegalMove(board, data)){
                    hareTrapped = false;
                }
                MapKey key = new MapKey(new int[] {j, i});
                if (stalling.containsKey(key)){
                    Map<String, Integer> temp = stalling.get(key);
                    if (temp.containsKey("hare")){
                        if (temp.get("hare") >= 3){
                            hareStalling = true;
                        }
                    } else if (temp.containsKey("hound")){
                        if (temp.get("hound") >= 3){
                            houndStalling = true;
                        }
                    }
                }
            }
        }
        if (hare[0] <= hound1[0] && hare[0] <= hound2[0] && hare[0] <= hound3[0]){
            board.setWin(Board.WIN_HARE_E);
        }

        if (hareTrapped){
            board.setWin(Board.WIN_HOUND);
        }

        if (houndStalling && hareStalling){
            board.setWin(Board.WIN_HARE_S);
        }

    }

   

    //-----------------------------------------------------------------------------//
    // Helper Classes and Methods
    //-----------------------------------------------------------------------------//

    public static class GameServiceException extends Exception {
        public GameServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class GameServiceIdException extends Exception {
        public GameServiceIdException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class GameServiceJoinException extends Exception {
        public GameServiceJoinException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class GameServiceMoveException extends Exception {
        public GameServiceMoveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    
    

}
