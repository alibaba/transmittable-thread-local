package com.alibaba.demo.hystrix;

import com.netflix.hystrix.strategy.HystrixPlugins;
import org.springframework.boot.SpringApplication;

public class MainApplication {
    public static void main(String[] args) {
        HystrixPlugins.getInstance().registerCommandExecutionHook(new HystrixHook());
        SpringApplication.run(Abc.class, args);
    }
}
