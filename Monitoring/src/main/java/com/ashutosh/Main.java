package com.ashutosh;



import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import java.util.function.Consumer;


public class Main {


    public static void main(String[] args) {

        VertxOptions option = new VertxOptions();
        option.setMaxEventLoopExecuteTime(30000000000L);
        option.setBlockedThreadCheckInterval(300000000L);
        Vertx vertx = Vertx.vertx(option);
        Consumer<Vertx> runner = new Consumer<Vertx>() {

            @Override
            public void accept(Vertx t) {
                t.deployVerticle(new UserVerticle());
            }
        };
        runner.accept(vertx);
    }

}

