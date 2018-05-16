# SeMoDe - Serverless Monitoring and Debugging

## Getting Started

SeMoDe is a prototype for troubleshooting Serverless Lambda functions implemented in Java. The idea is the combination between a monitoring service, like CloudWatch, to create notifications, if anomalous function behavior is detected, and debugging the function on a developer's machine. This tool enables an automatic test generation, where a developer must update a view settings and can use a test skeleton as the basis for further investigation of the errors, which caused the failed executions. Also further research interests, like performance measurement etc., are integrated in this tool to provide a cli tool for different purposes and platforms.

The tool itself is in an early stage and needs feedback and participation of the GitHub community. Feel free to contribute :)

## Command Line

### Usage

SeMoDe prototype is a command line application, buildable with gradle.

### Test generation feature

This feature is to generate tests automatically from your Lambda function executions.

```java -jar SeMoDe.jar "awsSeMoDe" "REGION" "LOG GROUP" "SEARCH STRING"```

0. "awsSeMoDe" is a constant, which specifies the used utility mechanism.

1. Region is the first argument. The AWS region
information is provided as a string, e.g. "eu-west-1" for Ireland, and is related to the
location of the function deployment.

2. Log Group Name is the specification, which Lambda function is under investigation.
The group name is assembled of the prefix "/aws/lambda/" and the Lambda
function name, as shown in the example above. If the function is not deployed to
the specified region, the prototype prints an error to the console and terminate the
execution.

3. Search String, as its name suggested, is used for searching in all log messages
in the specified log group. The implementation of the prototype is case sensitive,
because this enables a finer-grained result set. If various spelling for a search expression
exists, an user of the prototype must repeat the prototype invocation with
all spellings of the search string.

### New feature: Get performance data from aws cloud watch

This feature is to generate .csv files from the cloud watch logs with the metadata, like
memory consumption, billing duration etc.

```java -jar SeMoDe.jar "awsPerformanceData" "REGION" "LOG GROUP"```

0. "awsPerformanceData" is a constant, which specifies the used utility mechanism.

1. Region is the first argument. The AWS region
information is provided as a string, e.g. "eu-west-1" for Ireland, and is related to the
location of the function deployment.

2. Log Group Name is the specification, which Lambda function is under investigation.
The group name is assembled of the prefix "/aws/lambda/" and the Lambda
function name, as shown in the example above. If the function is not deployed to
the specified region, the prototype prints an error to the console and terminate the
execution.

### New feature: Get performance data from Microsoft Azure

This feature generates .csv files from logs of functions maintained by Microsoft Azure.

Usage:

```java -jar SeMoDe.jar "azurePerformanceData" "APPLICATION ID" "API KEY" "FUNCTION NAME" "START TIME FILTER" "END TIME FILTER"```

0. "azurePerformanceData" is a constant, which specifies the used utility mechanism.

1. Application ID of the Application Insights account. It is specified under "API Access" in Application Insights.

2. API Key of the Application Insights account. A key can be created under "API Access" in Application Insights.

3. Function Name of the function the performance data are fetched from. It is specified under the function apps.

4. Desired start time filter. Only logs after specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

5. Desired end time filter. Only logs before specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .
