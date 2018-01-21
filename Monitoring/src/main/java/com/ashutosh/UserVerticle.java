package com.ashutosh;
import com.ashutosh.database.RedisManager;
import com.ashutosh.model.ProfileModel;
import com.ashutosh.model.UserModel;
import  com.ashutosh.database.DatabaseManager;
import com.ashutosh.database.UserDatabaseListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author yogiadytia
 */
public class UserVerticle extends AbstractVerticle {
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ERROR = "error";
    private static final String KEY_FIRSTNAME = "first_name";
    private static final String KEY_LASTNAME = "last_name";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PROFILE_ID = "profile_id";
    private static final Integer STATUS_ON_REQUEST = 1;
    private static final Integer STATUS_ON_SUCCESS = 2;
    private static final Integer STATUS_ON_FAILED = 3;

    private JsonObject convertToJson(UserModel userModel) {
        JsonObject result = new JsonObject();
        result.put(KEY_USER_ID, userModel.getUserId());
        result.put(KEY_FIRSTNAME, userModel.getProfileModel().getFirstName());
        result.put(KEY_LASTNAME, userModel.getProfileModel().getLastName());
        return result;
    }
    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {

        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/login").handler(new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                final long localId = new Date().getTime();
                RedisManager.submitData(vertx, localId , "Login",STATUS_ON_REQUEST , new Date());

                JsonObject jsonBody = routingContext.getBodyAsJson();
                String username = jsonBody.getString("username");
                String password = jsonBody.getString("password");

                final HttpServerResponse response = routingContext.response();
                if (username == null){
                    sendError(400, response);
                } else {
                    DatabaseManager.getSingleton(vertx).RequestGetUserByUsername(username, new UserDatabaseListener() {
                        @Override
                        public void onSuccess(UserModel userModel) {
                            JsonObject jsonFormat = convertToJson(userModel);

                            if (Objects.equals(userModel.getPassword(), password)) {
                                RedisManager.submitData(vertx, localId , "Login", STATUS_ON_SUCCESS , new Date());
                                JsonObject jsonResponse = new JsonObject();
                                jsonResponse.put(KEY_ERROR, false);
                                jsonResponse.put(KEY_MESSAGE, "Success for login");
                                jsonResponse.put("user", jsonFormat);
                                response.putHeader("content-type", "application/json");
                                response.end(jsonResponse.toString());
                            } else {
                                RedisManager.submitData(vertx, localId , "Login", STATUS_ON_FAILED , new Date());
                                JsonObject jsonError = new JsonObject();
                                jsonError.put(KEY_ERROR, true);
                                jsonError.put(KEY_MESSAGE, "Invalid username or password");
                                response.putHeader("content-type", "application/json");
                                response.end(jsonError.toString());
                            }

                        }

                        @Override
                        public void onFailed(String message) {
                            RedisManager.submitData(vertx, localId , "Login", STATUS_ON_FAILED , new Date());
                            JsonObject jsonError = new JsonObject();
                            jsonError.put(KEY_ERROR, true);
                            jsonError.put(KEY_MESSAGE, message);
                            response.end(jsonError.toString());
                        }

                    });
                }
            }
        });
        router.post("/register").handler(new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                final long localId = new Date().getTime();
                RedisManager.submitData(vertx, localId , "Register",STATUS_ON_REQUEST , new Date());
               JsonObject jsonBody=  routingContext.getBodyAsJson() ;
               String username  = jsonBody.getString("username");
               String firstName = jsonBody.getString("first_name");
               String lastName  =jsonBody.getString("last_name");
               String password  =jsonBody.getString("password");
                final HttpServerResponse response = routingContext.response();
                if(username==null&&firstName ==null&&lastName==null&&password==null){
                    sendError(400, response);
                    }
                else DatabaseManager.getSingleton(vertx).RequestForRegistration(username, firstName, lastName, password, new UserDatabaseListener() {
                    @Override
                    public void onSuccess(UserModel userModel) {
                        RedisManager.submitData(vertx, localId , "Register",STATUS_ON_SUCCESS, new Date());
                        JsonObject jsonFormat = convertToJson(userModel);
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.put(KEY_ERROR, false);
                        jsonResponse.put(KEY_MESSAGE, "Successfully Registered");
                        jsonResponse.put("user", jsonFormat);
                        response.putHeader("content-type", "application/json");
                        response.end(jsonResponse.toString());

                    }

                    @Override
                    public void onFailed(String message) {
                        RedisManager.submitData(vertx, localId , "Register",STATUS_ON_FAILED , new Date());
                        JsonObject jsonError = new JsonObject();
                        jsonError.put(KEY_ERROR, true);
                        jsonError.put(KEY_MESSAGE, message);
                        response.putHeader("content-type", "application/json");
                        response.end(jsonError.toString());

                    }
                });
            }
        });
        //Get profile
        router.get("/profile/:userId").handler(new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                final long localId = new Date().getTime();
                RedisManager.submitData(vertx, localId , "GetProfile",STATUS_ON_REQUEST , new Date());
                String userIdInString = routingContext.request().getParam("userId");
                final long userId = Long.parseLong(userIdInString);
                final HttpServerResponse response = routingContext.response();
                if (userId == 0) {
                    sendError(400, response);
                }
                else{
                    DatabaseManager.getSingleton(vertx).RequestGetUserById(userId, new UserDatabaseListener() {
                        @Override
                        public void onSuccess(UserModel userModel) {
                            JsonObject jsonFormat = convertToJson(userModel);
                            if (Objects.equals(userModel.getUserId(), userId)){
                                RedisManager.submitData(vertx, localId , "GetProfile",STATUS_ON_SUCCESS, new Date());
                                JsonObject jsonResponse = new JsonObject();
                                jsonResponse.put(KEY_ERROR, false);
                                jsonResponse.put(KEY_MESSAGE, "Successfully got the Profile");
                                jsonResponse.put("user", jsonFormat);
                                response.putHeader("content-type", "application/json");
                                response.end(jsonResponse.toString());

                            }
                            else {
                                RedisManager.submitData(vertx, localId , "GetProfile",STATUS_ON_FAILED , new Date());
                                JsonObject jsonError = new JsonObject();
                                jsonError.put(KEY_ERROR, true);
                                jsonError.put(KEY_MESSAGE, "Invalid userId");
                                response.putHeader("content-type", "application/json");
                                response.end(jsonError.toString());
                            }
                        }

                        @Override
                        public void onFailed(String message) {
                            RedisManager.submitData(vertx, localId , "GetProfile",STATUS_ON_FAILED , new Date());
                            JsonObject jsonError = new JsonObject();
                            jsonError.put(KEY_ERROR, true);
                            jsonError.put(KEY_MESSAGE, message);
                            response.end(jsonError.toString());
                        }
                    });
                }
            }

        } );
        router.post("/update").handler(new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                final long localId = new Date().getTime();
                RedisManager.submitData(vertx, localId , "Update",STATUS_ON_REQUEST , new Date());
                JsonObject jsonBody=  routingContext.getBodyAsJson() ;
                final long userId = jsonBody.getLong("user_id");
                String firstName  = jsonBody.getString("first_name");
                String lastName = jsonBody.getString("last_name");
                final HttpServerResponse response = routingContext.response();
                if(firstName==null&&lastName==null){
                    sendError(400, response);
                }
                else DatabaseManager.getSingleton(vertx).RequestForUpdate(userId, firstName, lastName, new UserDatabaseListener() {
                    @Override
                    public void onSuccess(UserModel userModel) {
                        RedisManager.submitData(vertx, localId , "Update",STATUS_ON_SUCCESS, new Date());
                        JsonObject jsonFormat = convertToJson(userModel);
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.put(KEY_ERROR, false);
                        jsonResponse.put(KEY_MESSAGE, "Successfully updated the Profile");
                        jsonResponse.put("user", jsonFormat);
                        response.putHeader("content-type", "application/json");
                        response.end(jsonResponse.toString());
                    }

                    @Override
                    public void onFailed(String message) {
                        RedisManager.submitData(vertx, localId , "Update",STATUS_ON_FAILED , new Date());
                        JsonObject jsonError = new JsonObject();
                        jsonError.put(KEY_ERROR, true);
                        jsonError.put(KEY_MESSAGE, message);
                        response.end(jsonError.toString());

                    }
                });
            }
        });
        //DataBase Connection
            vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
                @Override
                public void handle(HttpServerRequest e) {
                    router.accept(e);
                }
            }).listen(8089, new Handler<AsyncResult<HttpServer>>() {
                @Override
                public void handle(AsyncResult<HttpServer> result) {
                    if (result.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }

                }
            });

    }

}