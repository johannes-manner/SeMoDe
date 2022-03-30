package de.uniba.dsg.serverless.pipeline.model.config.openfaas;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class OpenFaasCalibrationConfig {

    private String baseUrl;
    private String functionName;
    private int numberOfCalibrations;
    private double increments;
    private String dockerUsername;
    // using basic authentication
    private String username;
    private String password;
    // for file transfer
    private String fileTransferURL;
}
