package de.uniba.dsg.serverless.pipeline.model.openfaas;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("classpath:openfaas.properties")
@JsonSerialize(as = OpenFaasProvider.class)
public class OpenFaasProvider {
    @Value("${openfaas.name}")
    private String name;
    @Value("${openfaas.gateway}")
    private String gateway;
}
