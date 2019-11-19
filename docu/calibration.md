### Calibration Feature

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

It is not implemented yet.

#### Run Container
This subcommand runs a docker container and measures and stores the CPU and memory usage in a CSV file. Additionally, meta information about the execution is stored in a JSON file. The logs of the the docker container are also stored.
All three files are stored in "profiling/profiling_TIMESTAMP/".

Usage:

```
java -jar SeMoDe.jar "calibration" "runContainer" IMAGE_NAME ENV_FILE
```

1. "calibration" is a constant, which specifies the used utility mechanism.

2. "runContainer" is a constant, which specifies the used subcommand.

3. Image name specifies the name of the docker image. The image is defined in "profiling/IMAGE_NAME/Dockerfile". The image is built before every run and saved using the tag "semode/IMAGE_NAME".

4. The environment file specifies the file which stores additional environment variables passed to the docker container. It also resides in "profiling/IMAGE_NAME/".

5. TODO mapping using a pair of calibration files.

**Note:**
The ability to configure resources is WIP. It would be great to be able to generate Resource settings by providing a pair of calibrations and a desired simulated memory setting.


