# Deployment of azure-fibonacci-js with Serverless

Prerequisites:

- Node.js
- azure-cli


### Change the name of the service

Open `serverless.yml` and change the value of `service`. This will be the name of the Azure Functions app and needs to be a unique name on Azure.


### Installing the dependencies

Run 

```shell
npm install
```

in order to install the `serverless-azure-functions` plugin which is needed to deploy Azure Functions with Serverless.


### Setting up your Azure credentials

Login on Azure with `azure-cli` via the following command and follow the instructions:

```shell
az login
```


### Deploying the service

Once your Azure credentials are set, you can immediately deploy your service via the following command:

```shell
serverless deploy
```

This will create the necessary Azure resources to support the service and events that are defined in your `serverless.yml` file.


### Cleaning up

Once you're finished with your service, you can remove all of the generated Azure resources by simply running the following command:

```shell
serverless remove
```
