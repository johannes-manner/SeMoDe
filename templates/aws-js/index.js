'use strict';

const fs = require("fs")
const uuid = require('uuid');
const containerId = uuid.v4();

class ClassWithStaticAttribute {
    static cold = true;
}

module.exports.handler = (event, context, callback) => {
    var n = event.n;

	var vmIdentificationContent = fs.readFileSync('//proc/self/cgroup', 'utf8');
    var vmIdentification = (vmIdentificationContent.split("sandbox-root-")[1]).substring(0,6);
    var cpuModel = extractMetaInfo("//proc/cpuinfo", "model", ":", 0);
    var cpuModelName = extractMetaInfo("//proc/cpuinfo", "model", ":", 1);

	var coldResponse = ClassWithStaticAttribute.cold;
	ClassWithStaticAttribute.cold = false;

    if (!n || !isNumeric(n)) {
        context.fail(createErrorResponse(coldResponse, "Please pass a valid number 'n'.", context.awsRequestId, vmIdentification, cpuModel, cpuModelName));
        return;
    }

    n = parseInt(n);
    var result = fib(n);

    context.succeed(createSuccessResponse(coldResponse, result, context.awsRequestId, vmIdentification, cpuModel, cpuModelName));
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

var createErrorResponse = function (coldResponseEntity, message, requestId, vmIdentification, cpuModel, cpuModelName) {
    var response = {
		cold: coldResponseEntity,
        result: "[400] " + message,
        platformId: requestId,
        containerId: containerId,
        vmIdentification: vmIdentification,
        cpuModel: cpuModel,
        cpuModelName: cpuModelName
    };

    return JSON.stringify(response);
};

var createSuccessResponse = function (coldResponseEntity, message, requestId, vmIdentification, cpuModel, cpuModelName) {
    return {
		cold: coldResponseEntity,
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
