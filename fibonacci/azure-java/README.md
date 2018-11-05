# Deployment of azure-fibonacci-java with Maven

Prerequisites:

- Node.js
- azure-cli
- maven


### Change the name of the service

Open `pom.xml` and change the value of `functionAppName`. This will be the name of the Azure Functions app and needs to be a unique name on Azure.


### Installing the dependencies

Run 

```shell
npm install -g azure-functions-core-tools@core
```

in order to install `azure-functions-core-tools@core` which is needed to deploy Azure Functions with Maven.


### Setting up your Azure credentials

Login on Azure with `azure-cli` via the following command and follow the instructions:

```shell
az login
```


### Deploying the service

Once your Azure credentials are set, you can immediately deploy your service via the following commands:

```shell
mvn clean package
mvn azure-functions:deploy
```
