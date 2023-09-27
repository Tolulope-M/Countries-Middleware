package com.klasha;

import com.klasha.constants.ExternalSourceUrl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
@SpringBootApplication
@EnableConfigurationProperties(value = {ExternalSourceUrl.class})
public class KlashaAssessmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KlashaAssessmentApplication.class, args);
    }

}
