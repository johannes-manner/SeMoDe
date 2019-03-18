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
