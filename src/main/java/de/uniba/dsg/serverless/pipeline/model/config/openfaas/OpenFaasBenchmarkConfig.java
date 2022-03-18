package de.uniba.dsg.serverless.pipeline.model.config.openfaas;

import lombok.Data;

import javax.persistence.Embeddable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@Embeddable
public class OpenFaasBenchmarkConfig {
    private String openFaaSBaseUrl;
    private String openFaaSResourceSetting;
    private int openFaaSNumberOfRuns;

    public List<String> getResourceSettings() {
        if (this.openFaaSResourceSetting == null) {
            return List.of();
        }
        return Arrays.stream(this.openFaaSResourceSetting.split(","))
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.toList());
    }
}
