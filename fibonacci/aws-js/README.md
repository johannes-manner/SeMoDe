# Deployment of aws-fibonacci-js with Serverless

Prerequisites:

- aws-cli, serverless framework


### Change the name of the service

Open `serverless.yml` and change the value of `service`.

### Install necessary node modules

```shell
npm install --save line-reader
npm install uuid
```

### Setting up your AWS credentials

Login on AWS with `aws-cli` via the following command and follow the instructions:

```shell
aws configure
```


### Deploying the service

Once your AWS credentials are set, you can immediately deploy your service via the following command:

```shell
serverless deploy
```

This will create the necessary AWS resources to support the service and events that are defined in your `serverless.yml` file.


### Cleaning up

Once you're finished with your service, you can remove all of the generated AWS resources by simply running the following command:

```shell
serverless remove
```
