# SeMoDe - Serverless Monitoring and Debugging

## Getting Started

SeMoDe is a prototype for troubleshooting Serverless Lambda functions implemented in Java. The idea is the combination between a monitoring service, like CloudWatch, to create notifications, if anomalous function behavior is detected, and debugging the function on a developer's machine. This tool enables an automatic test generation, where a developer must update a view settings and can use a test skeleton as the basis for further investigation of the errors, which caused the failed executions. Also further research interests, like performance measurement etc., are integrated in this tool to provide a cli tool for different purposes and platforms.

The tool itself is in an early stage and needs feedback and participation of the GitHub community. Feel free to contribute :)

## Command Line

### Usage

SeMoDe prototype is a command line application, buildable with gradle.

### Test Generation Feature

This feature is to generate tests automatically from your Lambda function executions. Further information [here](docu/testGeneration.md).

### Performance Data Feature

This feature is to generate .csv files from the cloud watch logs with the metadata, like
memory consumption, billing duration etc. Further information [here](docu/performance.md)

### Calibration Feature

This feature compares the resources available on FaaS providers with local machines. It conducts the CPU-intensive linpack
benchmark both locally and on the provider infrastructure and creates a mapping between the two platforms. From there, 
FaaS workloads can be simulated locally with corresponding CPU resources.

Further information [here](docu/calibration.md)

### Firecracker

Documentation on the work in progress can be found [here](docu/firecracker.md)

### Benchmarking Tool for REST Interfaces

This feature triggers API requests in a controlled environment. It also logs the start and end time of the request to get
insights into the performance of the corresponding REST interface. The logged start and end times are consistent, because
local timestamps via log4j2 are used on the execution machine.

Further information [here](docu/benchmark.md)

### Simulation Aspects

Further information [here](docu/simulation.md)

##### Useful infos and links

Issue with the google cloud function serverless plugin.
https://github.com/serverless/serverless-google-cloudfunctions/issues/110
