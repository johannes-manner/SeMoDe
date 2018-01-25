package de.uniba.dsg.serverless.semode.instrumentation;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uniba.dsg.serverless.semode.util.LogAnalyzer;

/**
 * This class generates instrumentation statements and is closely related to the
 * automated analysis of the logged data. <br/> <br/>
 * 
 * It's an utility class, where all methods are static.
 * 
 * @author Johannes Manner
 * 
 * @version 1.0
 *
 */
public final class SeMoDeInstrumentation {

	/**
	 * This method instruments the function with all needed information for further
	 * troubleshooting. This instrumentation call must be integrated <b>before
	 * executing the request</b> (handle the request)!! The context object of the
	 * lambda function is not part of this implementation (integration of the
	 * context object is planned for version 1.1 of the SeMoDe tool).
	 * 
	 * @param handlerClass
	 * @param handlerMethod
	 * @param inputClass
	 * @param input
	 * @param outputClass
	 * @param logger
	 *            - LambdaLogger from the aws-lambda-java-core
	 */
	public static void instrumentFunction(String handlerClass, String handlerMethod, String inputClass, Object input,
			String outputClass, LambdaLogger logger) {

		String prefix = LogAnalyzer.TROUBLESHOOT_PREFIX + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN;

		logger.log(prefix + LogAnalyzer.HANDLER_CLASS + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN + handlerClass);
		logger.log(prefix + LogAnalyzer.HANDLER_METHOD + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN + handlerMethod);
		logger.log(prefix + LogAnalyzer.INPUT_CLASS + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN + inputClass);

		try {
			logger.log(prefix + LogAnalyzer.INPUT_JSON + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN
					+ new ObjectMapper().writeValueAsString(input));
		} catch (JsonProcessingException e) {
			logger.log(prefix + LogAnalyzer.INPUT_JSON + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN
					+ LogAnalyzer.ERROR_OBJECT_VALUE);
		}
		logger.log(prefix + LogAnalyzer.OUTPUT_CLASS + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN + outputClass);
	}

	/**
	 * This statement is used for instrumenting the output of the function.
	 * Sometimes the error is no exception, but a faulty behavior on the functional
	 * side and therefore the output of the request handling is needed to
	 * troubleshoot these errors.
	 * 
	 * @param output
	 * @param logger
	 *            - LambdaLogger from the aws-lambda-java-core
	 */
	public static void instrumentFunction(Object output, LambdaLogger logger) {
		String prefix = LogAnalyzer.TROUBLESHOOT_PREFIX + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN;

		try {
			logger.log(prefix + LogAnalyzer.OUTPUT_JSON + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN
					+ new ObjectMapper().writeValueAsString(output));
		} catch (JsonProcessingException e) {
			logger.log(prefix + LogAnalyzer.OUTPUT_JSON + LogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN
					+ LogAnalyzer.ERROR_OBJECT_VALUE);
		}

	}

}
