package com.ashutosh.database;


import com.ashutosh.model.ProfileModel;
import com.ashutosh.model.UserModel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import redis.clients.jedis.Jedis;

/**
 *
 * @author yogiadytia  DB_USER_ID
 */
public class DatabaseManager {

    SQLClient mySQLClient;
    static DatabaseManager singleton;
    private static final String DB_USER_ID ="userId";
    private static final String DB_FIRST_NAME ="firstName";
    private static final String DB_LAST_NAME ="lastName";
    private static final String DB_PASSWORD ="password";
    private static final String DB_USERNAME ="userName";

    public DatabaseManager(Vertx vertx) {
        mySQLClient = MySQLClient.createShared(vertx, generateConfig());
    }

    public static DatabaseManager getSingleton(Vertx vertx) {
        if (singleton == null) {
            singleton = new DatabaseManager(vertx);
        }
        return singleton;
    }

//get By Username
    public  void RequestGetUserByUsername(final String username,UserDatabaseListener listener){
        mySQLClient.getConnection(new Handler<AsyncResult<SQLConnection>>() {
            @Override
            public void handle(AsyncResult<SQLConnection> sqlConnection) {
                if(sqlConnection.succeeded()) {
                    final SQLConnection connection= sqlConnection.result();
                    String query = "select * from user join profile ON user.userID=profile.userId WHERE user.userName ='"+username+"'";
                    connection.query(query, new Handler<AsyncResult<ResultSet>>() {
                     @Override
                     public void handle(AsyncResult<ResultSet> e) {
                        ResultSet resultSet = e.result();
                        if (resultSet.getRows().size() > 0) {
                            JsonObject jsonResult = resultSet.getRows().get(0);
                            UserModel  userModel = new UserModel();
                            userModel.setUserId(jsonResult.getLong(DB_USER_ID));
                            userModel.setUserName(jsonResult.getString(DB_USERNAME));
                            userModel.setPassword(jsonResult.getString(DB_PASSWORD));
                            ProfileModel profileModel = new   ProfileModel();
                            profileModel.setFirstName(jsonResult.getString(DB_FIRST_NAME));
                            profileModel.setLastName(jsonResult.getString(DB_LAST_NAME));

                            userModel.setProfileModel(profileModel);
                            listener.onSuccess(userModel);
                            connection.close();

                        } else {
                            listener.onFailed("username is not found");
                            connection.close();
                        }
                     }

                 });

                } else {
                    sqlConnection.cause().printStackTrace();
                    listener.onFailed("ERROR CONNECT " + sqlConnection.cause().getMessage());
                }
            }
        });
    }
  //for get profile
  public  void RequestGetUserById(final long userId,UserDatabaseListener listener){
      mySQLClient.getConnection(new Handler<AsyncResult<SQLConnection>>() {
          @Override
          public void handle(AsyncResult<SQLConnection> sqlConnection) {
              if(sqlConnection.succeeded()) {
                  final SQLConnection connection= sqlConnection.result();
                  String query = "select * from user join profile ON user.userID=profile.userId WHERE user.userID ='"+userId+"'";
                  connection.query(query, new Handler<AsyncResult<ResultSet>>() {
                      @Override
                      public void handle(AsyncResult<ResultSet> e) {
                          ResultSet resultSet = e.result();
                          if (resultSet.getRows().size() > 0) {
                              JsonObject jsonResult = resultSet.getRows().get(0);

                              UserModel  userModel = new UserModel();
                              userModel.setUserId(jsonResult.getLong(DB_USER_ID));
                              userModel.setUserName(jsonResult.getString(DB_USERNAME));
                              userModel.setPassword(jsonResult.getString( DB_PASSWORD));

                              ProfileModel profileModel = new   ProfileModel();
                              profileModel.setFirstName(jsonResult.getString(DB_FIRST_NAME));
                              profileModel.setLastName(jsonResult.getString(DB_LAST_NAME));

                              userModel.setProfileModel(profileModel);
                              listener.onSuccess(userModel);
                              connection.close();

                          } else {
                              listener.onFailed("User does  not exist");
                              connection.close();
                          }
                      }

                  });

              }
              else {
                  sqlConnection.cause().printStackTrace();
                  listener.onFailed("ERROR CONNECT " + sqlConnection.cause().getMessage());
              }

          }
      });
  }

