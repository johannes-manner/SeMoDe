package de.uniba.dsg.serverless.pipeline.model.openfaas;

import lombok.Data;

@Data
public class OpenFaasFunction {
    private String lang;
    private String handler;
    private String image;
    private OpenFaasEnvironment environment;
    private OpenFaasLimit limits;
    private OpenFaasRequest requests;

    public OpenFaasFunction(String functionName,
                            String lang,
                            String dockerUsername,
                            String timeout,
                            Boolean debug,
                            int limit) {
        this.lang = lang;
        this.handler = "./" + functionName;
        this.image = dockerUsername + "/" + functionName + ":latest";
        this.environment = new OpenFaasEnvironment(debug, timeout);
        String sLimit = "" + limit + "m";
        this.limits = new OpenFaasLimit(sLimit);
        this.requests = new OpenFaasRequest(sLimit);
    }

    @Data
    class OpenFaasEnvironment {
        private boolean write_debug;
        private String read_timeout;
        private String write_timeout;
        private String exec_timeout;

        public OpenFaasEnvironment(boolean write_debug, String timeout) {
            this.write_debug = write_debug;
            this.read_timeout = timeout;
            this.write_timeout = timeout;
            this.exec_timeout = timeout;
        }
    }

    @Data
    class OpenFaasLimit {
        private String cpu;

        public OpenFaasLimit(String cpu) {
            this.cpu = cpu;
        }
    }

    @Data
    class OpenFaasRequest {
        private String cpu;

        public OpenFaasRequest(String cpu) {
            this.cpu = cpu;
        }
    }
}
