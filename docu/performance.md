####  Get Performance Data from AWS Cloud Watch

```
java -jar SeMoDe.jar "awsPerformanceData" "REGION" "LOG GROUP" "START TIME FILTER" "END TIME FILTER" ["REST CALLS FILE"]
```

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

```
java -jar SeMoDe.jar "azurePerformanceData" "APPLICATION ID" "API KEY" "SERVICE NAME" "FUNCTION NAME" "START TIME FILTER" "END TIME FILTER" ["REST CALLS FILE"]
```

0. "azurePerformanceData" is a constant, which specifies the used utility mechanism.

1. Application ID of the Application Insights account. It is specified under "API Access" in Application Insights.

2. API Key of the Application Insights account. A key can be created under "API Access" in Application Insights.

3. Service Name of the function app.

4. Function Name of the function the performance data are fetched from. It is specified under the function apps.

5. Desired start time filter. Only logs after specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

6. Desired end time filter. Only logs before specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

7. Optional - File name of the benchmarking log files, which was generated during the execution of the benchmarking utility.
It contains the start and end timestamps from the local REST calls on the developer's machine.

#### Get Performance Data from Google Cloud Functions (Stackdriver)

In contrast to the AWS Lambda and Azure Cloud Functions services, there is no possibility (to our knowledge) to get the
instance id of the executing host and also reading the platform's execution id is quite challenging. Therefore, a JSON
is placed as a log message with the following structure: SEMODE::{"platformId": "execution_id", "instanceId": "host_id", "memorySize": "size in MB"}.
"execution_id" and "host_id" are generated uuids.

The gcloud credentials are set via the Google Cloud SDK command ```gcloud auth application-default login```.

Usage:

```
java -jar SeMoDe.jar "googlePerformanceData" "FUNCTION NAME" "START TIME FILTER" "END TIME FILTER" ["REST CALLS FILE"]
```

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

```
java -jar SeMoDe.jar "openWhiskPerformanceData" "NAMESPACE" "FUNCTION NAME" "AUTHORIZATION TOKEN" "START TIME FILTER" "END TIME FILTER" ["REST CALLS FILE"]
```

0. "googlePerformanceData" is a constant, which specifies the used utility mechanism.

1. The OpenWhisk namespace. A concatenation of org underscore space, e.g. defaultOrg_defaultSpace.

2. Function Name of the function the performance data are fetched from. It is specified under the function apps.

3. Authorization token to authorize the request.

4. Desired start time filter. Only logs after specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

5. Desired end time filter. Only logs before specified time are taken into account. It has the format yyyy-MM-dd_HH:mm .

6. Optional - File name of the benchmarking log files, which was generated during the execution of the benchmarking utility.
It contains the start and end timestamps from the local REST calls on the developer's machine. 
