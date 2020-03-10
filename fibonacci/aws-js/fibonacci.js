'use strict';

const fs = require("fs")
const uuidv4 = require('uuid/v4');
const containerId = uuidv4();

module.exports.handler = (event, context, callback) => {
    var n = event.n;

    var vmIdentification = extractMetaInfo('//proc/stat', "btime", " ", 0);
    var cpuModel = extractMetaInfo("//proc/cpuinfo", "model", ":", 0);
    var cpuModelName = extractMetaInfo("//proc/cpuinfo", "model", ":", 1);


    if (!n || !isNumeric(n)) {
        context.fail(createErrorResponse("Please pass a valid number 'n'.", context.awsRequestId, vmIdentification, cpuModel, cpuModelName));
        return;
    }

    n = parseInt(n);
    var result = fib(n);

    context.succeed(createSuccessResponse(result, context.awsRequestId, vmIdentification, cpuModel, cpuModelName));
};

var extractMetaInfo = function(file, searchString, delimeter, occurence) {
    var content = fs.readFileSync(file, 'utf8');
    var value = content.split("\n").filter(line => line.startsWith(searchString)).map(s => s.split(delimeter)[1])[occurence];
    return value;
}

var fib = function(n) {
    if (n <= 1) {
        return 1;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
};

var createErrorResponse = function (message, requestId, vmIdentification, cpuModel, cpuModelName) {
    var response = {
        result: "[400] " + message,
        platformId: requestId,
        containerId: containerId,
        vmIdentification: vmIdentification,
        cpuModel: cpuModel,
        cpuModelName: cpuModelName
    };

    return JSON.stringify(response);
};

var createSuccessResponse = function (message, requestId, vmIdentification, cpuModel, cpuModelName) {
    return {
        result: message,
        platformId: requestId,
        containerId: containerId,
        vmIdentification: vmIdentification,
        cpuModel: cpuModel,
        cpuModelName: cpuModelName
    }
};

var isNumeric = function(value) {
    return /^\d+$/.test(value);
};
