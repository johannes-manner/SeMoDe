package de.uniba.dsg.serverless.model;

import com.amazonaws.services.logs.model.OutputLogEvent;
import de.uniba.dsg.serverless.benchmark.logs.aws.AWSLogAnalyzer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a model for data which are needed for the instrumentation of the template file to generate test
 * files based on the log events from the CloudWatch service.
 * Because of the string based nature of log data in AmazonCloudWatch, complex objects are serialized with JSON.
 *
 * @author Johannes Manner
 * @version 1.0
 */
public final class FunctionInstrumentation {

    private final String uniqueIdentifier;
    private final String handlerClass;

    private final String inputClass;
    private final String input;
    private final String context = "";
    private final FunctionExecutionEvent logEvent;
    private final String outputClass;
    private final String jsonOutput;
    private final String handlerMethod;
    private final String fileName;

    /**
     * Constructor of the Function Instrumentation.
     *
     * @param handlerClass  - String
     * @param handlerMethod - String
     * @param inputClass    - String
     * @param input         - Object
     * @param outputClass   - String
     * @param jsonOutput
     * @param logEvent      - FunctionExecutionEvent
     */
    public FunctionInstrumentation(final String handlerClass, final String handlerMethod, final String inputClass, final String input, final String outputClass, final String jsonOutput, final FunctionExecutionEvent logEvent) {
        super();
        this.uniqueIdentifier = logEvent.getRequestId();
        this.handlerClass = handlerClass;
        this.handlerMethod = handlerMethod;
        this.inputClass = inputClass;
        this.input = input;
        this.outputClass = outputClass;
        this.logEvent = logEvent;
        this.jsonOutput = jsonOutput;
        this.fileName = logEvent.getUniqueFileName();
    }

    /**
     * Returns a attribute map for all attribute included in the troubleshooting process.
     * This map is a convenience implementation, because the search and replace functionality
     * is used in a key-value fashion.
     *
     * @return
     */
    public Map<String, String> getAttributeMap() {
        final Map<String, String> attributeMap = new HashMap<>();

        attributeMap.put(AWSLogAnalyzer.HANDLER_CLASS, this.handlerClass);
        attributeMap.put(AWSLogAnalyzer.HANDLER_METHOD, this.handlerMethod);
        attributeMap.put(AWSLogAnalyzer.INPUT_CLASS, this.inputClass);
        attributeMap.put(AWSLogAnalyzer.INPUT_JSON, this.input);
        attributeMap.put(AWSLogAnalyzer.OUTPUT_CLASS, this.outputClass);
        attributeMap.put(AWSLogAnalyzer.FILE_NAME, this.fileName);
        String logContent = "";
        for (final OutputLogEvent event : this.logEvent.getEvents()) {
            logContent += event.getMessage() + "\n";
        }
        attributeMap.put(AWSLogAnalyzer.FUNCTION_LOG, logContent);

        return attributeMap;
    }

    /**
     * Returns the file name for the test class file.
     *
     * @return
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Returns an unique identifier, generated during lambda function execution.
     * (extracted from CloudWatch).
     *
     * @return
     */
    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    @Override
    public String toString() {
        return "FunctionInstrumentation [uniqueIdentifier=" + this.uniqueIdentifier + ", handlerClass=" + this.handlerClass
                + ", inputClass=" + this.inputClass + ", input=" + this.input + ", context=" + this.context + ", logEvent=" + this.logEvent
                + ", outputClass=" + this.outputClass + ", jsonOutput=" + this.jsonOutput + ", handlerMethod=" + this.handlerMethod
                + ", fileName=" + this.fileName + "]";
    }

}
