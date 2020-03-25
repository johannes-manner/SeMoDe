package de.uniba.dsg.serverless.semode.instrumentation;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.dsg.serverless.benchmark.log.aws.AWSLogAnalyzer;

/**
 * This class generates instrumentation statements and is closely related to the
 * automated analysis of the logged data. <br/> <br/>
 * <p>
 * It's an utility class, where all methods are static.
 *
 * @author Johannes Manner
 * @version 1.0
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
     * @param logger        - LambdaLogger from the aws-lambda-java-core
     */
    public static void instrumentFunction(final String handlerClass, final String handlerMethod, final String inputClass, final Object input,
                                          final String outputClass, final LambdaLogger logger) {

        final String prefix = AWSLogAnalyzer.TROUBLESHOOT_PREFIX + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN;

        logger.log(prefix + AWSLogAnalyzer.HANDLER_CLASS + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN + handlerClass);
        logger.log(prefix + AWSLogAnalyzer.HANDLER_METHOD + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN + handlerMethod);
        logger.log(prefix + AWSLogAnalyzer.INPUT_CLASS + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN + inputClass);

        try {
            logger.log(prefix + AWSLogAnalyzer.INPUT_JSON + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN
                    + new ObjectMapper().writeValueAsString(input));
        } catch (final JsonProcessingException e) {
            logger.log(prefix + AWSLogAnalyzer.INPUT_JSON + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN
                    + AWSLogAnalyzer.ERROR_OBJECT_VALUE);
        }
        logger.log(prefix + AWSLogAnalyzer.OUTPUT_CLASS + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN + outputClass);
    }

    /**
     * This statement is used for instrumenting the output of the function.
     * Sometimes the error is no exception, but a faulty behavior on the functional
     * side and therefore the output of the request handling is needed to
     * troubleshoot these errors.
     *
     * @param output
     * @param logger - LambdaLogger from the aws-lambda-java-core
     */
    public static void instrumentFunction(final Object output, final LambdaLogger logger) {
        final String prefix = AWSLogAnalyzer.TROUBLESHOOT_PREFIX + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN;

        try {
            logger.log(prefix + AWSLogAnalyzer.OUTPUT_JSON + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN
                    + new ObjectMapper().writeValueAsString(output));
        } catch (final JsonProcessingException e) {
            logger.log(prefix + AWSLogAnalyzer.OUTPUT_JSON + AWSLogAnalyzer.TROUBLESHOOT_SPLIT_PATTERN
                    + AWSLogAnalyzer.ERROR_OBJECT_VALUE);
        }

    }

}
