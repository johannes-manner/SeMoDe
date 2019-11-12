'use strict';
const AWS = require('aws-sdk');
const exec = require('child_process').exec;

// If parameter experiment is set, the profile is saved under that name.
// Else, the current time is used as a name.
exports.handler = (event, context) => {
  const s3 = new AWS.S3({ signatureVersion: 'v4' });

  var name = getToday()
  if (event.queryStringParameters != null) {
    name = event.queryStringParameters.resultFileName || name;
  }
  const filePath = "linpack/" + name;
  const start = Date.now();
  var btime;
  exec("cat /proc/stat | grep btime", (error, stdout, stderr) => {
    btime = stdout;
  });
  var model;
  exec("cat /proc/cpuinfo | grep model", (error, stdout, stderr) => {
    model = stdout;
  });

  exec("./runme64", (error, stdout, stderr) => {
    const end = Date.now();
    const diff = end - start;

    const rows = stdout.split("\n")
    const row = rows[rows.length - 8];
    const flops = parseFloat(row.split(/[\s,]+/)[4]);

    const additionalInfo = flops + " " + start + " " + end + " " + diff;
    const body = [btime, model, stdout].join("\n");
    s3.putObject({
      Bucket: 'calibration-linpack',
      Key: filePath,
      Body: body
    }, callback(additionalInfo));
  });

  function callback(additionalInfo) {
    return (err, resp) => {
      if (err) {
        console.log(err);
        context.error(err);
      } else {
        console.log('Successfully uploaded results.');
        console.log(additionalInfo);
        var response = {
          "statusCode": 200,
          "body": filePath,
          "isBase64Encoded": false
        };
        context.succeed(response);
      }
    }
  }

  function getToday() {
    var today = new Date();
    var yyyy = today.getFullYear();
    var MM = String(today.getMonth() + 1).padStart(2, '0');
    var DD = String(today.getDate()).padStart(2, '0');
    var HH = String(today.getHours()).padStart(2, '0');
    var mm = String(today.getMinutes()).padStart(2, '0');
    return yyyy + '_' + MM + '_' + DD + '-' + HH + '_' + mm;
  }

};

