# SeMoDe - Serverless Monitoring and Debugging

## Getting Started

SeMoDe is a prototype for troubleshooting Serverless Lambda functions implemented in Java. The idea is the combination between a monitoring service, like CloudWatch, to create notifications, if anomalous function behavior is detected, and debugging the function on a developer's machine. This tool enables an automatic test generation, where a developer must update a view settings and can use a test skeleton as the basis for further investigation of the errors, which caused the failed executions. Also further research interests, like performance measurement etc., are integrated in this tool to provide a cli tool for different purposes and platforms.

The tool itself is in an early stage and needs feedback and participation of the GitHub community. Feel free to contribute :)

## Command Line

### Usage

SeMoDe prototype is a command line application, buildable with gradle.

### Test Generation Feature

#### AWS Lambda Test Generation

This feature is to generate tests automatically from your Lambda function executions.

```java -jar SeMoDe.jar "awsSeMoDe" "REGION" "LOG GROUP" "SEARCH STRING" "START TIME FILTER" "END TIME FILTER"```

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

4. Desired start time filter. Only logs after specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

5. Desired end time filter. Only logs before specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

### Performance Data Feature

#### Get Performance Data from AWS Cloud Watch

This feature is to generate .csv files from the cloud watch logs with the metadata, like
memory consumption, billing duration etc.

```java -jar SeMoDe.jar "awsPerformanceData" "REGION" "LOG GROUP" "START TIME FILTER" "END TIME FILTER" ["REST CALLS FILE"]```

0. "awsPerformanceData" is a constant, which specifies the used utility mechanism.

1. Region is the first argument. The AWS region
information is provided as a string, e.g. "eu-west-1" for Ireland, and is related to the
location of the function deployment.

2. Log Group Name is the specification, which Lambda function is under investigation.
The group name is assembled of the prefix "/aws/lambda/" and the Lambda
function name, as shown in the example above. If the function is not deployed to
the specified region, the prototype prints an error to the console and terminate the
execution.

3. Desired start time filter. Only logs after specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

4. Desired end time filter. Only logs before specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

5. Optional - File name of the benchmarking log files, which was generated during the execution of the benchmarking utility.
It contains the start and end timestamps from the local REST calls on the developer's machine.

#### Get Performance Data from Microsoft Azure

This feature generates .csv files from logs of functions maintained by Microsoft Azure.

Usage:

```java -jar SeMoDe.jar "azurePerformanceData" "APPLICATION ID" "API KEY" "FUNCTION NAME" "START TIME FILTER" "END TIME FILTER" ["REST CALLS FILE"]```

0. "azurePerformanceData" is a constant, which specifies the used utility mechanism.

1. Application ID of the Application Insights account. It is specified under "API Access" in Application Insights.

2. API Key of the Application Insights account. A key can be created under "API Access" in Application Insights.

3. Function Name of the function the performance data are fetched from. It is specified under the function apps.

4. Desired start time filter. Only logs after specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

5. Desired end time filter. Only logs before specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

6. Optional - File name of the benchmarking log files, which was generated during the execution of the benchmarking utility.
It contains the start and end timestamps from the local REST calls on the developer's machine.

#### Get Performance Data from Google Cloud Functions (Stackdriver)

In contrast to the AWS Lambda and Azure Cloud Functions services, there is no possibility (to our knowledge) to get the
instance id of the executing host and also reading the platform's execution id is quite challenging. Therefore, a JSON
is placed as a log message with the following structure: SEMODE::{"platformId": "execution_id", "instanceId": "host_id", "memorySize": "size in MB"}.
"execution_id" and "host_id" are generated uuids.

The gcloud credentials are set via the Google Cloud SDK command ```gcloud auth application-default login```.

Usage:

```java -jar SeMoDe.jar "googlePerformanceData" "FUNCTION NAME" "START TIME FILTER" "END TIME FILTER" ["REST CALLS FILE"]```

0. "googlePerformanceData" is a constant, which specifies the used utility mechanism.

1. Function Name of the function the performance data are fetched from. It is specified under the function apps.

2. Desired start time filter. Only logs after specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

3. Desired end time filter. Only logs before specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

4. Optional - File name of the benchmarking log files, which was generated during the execution of the benchmarking utility.
It contains the start and end timestamps from the local REST calls on the developer's machine.  

#### Get Performance Data from IBM OpenWhisk

In contrast to the AWS Lambda and Azure Cloud Functions services, there is no possibility (to our knowledge) to get the
instance id of the executing host and the platform's execution id during the execution of the cloud function. Therefore, a JSON
is placed as a log message with the following structure: SEMODE::{"platformId": "execution_id", "instanceId": "host_id"}.
"execution_id" and "host_id" are generated uuids.

