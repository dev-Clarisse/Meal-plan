package com.takima.backskeleton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.takima.backskeleton.config.InventoryProperties;

@SpringBootApplication
@EnableConfigurationProperties(InventoryProperties.class)
@EnableScheduling
public class BackSkeletonApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackSkeletonApplication.class, args);
	}

}
