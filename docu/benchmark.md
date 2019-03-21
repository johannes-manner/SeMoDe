#### Benchmarking Modes

1. Concurrent triggering of a function (mode: "concurrent")

2. Sequential triggering of a function with a fixed time interval between triggering it (mode: "sequentialInterval")

4. Sequential combined with concurrent triggering of a function. Multiple sequential groups of requests execute functions concurrently. (mode: "sequentialConcurrent")

5. Similar to mode sequentialInterval. This mode triggers functions in an interval with varying delays between execution start times. (mode: "sequentialChangingInterval")

6. Similar to mode sequentialWait. This mode triggers functions with a varying delay between the execution termination and the next triggering/ request. (mode: "sequentialChangingWait")

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

##### Additional Parameters - Mode "concurrent"

```
java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "concurrent" "NUMBER_OF_REQUESTS"
```  

6. Number of function executions.

##### Additional Parameters - Mode "sequentialInterval"

```
java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "sequentialInterval" "NUMBER_OF_REQUESTS" "DELAY"
```  

6. Number of function executions.

7. Time between request execution start times in seconds.

##### Additional Parameters - Mode "sequentialConcurrent"

```
java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "sequentialConcurrent" "NUMBER_OF_GROUPS" "NUMBER_OF_REQUESTS_GROUP" "DELAY"
```

5. Number of execution groups.

6. Number of requests in each group.

7. Delay between termination of group g and start of group g + 1 in seconds.

##### Additional Parameters - Mode "sequentialChangingInterval"

```
java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "sequentialChangingInterval" "NUMBER_OF_REQUESTS" ("DELAY")+
```

5. Total number of executions.

6. List of delays. (at least one)

##### Additional Parameters - Mode "sequentialChangingWait"

```
java -jar SeMoDe.jar "benchmark" "FUNCTION_NAME" "URL" "FILENAME.json" "sequentialChangingWait" "NUMBER_OF_REQUESTS" ("DELAY")+
```

5. Total number of executions.

6. List of delays. (at least one)

### Utility Features

This category lists utility features for performing benchmarks in a REST environment.

#### Deployment Package Size Utility

This feature inflates the size of a file or jar/zip by adding random characters. (and a provided escape sequence for files)

Usage:

```
java -jar SeMoDe.jar "deploymentSize" "FILE_NAME" "SIZE" ["COMMENT_START"]
```

0. "deploymentSize" is a constant, which specifies the used utility mechanism.

1. Path to the file / jar / zip.

2. Desired size of the file / jar / zip in bytes.

3. When a file is enlarged (otherwise optional), the single line comment start string must be provided (e.g. "//" for Java files).
