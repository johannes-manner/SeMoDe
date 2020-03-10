# SeMoDe - Serverless Monitoring and Debugging

## Getting Started

SeMoDe is a research prototype investigating different aspects of Serverless computing.
Currently the main focus is on AWS Lambda functions implemented in Java. 
The idea is the combination between a simulation and benchmarking environment as already published.
Also further research interests, like performance measurement etc., are integrated in this tool to provide a cli tool for different purposes and platforms.

The tool itself is in an early stage and needs feedback and participation of the GitHub community. Feel free to contribute :)

## Prerequisites

Installing [Serverless Framework](https://serverless.com/). The Open-Source Edition is sufficient.

## Command Line

SeMoDe prototype is a command line application, buildable with gradle.

### Simulation and Benchmarking Pipeline

The current state of the implemented parts of the simulation and benchmarking pipeline sketched in: <br/>
Manner, J.: [Towards Performance and Cost Simulation in Function as a Service](https://www.researchgate.net/publication/331174539_Towards_Performance_and_Cost_Simulation_in_Function_as_a_Service)
   Proceedings of the 11th Central European Workshop on Services and their Composition (ZEUS), Bayreuth, Germany, February, 15, 2019.  

This pipeline guarantees repeatable performance experiments.
<details>
  <summary>Usage</summary>
  
  All possible configuration properties are stored in [`src/main/resources/pipeline.json`](src/main/resources/pipeline.json). 
  Do NOT change this file. If you change the global configuration, you also should change the `Config.java` correspondingly.
  
  The pipeline setup with the specified name is either created or loaded, if already present.
```
java -jar SeMoDe.jar "pipelineSetup" "SETUP_NAME"
```
0. "pipelineSetup" is a constant, which specifies the used utility mechanism. 
This mechanism via a qualified name is also used for all other utilities implemented in SeMoDe. 

1. Name of the pipeline setup and also name of the root folder, where all other related data is stored.
The generated files are located under `setups/<SETUP_NAME>`

The user interface is implemented as a console UI with the following options:
```
Please type in a command or "exit".
 (status)     Get the current configuration
 (config)     Alter/Specify the current configuration
 (deploy)     Starts the deployment
 (endpoints)  Generate endpoints for benchmarking
 (commands)   Generate benchmarking commands in a bat-file
 (fetch)      Fetch log data from various platforms
 (undeploy)   Undeploying the current cloud functions
 (exit)       Terminate the program
```

 - *status*: prints the current configuration to the console
 - *config*: asks the user for custom inputs <br/>
 <details>
   <summary>Example for *status* and *config*</summary>
   
   ```
   Please type in a command or "exit".
    (status)     Get the current configuration
    (config)     Alter/Specify the current configuration
    (deploy)     Starts the deployment
    (endpoints)  Generate endpoints for benchmarking
    (commands)   Generate benchmarking commands in a bat-file
    (fetch)      Fetch log data from various platforms
    (undeploy)   Undeploying the current cloud functions
    (exit)       Terminate the program
   config
   Insert a valid provider: [ibm, google, aws, azure]
   aws
   Configure property "name"
   Please specify the property. Think about the correct JSON representation for the value. 
   (empty to skip property)
   "aws"
   2020-02-18 13:32:30.369 [main] INFO  de.uniba.dsg.serverless.pipeline.controller.BenchmarkSetupController - Successfully stored property name
   Configure property "memorySize"
   Please specify the property. Think about the correct JSON representation for the value. 
   (empty to skip property)
   [128,256]
   2020-02-18 13:32:37.442 [main] INFO  de.uniba.dsg.serverless.pipeline.controller.BenchmarkSetupController - Successfully stored property memorySize
   Configure property "language"
   Please specify the property. Think about the correct JSON representation for the value. 
   (empty to skip property)
   ["java","js"]
   2020-02-18 13:32:45.298 [main] INFO  de.uniba.dsg.serverless.pipeline.controller.BenchmarkSetupController - Successfully stored property language
   Configure property "deploymentSize"
   Please specify the property. Think about the correct JSON representation for the value. 
   (empty to skip property)
   []
   2020-02-18 13:32:56.738 [main] INFO  de.uniba.dsg.serverless.pipeline.controller.BenchmarkSetupController - Successfully stored property deploymentSize
   Insert a valid provider: [ibm, google, aws, azure]
   
   
   Please type in a command or "exit".
    (status)     Get the current configuration
    (config)     Alter/Specify the current configuration
    (deploy)     Starts the deployment
    (endpoints)  Generate endpoints for benchmarking
    (commands)   Generate benchmarking commands in a bat-file
    (fetch)      Fetch log data from various platforms
    (undeploy)   Undeploying the current cloud functions
    (exit)       Terminate the program
   status
   Printing status of benchmark setup "readme"
   Printing Properties:
   UserConfig [providerConfigs=[ProviderConfig [name=aws, memorySize=[128, 256], language=[java, js], deploymentSize=[]]], benchmarkConfig=BenchmarkConfig [numberThreads=null, benchmarkMode=null, benchmarkParameters=null]]
   ```
   </details>

 - *deploy*: deploys the specified functions using the serverless framework (must be currently located under `fibonacci/<PROVIDER>-<LANGUAE>`)
 - *endpoints*: extracts the REST endpoints of the deployed function out of the serverless output stack and writes them in a file under `setups/<SETUP_NAME>/endpoints`
 - *commands*: generates the commands for benchmarking, therefore the user is asked to supply the benchmarking parameters. 
 The utility is also usable without the pipeline as already mentioned at the beginning of the documentation. 
 For each combination of **function**, **language**, **provider**, and **memorySetting** a single command is generated and the function on the corresponding platform is triggered via an API gateway.
 Therefore, the generated bash file has to be executed. 
 On client side, a single terminal is created for each combination and executes the requests.
 The log results are stored under `setups/<SETUP_NAME>/benchmarkingCommands/logs`. 
 The start and end time on **client side** and also a few metadata fields from the cloud provider, like VM-Identification etc. (see `FunctionTrigger.java` and `CloudFunctionResponse.java` for further details), are logged for a further matching to the platform data, which are retrieved in the next step.
 
 <details>
   <summary>Benchmarking Commands</summary>
   
 #### Benchmarking Tool for REST Interfaces
 
 This feature triggers API requests in a controlled environment. It also logs the start and end time of the request to get
 insights into the performance of the corresponding REST interface. The logged start and end times are consistent, because
 local timestamps via log4j2 are used on the execution machine.
 
 <details>
   <summary>Benchmarking Tool for REST Interfaces Details</summary>
   
  #### Benchmarking Modes
  
  1. Concurrent triggering of a function (mode: "concurrent")
  
  2. Sequential triggering of a function with a fixed time interval between triggering it (mode: "sequentialInterval")
  
  3. Sequential combined with concurrent triggering of a function. Multiple sequential groups of requests execute functions concurrently. (mode: "sequentialConcurrent")
  
  4. This mode triggers functions in an interval with varying delays between execution start times. (mode: "sequentialChangingInterval")
  
  5. This mode triggers the function endpoint based on a csv file. The file contains double values, when a specific call should be submitted. (mode: "arbitraryLoadPattern")
  
  ##### General Usage
  
  ```
  java -jar SeMoDe.jar "benchmark" "URL" "FILENAME.json" "MODE" "NoOfThreads"<additional parameters>
  ```
  
  0. "benchmark" is a constant, which specifies the used utility mechanism.
  
  1. Function name is contained in the log file name.
  
  2. URL is the HTTP endpoint of the function to trigger it.
  
  3. Local filename to read json, which is used within the POST request body.
  
  4. Mode can be "concurrent", "sequentialInterval", "sequentialWait", "sequentialConcurrent", "sequentialChangingInterval" or "sequentialChangingWait". See above for the description of the modes.
  
  5. No of Threads determines the number of the core pool size of the scheduled executor service which is used 
  to send the REST calls. Currently this parameter has to be set directly.
  
  <details>
    <summary>Additional Parameters - Mode "concurrent"</summary>
  
  ```
  java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "concurrent" "NUMBER_OF_REQUESTS"
  ```  
  
  6. Number of function executions.
  
  </details>
  
  <details>
    <summary>Additional Parameters - Mode "sequentialInterval"</summary>
  
  ```
  java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "sequentialInterval" "NUMBER_OF_REQUESTS" "DELAY"
  ```  
  
  6. Number of function executions.
  
  7. Time between request execution start times in seconds.
  
  </details>
  
  <details>
    <summary>Additional Parameters - Mode "sequentialConcurrent"</summary>
  
  ```
  java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "sequentialConcurrent" "NUMBER_OF_GROUPS" "NUMBER_OF_REQUESTS_GROUP" "DELAY"
  ```
  
  5. Number of execution groups.
  
  6. Number of requests in each group.
  
  7. Delay between termination of group g and start of group g + 1 in seconds.
  
  </details>
  
  <details>
    <summary>Additional Parameters - Mode "sequentialChangingInterval"</summary>
    
  
  ```
  java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "sequentialChangingInterval" "NUMBER_OF_REQUESTS" ("DELAY")+
  ```
  
  5. Total number of executions.
  
  6. List of delays. (at least one)
  
  </details>
  
  <details>
    <summary>Additional Parameters - Mode "arbitraryLoadPattern"</summary>
  
  5. File name of the csv load pattern file.
  
  </details>
  
  #### Utility Features for Benchmarking
  
  This category lists utility features for performing benchmarks in a REST environment.
  
  <details>
    <summary>Deployment Package Size Utility</summary>
  
  This feature inflates the size of a file or jar/zip by adding random characters. (and a provided escape sequence for files)
  
  Usage:
  
  ```
  java -jar SeMoDe.jar "deploymentSize" "FILE_NAME" "SIZE" ["COMMENT_START"]
  ```
  
  0. "deploymentSize" is a constant, which specifies the used utility mechanism.
  
  1. Path to the file / jar / zip.
  
  2. Desired size of the file / jar / zip in bytes.
  
  3. When a file is enlarged (otherwise optional), the single line comment start string must be provided (e.g. "//" for Java files).
 
 </details>
 
 </details>  
 </details>
 
 - *fetch*: retrieves the performance data from the logging/monitoring services of the corresponding cloud provider. Details are below.
 There is a possibility to use the local data (collected in the command execution step) and use it for analysis purposes.
 
 <details>
   <summary>Performance Data Feature Details</summary>
   
   #### Performance Data Feature
   
   This feature is to generate .csv files from the corresponding log services with the metadata, like
   memory consumption, billing duration etc.
   
   <details>
     <summary>Performance Data Feature Details</summary>
     
   ####  Get Performance Data from AWS Cloud Watch
   <details>
     <summary>Details here</summary>
     
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
   </details>
   
   #### Get Performance Data from Microsoft Azure
   <details>
     <summary>Details here</summary>
     
   **Currently not actively supported!!**
   
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
   </details>
   
   #### Get Performance Data from Google Cloud Functions (Stackdriver)
   
   <details>
     <summary>Details here</summary>
     
   **Currently not actively supported!!**
   
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
   </details>
   
   #### Get Performance Data from IBM OpenWhisk
   
   <details>
     <summary>Details here</summary>
     
   **Currently not actively supported!!**
   
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
   
   </details>
   
   </details>

</details>
 
 - *undeploy*: undeploys the specified functions using the serverless framework


</details>

### Calibration Feature

This feature compares the resources available on FaaS providers with local machines. It conducts the CPU-intensive linpack
benchmark both locally and on the provider infrastructure and creates a mapping between the two platforms. From there, 
FaaS workloads can be simulated locally with corresponding CPU resources.

<details>
  <summary>Calibration Feature Details</summary>

This feature is currently only tested on AWS Lambda.
 
To use the calibration feature, you need to update the file `src/main/resources/aws_calibration.json` 
and provide the correct API key for S3. (As the name suggest, currently it is only executable on AWS Labmda.)
 
The `calibrate` command has to be executed only once to collect the data on the corresponding platform.
The `mapping` command is only for testing the mapping and the quality of the local and platform data.
The `runContainer` command uses the two calibration files generated for local and platform environments and runs the container locally with the desired setting.
 
#### Calibrate
This subcommand performs a calibration on one of the supported platforms { 'local', 'AWS' }. It runs the linpack
benchmark and stores the results in a CSV file in 'calibration/<platform>/<name>'

Usage:

```
java -jar SeMoDe.jar "calibration" "calibrate" PLATFORM CALIBRATION_NAME
```

0. "calibration" is a constant, which specifies the used utility mechanism.

1. "calibrate" is a constant, which specifies the used subcommand.

2. Platform is the first argument and specifies the platform on which the linpack benchmark is run. Currently supported platforms are "aws" (AWS Lambda) and "local". On both platforms, the benchmark is run using various CPU capabilities, as specified in "src/main/resources/aws_calibration.json". To change the settings, adjust the provided template accordingly. Locally, the benchmark is run inside a docker container with different CPU quotas. They range from 10% to the maximum amount of physical CPU cores.

3. The name under which the results are stored is the second argument.


#### Mapping
This subcommands calculates the corresponding CPU quota for a combination of two linpack calibrations, for example "aws/calibration1" and "local/calibration2".

Usage:

```
java -jar SeMoDe.jar "calibration" "mapping" LOCAL_CALIBRATION PROVIDER_CALIBRATION
```

0. "calibration" is a constant, which specifies the used utility mechanism.

1. "mapping" is a constant, which specifies the used subcommand.

2. The local calibration file stored in /calibration/local/ 

3. The calibration file of a provider stored in /calibration/<provider>/.


#### Run Container
This subcommand runs a docker container and measures and stores the CPU and memory usage in a CSV file. Additionally, meta information about the execution is stored in a JSON file. The logs of the the docker container are also stored.
All three files are stored in "profiling/profiling_TIMESTAMP/".

Usage:

```
java -jar SeMoDe.jar "calibration" "runContainer" IMAGE_NAME ENV_FILE LOCAL_CALIBRATION PROVIDER_CALIBRATION SIMULATED_MEMORY NUMBER_OF_PROFILES
```

1. "calibration" is a constant, which specifies the used utility mechanism.

2. "runContainer" is a constant, which specifies the used subcommand.

3. Image name specifies the name of the docker image. The image is defined in "profiling/IMAGE_NAME/Dockerfile". The image is built before every run and saved using the tag "semode/IMAGE_NAME".

4. The environment file specifies the file which stores additional environment variables passed to the docker container. It also resides in "profiling/IMAGE_NAME/".

5. Local Calibration file (previously generated by calibrate command)

6. Provider Calibration file (previously generated by calibrate command)

7. The Simulated Memory Setting will be used to estimate the equivalent local container quota.

8. The Profiling Container will be executed n times.

</details>  

### Publications

 - Overall idea of the research prototype and roadmap: <br/>
   Manner, J.: [Towards Performance and Cost Simulation in Function as a Service](https://www.researchgate.net/publication/331174539_Towards_Performance_and_Cost_Simulation_in_Function_as_a_Service)
   Proceedings of the 11th Central European Workshop on Services and their Composition (ZEUS), Bayreuth, Germany, February, 15, 2019. 
 - Cold Start Investigation on AWS Lambda and Micorsoft Azure for Java and Javascript: <br/>
 Manner, J., Endreß, M., Heckel, T., Wirtz, G.: [Cold Start Influencing Factors in Function as a Service](https://www.researchgate.net/publication/328450988_Cold_Start_Influencing_Factors_in_Function_as_a_Service)
 Proceedings of the 4th Workshop on Serverless Computing (WoSC), Zurich, Switzerland, December, 20, 2018. 
 - Related Work for Application Load and their potential impact on FaaS: <br/>
   Manner, J. and Wirtz, G.: [Impact of Application Load in Function as a Service](https://www.researchgate.net/publication/335691397_Impact_of_Application_Load_in_Function_as_a_Service)
   Proceedings of the 13th Symposium and Summer School On Service-Oriented Computing (SummerSoC), Crete, Greece, June, 17-21, 2019. 
#### Side Aspects
  
 - Manner, J., Kolb, S., Wirtz, G.: [Troubeshooting Serverless Functions: A Combined Monitoring and Debugging Approach](https://www.researchgate.net/publication/330915584_Troubleshooting_Serverless_functions_a_combined_monitoring_and_debugging_approach)
Proceedings of the 12th Summer School on Service Oriented Computing, Crete, Greece, June 25 - June 29, 2018.

##### Useful infos and links

Issue with the google cloud function serverless plugin.
https://github.com/serverless/serverless-google-cloudfunctions/issues/110

<details>
  <summary>Work in Progress . . . </summary>


### Firecracker

<details>
  <summary>Documentation on the work in progress . . .</summary>
  
  #### Firecracker VM
  
  #### Prerequisites
  
  - Follow the [Quick Start Guide](https://github.com/firecracker-microvm/firecracker/blob/master/docs/getting-started.md) to install firecracker microVM.  
  - Alternatively, if available, install the package `aur/firecracker-git`.  
  - Binaries `kernel_image_path` and `rootfs` must be placed in firecracker/bin/.  
  
  
  #### Defining the Network Bridge
  
  Useful links:
  - [firecracker network docs](https://github.com/firecracker-microvm/firecracker/blob/master/docs/network-setup.md)
  - [LWN Documentation](https://lwn.net/Articles/775736/)
  - [Use Docker bridge for NAT](https://github.com/firecracker-microvm/firecracker/issues/711#issuecomment-450928398)
  
  Firecracker uses a `TAP interface` on the host.
  Create an TAP interface on the host:
  ```sh
  ip tuntap add dev tap0 mode tap
  
  # Option 1
  ip addr add 172.17.0.1/16 dev tap0
  # Option 2 - (worked for me)
  brctl addif docker0 tap0
  
  ip link set tap0 up
  ```
  
  #### Run firecracker
  
  ```sh
  # Make sure firecracker can create its API socket:
  rm -f /tmp/firecracker.socket
  # Replace network address in vmConfig
  firecracker --api-sock /tmp/firecracker.socket --config-file firecracker/vmConfig.json
  ```
  
  Log in as `root` (pw=`root`).
  
  ```sh
  ip link set eth0 up
  ip addr add dev eth0 172.17.0.3/16
  
  # Try network with ping
  ```
  
  
  Stop firecracker by sending a kill/ reboot command.
</details>

### Simulation Aspects

<details>
  <summary>Performance Data Feature Details</summary>
  
  #### Simulating Number of Concurrent Running Containers
  
  This utility can test different scenarios for a single cloud function and computes the number of running cloud function containers at a given point in time.
  
  A user can simulte the scaling behaviour, which is essential for an assessment how the cloud function part will influence other parts in an architecture (prevention for DDos attacking yourself).
  
  ```
  java -jar SeMoDe.jar "loadSimulation" "file.csv" "json, list of simulation inputs"
  ```
  
  Example JSON, which matches the given interface.
  ```json
  [
    {
      "averageExecutionTime": 4.211,
      "averageColdStartTime": 1.750,
      "shutdownAfter": 1800.0
    }
  ]
  ```
  
  #### Publication
  
  Manner, J. and Wirtz, G.: [Impact of Application Load in Function as a Service](https://www.researchgate.net/publication/335691397_Impact_of_Application_Load_in_Function_as_a_Service)
     Proceedings of the 13th Symposium and Summer School On Service-Oriented Computing (SummerSoC), Crete, Greece, June, 17-21, 2019.
  
</details>

</details>
  
<details>
  <summary>Outdated Features</summary>
  
  
### Test Generation Feature

This feature is to generate tests automatically from your Lambda function executions.

Not actively developed any more (February 2020)!
<details>
  <summary>Test Generation Feature Details</summary>
  
  #### AWS Lambda Test Generation
  
  ```
  java -jar SeMoDe.jar "awsSeMoDe" "REGION" "LOG GROUP" "SEARCH STRING" "START TIME FILTER" "END TIME FILTER"
  ```
  
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

  #### Publication
  
  Manner, J., Kolb, S., Wirtz, G.: [Troubeshooting Serverless Functions: A Combined Monitoring and Debugging Approach](https://www.researchgate.net/publication/330915584_Troubleshooting_Serverless_functions_a_combined_monitoring_and_debugging_approach)
  Proceedings of the 12th Summer School on Service Oriented Computing, Crete, Greece, June 25 - June 29, 2018.
  
</details>

</details>