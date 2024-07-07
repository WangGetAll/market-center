package com.wjy.marketcenter;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Configurable
@EnableScheduling
public class MarketCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketCenterApplication.class, args);
    }

}
