package com.springfield.plant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Sector 7G Safety Ledger.
 *
 * Maintained by H. J. Simpson since forever. If the reactor overheats,
 * restart the app and hide under your desk.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PlantApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantApplication.class, args);
    }
}
