package com.viettel.deliverymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeliveryManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryManagementApplication.class, args);
    }
}
