//-------------------------------------------------------------------------------------------------------------//
// Code based on a tutorial by Shekhar Gulati of SparkJava at
// https://blog.openshift.com/developing-single-page-web-applications-using-java-8-spark-mongodb-and-angularjs/
//-------------------------------------------------------------------------------------------------------------//

package com.oose2017.kwang57.hareandhounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static spark.Spark.*;

public class GameController {

    private static final String API_CONTEXT = "/hareandhounds/api/games";

    private final GameService gameService;
    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    public GameController(GameService gameService) {
        this.gameService = gameService;
        setupEndpoints();
    }

    private void setupEndpoints() {
        // Start a new game.
        post(API_CONTEXT, "application/json", (request, response) -> {
            try {
                response.status(201);
                return gameService.startGame(request.body());
            } catch (Exception e) {
                logger.error("Failed to create a new game");
                response.status(400);
            }
            return Collections.EMPTY_MAP;
        }, new JsonTransformer());

        // Join a game
        put(API_CONTEXT + "/:id", "application/json", (request, response) -> {
            try {
                response.status(200);
                return gameService.joinGame(request.params(":id"));
            } catch (GameService.GameServiceIdException ex) {
                response.status(404);
                return new ErrorMessage(ex.getMessage());
            } catch (GameService.GameServiceJoinException ex) {
                response.status(410);
                return new ErrorMessage(ex.getMessage());
            }
        }, new JsonTransformer());

        // Play a game
        post(API_CONTEXT + "/:id/turns","application/json", (request, response) -> {
            try {
                response.status(200);
                gameService.play(request.params(":id"), request.body());
            } catch (GameService.GameServiceIdException ex){
                response.status(404);
                return new ErrorMessage(ex.getMessage());
            } catch (GameService.GameServiceMoveException ex) {
                response.status(422);
                return new ErrorMessage(ex.getMessage());
            }
            return Collections.EMPTY_MAP;
        }, new JsonTransformer());

        // Describe the game board
        get(API_CONTEXT + "/:id/board", "application/json", (request, response) -> {
            try {
                response.status(200);
                return gameService.describeBoard(request.params(":id"));
            } catch (GameService.GameServiceIdException ex) {
                response.status(404);
                return new ErrorMessage(ex.getMessage());
            }
            //return Collections.EMPTY_MAP;
        }, new JsonTransformer());

        // fetch state
        get(API_CONTEXT + "/:id/state","application/json", (request, response) -> {
            try {
                response.status(200);
                return gameService.describeState(request.params(":id"));
            } catch (GameService.GameServiceIdException ex) {
                response.status(404);
                return new ErrorMessage(ex.getMessage());
            }
            //return Collections.EMPTY_MAP;
        }, new JsonTransformer());

//        get(API_CONTEXT + "/todos/:id", "application/json", (request, response) -> {
//            try {
//                return todoService.find(request.params(":id"));
//            } catch (TodoService.TodoServiceException ex) {
//                logger.error(String.format("Failed to find object with id: %s", request.params(":id")));
//                response.status(500);
//                return Collections.EMPTY_MAP;
//            }
//        }, new JsonTransformer());
//
//        get(API_CONTEXT + "/todos", "application/json", (request, response)-> {
//            try {
//                return todoService.findAll() ;
//            } catch  (TodoService.TodoServiceException ex) {
//                logger.error("Failed to fetch the list of todos");
//                response.status(500);
//                return Collections.EMPTY_MAP;
//            }
//        }, new JsonTransformer());
//
//        put(API_CONTEXT + "/todos/:id", "application/json", (request, response) -> {
//            try {
//                return todoService.update(request.params(":id"), request.body());
//            } catch (TodoService.TodoServiceException ex) {
//                logger.error(String.format("Failed to update todo with id: %s", request.params(":id")));
//                response.status(500);
//                return Collections.EMPTY_MAP;
//            }
//        }, new JsonTransformer());
//
//        delete(API_CONTEXT + "/todos/:id", "application/json", (request, response) -> {
//            try {
//                todoService.delete(request.params(":id"));
//                response.status(200);
//            } catch (TodoService.TodoServiceException ex) {
//                logger.error(String.format("Failed to delete todo with id: %s", request.params(":id")));
//                response.status(500);
//            }
//            return Collections.EMPTY_MAP;
//        }, new JsonTransformer());

    }
}