Before starting the functionality, the CLI-plugin must be installed and also configured. Additionally, the cloud-functions plug-in is
needed to access needed commands. Please see the [docs](https://console.bluemix.net/docs/openwhisk/bluemix_cli.html#cloudfunctions_cli).
To get the authorization token, the easiest way is to execute a function via its curl command in the -v verbose mode or follow the following
[tutorial](https://www.raymondcamden.com/2017/07/24/using-postman-with-openwhisk).

Usage:

```java -jar SeMoDe.jar "openWhiskPerformanceData" "NAMESPACE" "FUNCTION NAME" "AUTHORIZATION TOKEN" "START TIME FILTER" "END TIME FILTER" ["REST CALLS FILE"]```

0. "googlePerformanceData" is a constant, which specifies the used utility mechanism.

1. The OpenWhisk namespace. A concatenation of org underscore space, e.g. defaultOrg_defaultSpace.

2. Function Name of the function the performance data are fetched from. It is specified under the function apps.

3. Authorization token to authorize the request.

4. Desired start time filter. Only logs after specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

5. Desired end time filter. Only logs before specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

6. Optional - File name of the benchmarking log files, which was generated during the execution of the benchmarking utility.
It contains the start and end timestamps from the local REST calls on the developer's machine.  

### Benchmarking Tool for REST Interfaces

This feature triggers API requests in a controlled environment. It also logs the start and end time of the request to get
insights into the performance of the corresponding REST interface. The logged start and end times are consistent, because
local timestamps via log4j2 are used on the execution machine.
It provides four modes:

1. Concurrent triggering of a function (mode: "concurrent")

2. Sequential triggering of a function with a fixed time interval between triggering it (mode: "sequentialInterval")

3. Sequential triggering of a function with a fixed time between the execution termination and the next triggering/ request (mode: "sequentialWait")

4. Sequential combined with concurrent triggering of a function. Multiple sequential groups of requests execute functions concurrently. (mode: "sequentialConcurrent")

5. Similar to mode sequentialInterval. This mode triggers functions in an interval with varying delays between execution start times. (mode: "sequentialChangingInterval")

6. Similar to mode sequentialWait. This mode triggers functions with a varying delay between the execution termination and the next triggering/ request. (mode: "sequentialChangingWait")

Usage:

```java -jar SeMoDe.jar "benchmark" "URL" "FILENAME.json" "MODE" <additional parameters>```

0. "benchmark" is a constant, which specifies the used utility mechanism.

1. URL is the HTTP endpoint of the function to trigger it.

2. Local filename to read json, which is used within the POST request body.

3. Mode can be "concurrent", "sequentialInterval", "sequentialWait", "sequentialConcurrent", "sequentialChangingInterval" or "sequentialChangingWait". See above for the description of the modes.

Usage for each mode:  

```java -jar SeMoDe.jar "benchmark" "URL" "FILENAME.json" "concurrent" "NUMBER_OF_REQUESTS"```  

4. Number of function executions.

```java -jar SeMoDe.jar "benchmark" "URL" "FILENAME.json" "sequentialInterval" "NUMBER_OF_REQUESTS" "DELAY"```  

4. Number of function executions.

5. Time between request execution start times in seconds.

```java -jar SeMoDe.jar "benchmark" "URL" "FILENAME.json" "sequentailWait" "NUMBER_OF_REQUESTS" "DELAY"```  

4. Number of requests.

5. Delay between termination of function execution n and start of execution n + 1 in seconds.

```java -jar SeMoDe.jar "benchmark" "URL" "FILENAME.json" "sequentialConcurrent" "NUMBER_OF_GROUPS" "NUMBER_OF_REQUESTS_GROUP" "DELAY"```

4. Number of execution groups.

5. Number of requests in each group.

6. Delay between termination of group g and start of group g + 1 in seconds.

```java -jar SeMoDe.jar "benchmark" "URL" "FILENAME.json" "sequentialChangingInterval" "NUMBER_OF_REQUESTS" ("DELAY")+ ```

4. Total number of executions.

5. List of delays. (at least one)

```java -jar SeMoDe.jar "benchmark" "URL" "FILENAME.json" "sequentialChangingWait" "NUMBER_OF_REQUESTS" ("DELAY")+ ```

4. Total number of executions.

5. List of delays. (at least one)

### Utility Features

This category lists utility features for performing benchmarks in a REST environment.

#### Deployment Package Size Utility

This feature inflates the size of a file or jar/zip by adding random characters. (and a provided escape sequence for files)

Usage:

```java -jar SeMoDe.jar "deploymentSize" "FILE_NAME" "SIZE" ["COMMENT_START"]```

0. "deploymentSize" is a constant, which specifies the used utility mechanism.

1. Path to the file / jar / zip.

2. Desired size of the file / jar / zip in bytes.

3. When a file is enlarged (otherwise optional), the single line comment start string must be provided (e.g. "//" for Java files).
