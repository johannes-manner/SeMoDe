# SeMoDe - Serverless Monitoring and Debugging

## Getting Started

SeMoDe is a research prototype investigating different aspects of Serverless computing. The idea is the combination
between a simulation and benchmarking environment as already published. Also further research interests, like
performance measurement etc., are integrated in this tool to provide a cli tool for different purposes and platforms.

The tool itself is in an early stage and needs feedback and participation of the GitHub community. Feel free to
contribute :)

## Prerequisites

1. Install Docker on your machine to be able to use the simulation and benchmarking facility.
2. For AWS Lambda, create an AWS account and install the CLI.

## Setup

1. Run `docker-compose up` to start the PostgreSQL database
2. Run `gradlew bootRun` to start the prototype and access the front page via `http://localhost:8080/setups

## Hardware Calibration feature

introduced in V0.4.

This feature computes a linear regression of your used hardware when working on Linux OS. To run the hardware
calibration go to the root directory of this project and execute the following commands.

```
./gradlew bootJar
java -jar -Dspring.main.web-application-type=NONE build/libs/SeMoDe-0.4.jar hardwareCalibration
```

This will run the Spring Boot application in CLI mode without starting a web server.

You can configure the calibration via the following properties, default configuration is also specified here:

``` 
-Dsemode.hardware.runs=3    # default is 1    - number how often the calibration is executed before computing a linear regression
-Dsemode.hardware.steps=0.4 # default is 0.1  - increments (cpus) which are used to invoke the LINPACK benchmark
```

### Publications

- Overall idea of the research prototype and roadmap: <br/>
  Manner,
  J.: [Towards Performance and Cost Simulation in Function as a Service](https://www.researchgate.net/publication/331174539_Towards_Performance_and_Cost_Simulation_in_Function_as_a_Service)
  Proceedings of the 11th Central European Workshop on Services and their Composition (ZEUS), Bayreuth, Germany,
  February, 15, 2019.
- Related Work for Application Load and their potential impact on FaaS (tag **summersoc13**): <br/>
  Manner, J. and Wirtz,
  G.: [Impact of Application Load in Function as a Service](https://www.researchgate.net/publication/335691397_Impact_of_Application_Load_in_Function_as_a_Service)
  Proceedings of the 13th Symposium and Summer School On Service-Oriented Computing (SummerSoC), Crete, Greece, June,
  17-21, 2019.
- Cold Start Investigation on AWS Lambda and Micorsoft Azure for Java and Javascript (tag **wosc4**): <br/>
  Manner, J., Endreß, M., Heckel, T., Wirtz,
  G.: [Cold Start Influencing Factors in Function as a Service](https://www.researchgate.net/publication/328450988_Cold_Start_Influencing_Factors_in_Function_as_a_Service)
  Proceedings of the 4th Workshop on Serverless Computing (WoSC), Zurich, Switzerland, December, 20, 2018.
- A first contribution to Monitoring and Debugging (intrusive approach) <br/>Manner, J., Kolb, S., Wirtz,
  G.: [Troubeshooting Serverless Functions: A Combined Monitoring and Debugging Approach](https://www.researchgate.net/publication/330915584_Troubleshooting_Serverless_functions_a_combined_monitoring_and_debugging_approach)
  Proceedings of the 12th Summer School on Service Oriented Computing, Crete, Greece, June 25 - June 29, 2018.
