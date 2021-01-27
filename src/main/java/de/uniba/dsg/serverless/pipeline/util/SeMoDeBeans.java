package de.uniba.dsg.serverless.pipeline.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeMoDeBeans {

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
