package com.takima.backskeleton;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackSkeletonApplicationTests {

	    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Test
    void contextLoads() {
        System.out.println(">>> Datasource URL: " + datasourceUrl);
    }

}
