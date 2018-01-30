# SeMoDe - Serverless Monitoring and Debugging

## Getting Started

SeMoDe is a prototype for troubleshooting Serverless Lambda function implemented in Java. The idea is the combination between a monitoring service, like CloudWatch, to create notifications, if anomalous function behavior is detected, and debugging the function on a developer's machine. This tool enables an automatic test generation, where a developer must update a view settings and can use a test skeleton as the basis for further investigation of the errors, which caused the failed executions.

The tool itself is in an early stage and needs feedback and participation of the GitHub community. Feel free to contribute :)

## Command Line 

### Usage
 
Following are the five command to use the SeMoDe prototype to generate tests automatically from your Lambda function executions.
 
1. git clone https://github.com/johannes-manner/SeMoDe.git
2. cd SeMoDe
3. gradlew build
4. cd build\libs
5. java -jar SeMoDe.jar "REGION" "LOG GROUP" "SEARCH STRING"
 
### Parameters 

1. Region is the first argument, when executing the prototype. The AWS region
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