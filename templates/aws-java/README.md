# AWS Lambda Function Template

With a gradle task `buildZip`, you can create a bundle which can be used by SeMoDe for Zip upload.
Handler name is `de.uniba.dsg.serverless.java.aws.JavaFunction`.
At the handler, you can also implement your custom Java function and use the return values of `Response` for assessing your functions.