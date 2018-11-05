# Deployment of aws-fibonacci-java with Serverless

Prerequisites:

- gradle
- aws-cli


### Change the name of the service

Open `serverless.yml` and change the value of `service`.


### Setting up your AWS credentials

Login on AWS with `aws-cli` via the following command and follow the instructions:

```shell
aws configure
```


### Building the lambda

Once your AWS credentials are set, you can build your lambda via gradle:

```shell
gradle build
```


### Deploying the service

Once your lambda is build, you can immediately deploy your service via the following command:

```shell
serverless deploy
```

This will create the necessary AWS resources to support the service and events that are defined in your `serverless.yml` file.


### Cleaning up

Once you're finished with your service, you can remove all of the generated AWS resources by simply running the following command:

```shell
serverless remove
```
