'use strict';

const uuidv4 = require('uuid/v4');
const containerId = uuidv4();

module.exports.handler = (event, context, callback) => {
    var n = event.n;

    if (!n || !isNumeric(n)) {
        context.fail(createErrorResponse("Please pass a valid number 'n'.", context.awsRequestId));
        return;
    }

    n = parseInt(n);
    var result = fib(n);

    context.succeed(createSuccessResponse(result, context.awsRequestId));
};

var fib = function(n) {
    if (n <= 1) {
        return 1;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
};

var createErrorResponse = function (message, requestId) {
    var response = {
        result: "[400] " + message,
        platformId: requestId,
        containerId: containerId
    };

    return JSON.stringify(response);
};

var createSuccessResponse = function (message, requestId) {
    return {
        result: message,
        platformId: requestId,
        containerId: containerId
    }
};

var isNumeric = function(value) {
    return /^\d+$/.test(value);
};
