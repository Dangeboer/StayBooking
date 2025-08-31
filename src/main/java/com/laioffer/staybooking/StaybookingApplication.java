package com.laioffer.staybooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StaybookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(StaybookingApplication.class, args);
    }

}
