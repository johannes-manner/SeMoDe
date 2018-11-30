#### Benchmarking Pipeline

This pipeline guarantees repeatable performance experiments.

We conducted a benchmark and published the results at the Workshop on Serverless Computing, [WoSC4](https://www.serverlesscomputing.org/wosc4/). Here is a per-print version [available](https://www.researchgate.net/publication/328450988_Cold_Start_Influencing_Factors_in_Function_as_a_Service).

```
java -jar SeMoDe.jar "pipelineSetup" "init or load" "NAME"
```
0. "pipelineSetup" is a constant, which specifies the used utility mechanism.

1. Option **init** initializes a new pipeline project with the name (nr.2).
Option **load** loads a predefined pipeline configuration via the folder name (nr.2).

2. Name of the pipeline setup and also name of the root folder, where all other related data is stored.

Further infos comming soon . . . 
