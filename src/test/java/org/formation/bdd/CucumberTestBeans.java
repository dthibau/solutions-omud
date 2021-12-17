package org.formation.bdd;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class CucumberTestBeans {

	@Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
