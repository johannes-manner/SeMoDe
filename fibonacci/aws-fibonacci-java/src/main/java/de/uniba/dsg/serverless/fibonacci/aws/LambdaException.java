package de.uniba.dsg.serverless.fibonacci.aws;

public class LambdaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LambdaException(String message) {
		super(message);
	}

}
