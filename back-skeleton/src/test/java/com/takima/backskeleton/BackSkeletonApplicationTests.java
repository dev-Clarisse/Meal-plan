package com.takima.backskeleton;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackSkeletonApplicationTests {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Test
    void contextLoads() {
        System.out.println(">>> Datasource URL: " + datasourceUrl);
    }
}
