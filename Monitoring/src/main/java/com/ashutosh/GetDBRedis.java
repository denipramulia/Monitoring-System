package com.ashutosh;

import redis.clients.jedis.Jedis;

import java.util.List;

public class GetDBRedis {
    public static void main(String[] args) throws InterruptedException {
        //Connect to Redis localhost
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        System.out.println("Connect to Redis successfully");

        //Get DB Redis with range
        /* List<String> redisDB = jedis.lrange("LOGS" , 0 , 5);
        for(int i = 0 ; i < redisDB.size() ; i++){
            System.out.println(redisDB.get(i));
        } */

        //Get DB Redis with pop
        while(true){
            String getDB = jedis.lpop("LOGS");
            if(getDB.isEmpty()){
                Thread.sleep(10000);
            }
            else{
                StringBuilder sb = new StringBuilder(getDB);
                sb.deleteCharAt(0);
                sb.deleteCharAt(getDB.length()-2);
                getDB = sb.toString();
                System.out.println(getDB);
                String[] getDB_parse = getDB.split(",");
                for(String s : getDB_parse){
                    System.out.println(s);
                }
            }
        }
    }
}
