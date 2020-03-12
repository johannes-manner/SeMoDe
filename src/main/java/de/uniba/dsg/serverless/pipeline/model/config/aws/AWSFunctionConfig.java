package de.uniba.dsg.serverless.pipeline.model.config.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration parameters for a single function.
 */
public class AWSFunctionConfig {
    @Expose
    public String region;
    @Expose
    public String runtime;
    @Expose
    public String awsArnLambdaRole;
    @Expose
    public String functionHandler;
    @Expose
    public int timeout;
    @Expose
    public List<Integer> memorySizes;
    @Expose
    public String targetUrl;
    @Expose
    public String apiKey;
    @Expose
    public String pathToSource;

    public AWSFunctionConfig() {

    }

    public AWSFunctionConfig(final AWSFunctionConfig functionConfig) {
        this.region = functionConfig.region;
        this.runtime = functionConfig.runtime;
        this.awsArnLambdaRole = functionConfig.awsArnLambdaRole;
        this.functionHandler = functionConfig.functionHandler;
        this.timeout = functionConfig.timeout;
        this.memorySizes = List.copyOf(functionConfig.memorySizes);
        this.targetUrl = functionConfig.targetUrl;
        this.apiKey = functionConfig.apiKey;
        this.pathToSource = functionConfig.pathToSource;
    }

    public void update(final String region, final String runtime, final String awsArnLambdaRole, final String functionHandler, final String timeout, final String targetUrl, final String apiKey, final String memorySizes, final String pathToSource) throws IOException {
        if (!"".equals(region)) this.region = region;
        if (!"".equals(runtime)) this.runtime = runtime;
        if (!"".equals(awsArnLambdaRole)) this.awsArnLambdaRole = awsArnLambdaRole;
        if (!"".equals(functionHandler)) this.functionHandler = functionHandler;
        if (!"".equals(timeout) && Ints.tryParse(timeout) != null) this.timeout = Ints.tryParse(timeout);
        if (!"".equals(targetUrl)) this.targetUrl = targetUrl;
        if (!"".equals(apiKey)) this.apiKey = apiKey;
        if (!"".equals(memorySizes)) {
            this.memorySizes = (List<Integer>) new ObjectMapper().readValue(memorySizes, ArrayList.class);
        }
        if (!"".equals(pathToSource) && Files.isDirectory(Paths.get(pathToSource))) {
            this.pathToSource = pathToSource;
        }
    }

    @Override
    public String toString() {
        return "AWSFunctionConfig{" +
                "region='" + this.region + '\'' +
                ", runtime='" + this.runtime + '\'' +
                ", awsArnLambdaRole='" + this.awsArnLambdaRole + '\'' +
                ", functionHandler='" + this.functionHandler + '\'' +
                ", timeout=" + this.timeout +
                ", memorySizes=" + this.memorySizes +
                ", targetUrl='" + this.targetUrl + '\'' +
                ", apiKey='" + this.apiKey + '\'' +
                ", pathToSource=" + this.pathToSource +
                '}';
    }
}