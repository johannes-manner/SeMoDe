package de.uniba.dsg.serverless.pipeline.model.openfaas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.uniba.dsg.serverless.pipeline.calibration.util.QuotaCalculator;
import de.uniba.dsg.serverless.pipeline.model.config.openfaas.OpenFaasConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@JsonSerialize(as = OpenFaasStackModel.class)
public class OpenFaasStackModel {
    @Value("${openfaas.version}")
    private String version;
    private OpenFaasProvider provider;

    private Map<String, OpenFaasFunction> functions;

    @JsonIgnore
    @Value("${openfaas.lang}")
    private String lang;
    @JsonIgnore
    @Value("${openfaas.timeout}")
    private String timeout;
    @JsonIgnore
    @Value("${openfaas.debug}")
    private Boolean debug;

    public OpenFaasStackModel() {
    }

    @Autowired
    public OpenFaasStackModel(OpenFaasProvider provider) {
        this.provider = provider;
    }

    public OpenFaasStackModel createFunctions(OpenFaasConfig openFaasConfig) throws SeMoDeException {
        OpenFaasStackModel openFaasStackModel = new OpenFaasStackModel();
        openFaasStackModel.version = this.version;
        openFaasStackModel.provider = this.provider;
        openFaasStackModel.functions = new HashMap<>();

        // Generate functions
        for (Double quota : QuotaCalculator.calculateQuotas(openFaasConfig.getIncrements())) {
            int limit = (int) (quota * 1000);
            String displayedFunctionName = openFaasConfig.getFunctionName() + "-" + limit;
            OpenFaasFunction function = new OpenFaasFunction(openFaasConfig.getFunctionName(),
                    lang, openFaasConfig.getDockerUsername(), timeout, debug, limit);

            openFaasStackModel.functions.put(displayedFunctionName, function);
        }

        return openFaasStackModel;
    }
}