    //For Update
    public  void  RequestForUpdate(final long userId,String firstName, String lastName ,UserDatabaseListener listener ){

        mySQLClient.getConnection(new Handler<AsyncResult<SQLConnection>>() {
            @Override
            public void handle(AsyncResult<SQLConnection> sqlConnection) {
                if(sqlConnection.succeeded()){
                    final  SQLConnection connection=sqlConnection.result();
                    String query = "Select * from profile where profile.userId ='"+userId+"'";
                    connection.query(query, new Handler<AsyncResult<ResultSet>>() {
                        @Override
                        public void handle(AsyncResult<ResultSet> e) {
                            ResultSet resultSet = e.result();
                            if (resultSet.getRows().size() <= 0){
                                listener.onFailed("User not exist,please check your profileId");
                                connection.close();
                            } else{
                                String UpdateUser="UPDATE profile SET firstName='"+firstName+"',lastName='"+lastName+"'WHERE profile.userId='"+userId+"'";
                                connection.update(UpdateUser, new Handler<AsyncResult<UpdateResult>>() {
                                    @Override
                                    public void handle(AsyncResult<UpdateResult> updateResultAsyncResult) {
                                        if (updateResultAsyncResult.succeeded()) {


                                            UserModel userModel = new UserModel();
                                            userModel.setUserId(userId);
                                            ProfileModel profileModel = new ProfileModel();
                                            profileModel.setFirstName(firstName);
                                            profileModel.setLastName(lastName);
                                            userModel.setProfileModel(profileModel);
                                            listener.onSuccess(userModel);
                                            connection.close();
                                        }else {
                                            updateResultAsyncResult.cause().printStackTrace();
                                            listener.onFailed("ERROR " + updateResultAsyncResult.cause().getMessage());
                                            connection.close();

                                        }
                                    }
                                });
                            }

                        }
                    });

                }
                else{
                    sqlConnection.cause().printStackTrace();
                    listener.onFailed("ERROR CONNECT " + sqlConnection.cause().getMessage());

                }

            }
        });

    }
     //For registration
    public void RequestForRegistration(final String username,String firstname,String lastname,String password,UserDatabaseListener listener){
        mySQLClient.getConnection(new Handler<AsyncResult<SQLConnection>>() {
            @Override
            public void handle(AsyncResult<SQLConnection> sqlConnection) {
                if(sqlConnection.succeeded())
                {
                    final  SQLConnection connection=sqlConnection.result();
                    String query= "SELECT username FROM user WHERE username = '"+username+"'";
                    connection.query(query, new Handler<AsyncResult<ResultSet>>() {
                        @Override
                        public void handle(AsyncResult<ResultSet> e) {
                            ResultSet resultSet = e.result();
                            if (resultSet.getRows().size() > 0) {
                                listener.onFailed("Username is used, please use another username");
                                connection.close();
                            } else {
                                String queryInsertUser = "INSERT INTO user (userName, password) VALUES (?,?)";
                                JsonArray jsonInsertUser = new JsonArray();
                                jsonInsertUser.add(username);
                                jsonInsertUser.add(password);
                                connection.updateWithParams(queryInsertUser, jsonInsertUser, new Handler<AsyncResult<UpdateResult>>() {
                                    @Override
                                    public void handle(AsyncResult<UpdateResult> updateResultAsyncResult) {
                                        if (updateResultAsyncResult.succeeded()) {
                                            System.out.println("CHECK "+updateResultAsyncResult.result().toJson().toString());
                                            long userId = updateResultAsyncResult.result().getKeys().getLong(0);

                                            String queryInsertProfile = "INSERT INTO profile(userId, firstName, lastName) value(?,?,?)";
                                            JsonArray jsonInsertProfile= new JsonArray();
                                            jsonInsertProfile.add(  userId );
                                            jsonInsertProfile.add(firstname);
                                            jsonInsertProfile.add(lastname);

                                            connection.updateWithParams(queryInsertProfile, jsonInsertProfile, new Handler<AsyncResult<UpdateResult>>() {
                                                @Override
                                                public void handle(AsyncResult<UpdateResult> updateResultAsyncResult) {
                                                    if(updateResultAsyncResult.succeeded()) {
                                                        UserModel userModel = new UserModel();

                                                        ProfileModel profileModel = new ProfileModel();
                                                        profileModel.setFirstName(firstname);
                                                        profileModel.setLastName(lastname);
                                                        userModel.setProfileModel(profileModel);
                                                        listener.onSuccess(userModel);
                                                        connection.close();
                                                    } else{
                                                        sqlConnection.cause().printStackTrace();
                                                        listener.onFailed("ERROR CONNECT " + sqlConnection.cause().getMessage());
                                                    }
                                                }
                                            });
                                        } else {
                                            listener.onFailed("Failed insert data user");
                                            connection.close();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    sqlConnection.cause().printStackTrace();
                    listener.onFailed("ERROR CONNECT " + sqlConnection.cause().getMessage());
                }
            }
        });
    }

    private JsonObject generateConfig() {
        JsonObject jsonConfig = new JsonObject();
        jsonConfig.put("host", "127.0.0.1");
        jsonConfig.put("port", 3306);
        jsonConfig.put("username", "admin");
        jsonConfig.put("password","deni@123");
        jsonConfig.put("database", "customer");

        return jsonConfig;
    }

}
