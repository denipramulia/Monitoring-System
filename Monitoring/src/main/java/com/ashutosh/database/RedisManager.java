package com.ashutosh.database;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import redis.clients.jedis.*;


import java.util.Date;
import java.text.SimpleDateFormat;


public class RedisManager {

    private static final String KEY_ID = "id";
    private static final String KEY_API_NAME = "api_name";
    private static final String KEY_STATUS = "status";
    private static final String KEY_DATE = "date";
    private static final String COLLECTION_NAME_LOGS = "LOGS";


    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
    }

    public static void submitData(Vertx vertx, long id, String apiName, int status, Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");
        String datestring = dateFormat.format(date);

        JsonObject jsonBody = new JsonObject();
        jsonBody.put(KEY_ID, id);
        jsonBody.put(KEY_API_NAME, apiName);
        jsonBody.put(KEY_STATUS , status);
        jsonBody.put(KEY_DATE, datestring);

        RedisClient redisClient = RedisClient.create(vertx, getOption());
        redisClient.lpush(COLLECTION_NAME_LOGS , (jsonBody).toString() ,  new Handler<AsyncResult<Long>> () {
            @Override
            public void handle(AsyncResult<Long> longAsyncResult){
                if(longAsyncResult.succeeded()){
                    System.out.println("Yeah, we made redis");
                }
                else {

                }
            }
        });
    }

    private static RedisOptions getOption() {
        RedisOptions options = new RedisOptions();
        options.setHost("127.0.0.1");
        options.setPort(6379);
        return options;
    }


}
