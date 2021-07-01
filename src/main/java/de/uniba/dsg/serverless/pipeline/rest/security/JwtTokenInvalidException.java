package de.uniba.dsg.serverless.pipeline.rest.security;

public class JwtTokenInvalidException extends RuntimeException {

    public JwtTokenInvalidException() {
        super();
    }

    public JwtTokenInvalidException(String message) {
        super(message);
    }
}
